package com.apk.editor.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.apk.editor.utils.AppData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditorActivity extends AppCompatActivity {

    private AppCompatEditText mText;
    public static final String PATH_INTENT = "path";
    private String mTextContents = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        MaterialTextView mTitle = findViewById(R.id.title);
        mText = findViewById(R.id.text);

        AppData.toggleKeyboard(1, mText, this);

        String mPath = getIntent().getStringExtra(PATH_INTENT);

        mText.setTextColor(sUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        mTitle.setText(new File(mPath).getName());
        mText.setText(sUtils.read(new File(mPath)));
        mTextContents = sUtils.read(new File(mPath));
        mSave.setVisibility(View.VISIBLE);

        mSave.setOnClickListener(v -> saveDialog(Objects.requireNonNull(mText.getText()).toString().trim(), mPath));

        mBack.setOnClickListener(v -> onBackPressed());
    }

    private void saveDialog(String text, String path) {
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.save_question)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.save), (dialog, id) -> {
                    sUtils.create(text, new File(path));
                    if (path.contains("classes") && path.contains(".dex")) {
                        String parentPath = path.split(".dex")[0] + ".dex";
                        if (!sUtils.exist(new File(parentPath, "edited"))) {
                            sUtils.create("# Edited", new File(parentPath, "edited"));
                        }
                    }
                    finish();
                }).show();
    }

    @Override
    public void onBackPressed() {
        if (mTextContents != null && mText.getText() != null && !mTextContents.equals(mText.getText().toString())) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.text_editor)
                    .setMessage(getString(R.string.discard_message))
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .setPositiveButton(R.string.discard, (dialogInterface, i) -> finish()).show();
            return;
        }
        super.onBackPressed();
    }

}