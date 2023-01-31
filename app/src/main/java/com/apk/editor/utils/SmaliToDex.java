package com.apk.editor.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.apk.editor.R;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 12, 2021
 * The following code is based on the original work of @iBotPeaches (ref: https://github.com/iBotPeaches/Apktool)
 * and @JesusFreke (ref: https://github.com/JesusFreke/smali)
 */
public class SmaliToDex {

    private final Context mContext;
    private final File mDexFile, mSmaliDir;
    private final int mApiLevel;
    private final List<File> mFiles = new ArrayList<>();

    public SmaliToDex(File smaliDir, File dexFile, int apiLevel, Context context) {
        mSmaliDir = smaliDir;
        mDexFile = dexFile;
        mApiLevel = apiLevel;
        mContext = context;
    }

    @SuppressLint("StringFormatInvalid")
    private static void buildFile(int api, File file, DexBuilder dexBuilder, Context context) {
        try {
            FileInputStream inStream = new FileInputStream(file);
            if (!assembleSmaliFile(file, dexBuilder, api)) {
                Common.setStatus(context.getString(R.string.assembling, file.getName()) + " : " + context.getString(R.string.failed));
                Common.setError(Common.getError() + 1);
                Common.getErrorList().add(file.getAbsolutePath());
                throw new RuntimeException("Could not smali file: " + file.getName());
            }
            Common.setSuccess(Common.getSuccess() + 1);
            Common.setStatus(context.getString(R.string.assembling, file.getName()) + " : " + context.getString(R.string.success));
            inStream.close();
        } catch (Exception ignored) {}
    }

    private static List<File> getSmaliFiles(File file, List<File> fileList) {
        for (File files : Objects.requireNonNull(file.listFiles())) {
            if (files.isDirectory()) {
                getSmaliFiles(files, fileList);
            } else if (files.getName().endsWith(".smali")) {
                fileList.add(files);
            }
        }
        return fileList;
    }

    private static boolean assembleSmaliFile(File smaliFile, DexBuilder dexBuilder, int apiLevel) throws Exception {
        if (Common.isCancelled()) {
            return false;
        }
        try (FileInputStream fis = new FileInputStream(smaliFile)) {
            InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);

            smaliFlexLexer lexer = new smaliFlexLexer(reader, apiLevel);
            lexer.setSourceFile(smaliFile);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            smaliParser parser = new smaliParser(tokens);
            parser.setVerboseErrors(false);
            parser.setAllowOdex(false);
            parser.setApiLevel(apiLevel);

            smaliParser.smali_file_return result = parser.smali_file();
            if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
                return false;
            }

            CommonTree t = result.getTree();
            CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
            treeStream.setTokenStream(tokens);

            smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);
            dexGen.setApiLevel(apiLevel);
            dexGen.setVerboseErrors(false);
            dexGen.setDexBuilder(dexBuilder);
            dexGen.smali_file();

            return dexGen.getNumberOfSyntaxErrors() == 0;
        }
    }

    void execute() {
        DexBuilder dexBuilder;
        if (mApiLevel > 0) {
            dexBuilder = new DexBuilder(Opcodes.forApi(mApiLevel));
        } else {
            dexBuilder = new DexBuilder(Opcodes.getDefault());
        }

        for (File file : getSmaliFiles(mSmaliDir, mFiles)) {
            buildFile(mApiLevel, file, dexBuilder, mContext);
        }
        try {
            dexBuilder.writeTo(new FileDataStore(mDexFile));
        } catch (IOException ignored) {
        }
    }

}