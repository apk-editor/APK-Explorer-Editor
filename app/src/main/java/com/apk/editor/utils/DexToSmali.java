package com.apk.editor.utils;

import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.InlineMethodResolver;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 08, 2021
 * The following code is based on the original work of @iBotPeaches (ref: https://github.com/iBotPeaches/Apktool)
 * and @JesusFreke (ref: https://github.com/JesusFreke/smali)
 */
public class DexToSmali {

    private final boolean mDebugInfo;
    private final File mApkFile, mOutDir;
    private final int mAPI;
    private final String mDEXName;

    DexToSmali(boolean debugInfo, File apkFile, File outDir, int api, String dexName) {
        this.mDebugInfo = debugInfo;
        this.mApkFile = apkFile;
        this.mOutDir = outDir;
        this.mAPI = api;
        this.mDEXName = dexName;
    }

    void execute() {
        try {
            final BaksmaliOptions options = new BaksmaliOptions();
            // options
            options.deodex = false;
            options.implicitReferences = false;
            options.parameterRegisters = true;
            options.localsDirective = true;
            options.sequentialLabels = true;
            options.debugInfo = mDebugInfo;
            options.codeOffsets = false;
            options.accessorComments = false;
            options.registerInfo = 0;
            options.inlineResolver = null;
            // set jobs automatically
            int jobs = Runtime.getRuntime().availableProcessors();
            if (jobs > 6) {
                jobs = 6;
            }
            // create the container
            MultiDexContainer<? extends DexBackedDexFile> container = DexFileFactory.loadDexContainer(mApkFile, Opcodes.forApi(mAPI));
            MultiDexContainer.DexEntry<? extends DexBackedDexFile> dexEntry;
            DexBackedDexFile dexFile;
            // If we have 1 item, ignore the passed file. Pull the DexFile we need.
            if (container.getDexEntryNames().size() == 1) {
                dexEntry = container.getEntry(container.getDexEntryNames().get(0));
            } else {
                dexEntry = container.getEntry(mDEXName);
            }
            // Double check the passed param exists
            if (dexEntry == null) {
                dexEntry = container.getEntry(container.getDexEntryNames().get(0));
            }
            assert dexEntry != null;
            dexFile = dexEntry.getDexFile();
            if (dexFile.supportsOptimizedOpcodes()) {
                throw new Exception("Warning: You are disassembling an odex file without deodexing it.");
            }
            if (dexFile instanceof DexBackedOdexFile) {
                options.inlineResolver = InlineMethodResolver.createInlineMethodResolver(((DexBackedOdexFile)dexFile).getOdexVersion());
            }
            Baksmali.disassembleDexFile(dexFile, mOutDir, jobs, options);
        } catch (Exception ignored) {
        }
    }

}