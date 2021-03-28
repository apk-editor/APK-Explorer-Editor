package com.apk.editor.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditorActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";
    private String mExternalFile = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        MaterialTextView mTitle = findViewById(R.id.title);
        AppCompatEditText mText = findViewById(R.id.text);

        AppData.toggleKeyboard(1, mText, this);

        String mPath = getIntent().getStringExtra(PATH_INTENT);

        mText.setTextColor(APKEditorUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        if (getIntent().getData() != null) {
            if (!APKEditorUtils.isWritePermissionGranted(this)) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                finish();
            }
            Uri uri = getIntent().getData();
            assert uri != null;
            File file = new File(Objects.requireNonNull(uri.getPath()));
            if (APKEditorUtils.isDocumentsUI(uri)) {
                @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mExternalFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" +
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                mExternalFile = APKEditorUtils.getPath(file);
            }
            if (mExternalFile != null && APKEditorUtils.exist(mExternalFile)) {
                mTitle.setText(new File(mExternalFile).getName());
                mText.setText(APKEditorUtils.read(mExternalFile));
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.text_editor)
                        .setMessage(getString(R.string.file_path_error))
                        .setCancelable(false)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                            finish();
                        }).show();
            }
        } else if (mPath != null) {
            mTitle.setText(new File(mPath).getName());
            mText.setText(APKEditorUtils.read(mPath));
        }

        mSave.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.save_question)
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> {
                        APKEditorUtils.create(Objects.requireNonNull(mText.getText()).toString(), mExternalFile != null ? mExternalFile : mPath);
                        finish();
                    }).show());

        mBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}