package com.apk.editor.utils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 23, 2023
 * Based on the original work of Iyxan23 for zipalign-java
 * Ref: https://github.com/iyxan23/zipalign-java
 */
public class ZipAlign {

    private static final int maxEOCDLookup = 0xffff + 22;

    public static void alignZip(RandomAccessFile file, OutputStream out) throws IOException {
        alignZip(file, out, 4, 16384);
    }

    public static void alignZip(RandomAccessFile file, OutputStream out, int alignment, int soFileAlignment)
            throws IOException {

        // find the end of central directory
        long seekStart;
        int readAmount;
        final long fileLength = file.length();

        if (fileLength > maxEOCDLookup) {
            seekStart = fileLength - maxEOCDLookup;
            readAmount = maxEOCDLookup;
        } else {
            seekStart = 0;
            readAmount = (int) fileLength;
        }

        // find the signature
        file.seek(seekStart);

        int i;
        for (i = readAmount - 4; i >= 0; i--) {
            if (file.readByte() != 0x50) continue;
            file.seek(file.getFilePointer() - 1);
            if (file.readInt() == 0x504b0506) break; // EOCD signature (in big-endian)
        }

        if (i < 0)
            throw new IOException("No end-of-central-directory found");

        long eocdPosition = file.getFilePointer() - 4;

        // skip disk fields
        file.seek(eocdPosition + 10);

        byte[] buf = new byte[10]; // we're keeping the total entries (2B), central dir size (4B), and the offset (4B)
        file.read(buf);
        ByteBuffer eocdBuffer = ByteBuffer.wrap(buf)
                .order(ByteOrder.LITTLE_ENDIAN);

        // read em
        short totalEntries = eocdBuffer.getShort();
        int centralDirOffset = eocdBuffer.getInt();

        ArrayList<Alignment> neededAlignments = new ArrayList<>();
        ArrayList<FileOffsetShift> shifts = new ArrayList<>();

        // to keep track of how many bytes we've shifted through the whole file (because we're going to pad null bytes
        // to align)
        int shiftAmount = 0;

        file.seek(centralDirOffset);
        byte[] entry = new byte[46]; // not including the filename, extra field, and file comment
        ByteBuffer entryBuffer = ByteBuffer.wrap(entry)
                .order(ByteOrder.LITTLE_ENDIAN);

        for (int ei = 0; ei < totalEntries; ei++) {
            final long entryStart = file.getFilePointer();
            file.read(entry);

            if (entryBuffer.getInt(0) != 0x02014b50)
                throw new IOException(
                        "assumed central directory entry at " + entryStart + " doesn't start with a signature"
                );

            short entry_fileNameLen = entryBuffer.getShort(28);
            short entry_extraFieldLen = entryBuffer.getShort(30);
            short entry_commentLen = entryBuffer.getShort(32);
            int fileOffset = entryBuffer.getInt(42);

            if (shiftAmount != 0)
                shifts.add(new FileOffsetShift(entryStart + 42, fileOffset + shiftAmount));

            boolean soAligned = false;

            // read the filename to check whether it is an .so file (of which we shall align if alignSoFiles is true)
            if (soFileAlignment != 0) {
                byte[] filenameBuffer = new byte[entry_fileNameLen];
                file.read(filenameBuffer);

                String filename = new String(filenameBuffer, StandardCharsets.UTF_8);
                if (filename.endsWith(".so")) {
                    // we got to align this
                    file.seek(fileOffset + 26); // skip all fields before filename length

                    // read the filename & extra field length
                    ByteBuffer lengths = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                    file.read(lengths.array());
                    short fileNameLen = lengths.getShort();
                    short extraFieldLen = lengths.getShort();

                    // calculate the amount of alignment needed
                    long dataPos = fileOffset + 30 + fileNameLen + extraFieldLen + shiftAmount;
                    int wrongOffset = (int) (dataPos % soFileAlignment);
                    int alignAmount = wrongOffset == 0 ? 0 : (soFileAlignment - wrongOffset);
                    shiftAmount += alignAmount;

                    // only align when alignAmount is not 0 (not already aligned)
                    if (alignAmount != 0) {
                        // push it!
                        neededAlignments.add(new Alignment(
                                alignAmount,
                                fileOffset + 28,
                                (short) (extraFieldLen + alignAmount),
                                fileNameLen + extraFieldLen
                        ));
                    }

                    soAligned = true;
                }
            }

            // if this file is uncompressed, and it has not been aligned, we align it
            if (entryBuffer.getShort(10) == 0 && !soAligned) {
                // temporarily seek to the file header to calculate the alignment amount
                file.seek(fileOffset + 26); // skip all fields before filename length

                // read the filename & extra field length
                ByteBuffer lengths = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                file.read(lengths.array());
                short fileNameLen = lengths.getShort();
                short extraFieldLen = lengths.getShort();

                // calculate the amount of alignment needed
                long dataPos = fileOffset + 30 + fileNameLen + extraFieldLen + shiftAmount;
                int wrongOffset = (int) (dataPos % alignment);
                int alignAmount = wrongOffset == 0 ? 0 : (alignment - wrongOffset);
                shiftAmount += alignAmount;

                // only align when alignAmount is not 0 (not already aligned)
                if (alignAmount != 0) {
                    // push it!
                    neededAlignments.add(new Alignment(
                            alignAmount,
                            fileOffset + 28,
                            (short) (extraFieldLen + alignAmount),
                            fileNameLen + extraFieldLen
                    ));
                }
            }

            file.seek(entryStart + 46 + entry_fileNameLen + entry_extraFieldLen + entry_commentLen);
        }

        // done analyzing! now we're going to stream the aligned zip
        file.seek(0);
        if (neededAlignments.isEmpty()) {
            // there is no needed alignment, stream it all!
            byte[] buffer = new byte[8192];
            int len;
            while (-1 != (len = file.read(buffer))) {
                out.write(buffer, 0, len);
            }
            return;
        }

        // alignments needed! this aligns files to the defined boundaries by padding null bytes to the extra field
        for (Alignment al : neededAlignments) {
            if (al.extraFieldLenOffset != 0) {
                passBytes(file, out, al.extraFieldLenOffset - file.getFilePointer());
            }

            // write the changed extra field length (in little-endian)
            out.write(al.extraFieldLenValue & 0xFF);
            out.write((al.extraFieldLenValue >>> 8) & 0xFF);
            file.readShort(); // mirror the new position to the file

            passBytes(file, out, al.extraFieldExtensionOffset);

            byte[] padding = new byte[al.alignAmount];
            out.write(padding); // sneak in null bytes
            out.flush();
        }

        // the code below overrides the bytes that reference to other parts of the file that may be shifted
        // due to the fact that we're padding bytes to align uncompressed data

        // this changes the "file offset" defined in EOCD headers
        for (FileOffsetShift shift : shifts) {
            // write data before this
            passBytes(file, out, shift.eocdhPosition - file.getFilePointer());

            // write shifted file offset (in litte-endian)
            out.write(shift.shiftedFileOffset & 0xFF);
            out.write((shift.shiftedFileOffset >>> 8) & 0xFF);
            out.write((shift.shiftedFileOffset >>> 16) & 0xFF);
            out.write((shift.shiftedFileOffset >>> 24) & 0xFF);
            file.readInt(); // mirror the new position to the file
        }

        // after that we need to edit the EOCDR's "EOCDH start offset" field
        passBytes(file, out, eocdPosition + 0x10 - file.getFilePointer());
        int shiftedCDOffset = centralDirOffset + shiftAmount;

        out.write(shiftedCDOffset & 0xFF);
        out.write((shiftedCDOffset >>> 8) & 0xFF);
        out.write((shiftedCDOffset >>> 16) & 0xFF);
        out.write((shiftedCDOffset >>> 24) & 0xFF);
        file.readInt(); // mirror the new position change

        // write all that's left
        passBytes(file, out, file.length() - file.getFilePointer());
    }

