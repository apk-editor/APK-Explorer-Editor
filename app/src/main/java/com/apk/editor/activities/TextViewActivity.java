package com.apk.editor.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class TextViewActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        AppCompatImageButton mExport = findViewById(R.id.export);
        MaterialTextView mTitle = findViewById(R.id.title);
        AppCompatEditText mText = findViewById(R.id.text);

        String path = getIntent().getStringExtra(PATH_INTENT);

        assert path != null;
        mTitle.setText(new File(path).getName());
        if (APKExplorer.mAppID != null && path.endsWith(".xml")) {
            mText.setText(APKExplorer.readXMLFromAPK(AppData.getSourceDir(APKExplorer.mAppID, this), path.replace(
                    getCacheDir().getPath() + "/" + APKExplorer.mAppID + "/", "")));
        } else {
            mText.setText(APKEditorUtils.read(path));
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.export_question)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialog, id) -> {
                    APKEditorUtils.mkdir(getExternalFilesDir("") + "/" + APKExplorer.mAppID);
                    APKEditorUtils.copy(path, getExternalFilesDir("") + "/" + APKExplorer.mAppID + "/" + new File(path).getName());
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(getString(R.string.export_complete_message, getExternalFilesDir("") + "/" + APKExplorer.mAppID))
                            .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                            }).show();
                }).show());

        mSave.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.save_question)
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> {
                        APKEditorUtils.create(Objects.requireNonNull(mText.getText()).toString(), path);
                        finish();
                    }).show());

        mBack.setOnClickListener(v -> finish());
    }
}