    private static class Alignment {
        public int alignAmount;
        public long extraFieldLenOffset;
        public short extraFieldLenValue;
        public int extraFieldExtensionOffset;

        public Alignment(int alignAmount, long extraFieldLenOffset, short extraFieldLenValue,
                         int extraFieldExtensionOffset) {
            this.alignAmount = alignAmount;
            this.extraFieldLenOffset = extraFieldLenOffset;
            this.extraFieldLenValue = extraFieldLenValue;
            this.extraFieldExtensionOffset = extraFieldExtensionOffset;
        }

        @NonNull
        @Override
        public String toString() {
            return "Alignment{" +
                    "alignAmount=" + alignAmount +
                    ", extraFieldLenOffset=" + extraFieldLenOffset +
                    ", extraFieldLenValue=" + extraFieldLenValue +
                    ", extraFieldExtensionOffset=" + extraFieldExtensionOffset +
                    '}';
        }
    }

    private static class FileOffsetShift {
        public long eocdhPosition;
        public int shiftedFileOffset;

        public FileOffsetShift(long eocdhPosition, int shiftedFileOffset) {
            this.eocdhPosition = eocdhPosition;
            this.shiftedFileOffset = shiftedFileOffset;
        }

        @NonNull
        @Override
        public String toString() {
            return "FileOffsetShift{" +
                    "eocdhPosition=" + eocdhPosition +
                    ", shiftedFileOffset=" + shiftedFileOffset +
                    '}';
        }
    }

    private static void passBytes(RandomAccessFile raf, OutputStream out, long len) throws IOException {
        byte[] buffer = new byte[8162];

        long left;
        for (left = len; left > 8162; left -= 8162) {
            raf.read(buffer);
            out.write(buffer);
        }

        if (left != 0) {
            buffer = new byte[(int) left];
            raf.read(buffer);
            out.write(buffer);
        }

        out.flush();
    }

}