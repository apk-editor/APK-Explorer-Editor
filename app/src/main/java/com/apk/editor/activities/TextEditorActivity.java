package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditorActivity extends AppCompatActivity {

    private AppCompatEditText mText;
    private File mFile = null;
    public static final String PATH_INTENT = "path";
    private String mExternalFile = null, mTextContents = null;

    @SuppressLint({"UseCompatLoadingForDrawables", "Range"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mMenu = findViewById(R.id.menu);
        AppCompatImageButton mSave = findViewById(R.id.save);
        LinearLayoutCompat mMainLayout = findViewById(R.id.main_layout);
        MaterialTextView mTitle = findViewById(R.id.title);
        mText = findViewById(R.id.text);

        AppData.toggleKeyboard(1, mText, this);

        String mPath = getIntent().getStringExtra(PATH_INTENT);

        mText.setTextColor(APKEditorUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        if (getIntent().getData() != null) {
            if (APKExplorer.isPermissionDenied(this)) {
                LinearLayoutCompat mPermissionLayout = findViewById(R.id.permission_layout);
                MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
                mPermissionLayout.setVisibility(View.VISIBLE);
                mMainLayout.setVisibility(View.GONE);
                mPermissionGrant.setOnClickListener(v -> APKExplorer.requestPermission(this));
                return;
            }

            try {
                InputStream inputStream = getContentResolver().openInputStream(getIntent().getData());
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                for (int result = bis.read(); result != -1; result = bis.read()) {
                    buf.write((byte) result);
                }
                mExternalFile = buf.toString("UTF-8");
            } catch (IOException ignored) {}

            if (mExternalFile != null) {
                Uri uri = getIntent().getData();
                assert uri != null;
                if (APKEditorUtils.isDocumentsUI(uri)) {
                    @SuppressLint("Recycle")
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
                    }
                } else {
                    mFile = new File(APKEditorUtils.getPath(getIntent().getData().getPath()));
                }
                if (mFile != null && mFile.exists()) {
                    mTitle.setText(mFile.getName());
                    if (Build.VERSION.SDK_INT >= 29) {
                        mMenu.setVisibility(View.GONE);
                        mSave.setVisibility(View.GONE);
                    } else {
                        mSave.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_save));
                        mMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_dots));
                        mSave.setVisibility(View.VISIBLE);
                        mMenu.setVisibility(View.VISIBLE);
                    }
                } else {
                    mMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_save));
                    mSave.setVisibility(View.GONE);
                }
                mText.setText(mExternalFile);
                mTextContents = mExternalFile;
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.text_editor)
                        .setMessage(getString(R.string.file_path_error))
                        .setCancelable(false)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
            }
        } else if (mPath != null && APKEditorUtils.exist(mPath)) {
            mTitle.setText(new File(mPath).getName());
            mText.setText(APKEditorUtils.read(mPath));
            mTextContents = APKEditorUtils.read(mPath);
            mSave.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_save));
            mSave.setVisibility(View.VISIBLE);
        }

        mMenu.setOnClickListener(v -> {
            if (mExternalFile == null) {
                saveDialog(Objects.requireNonNull(mText.getText()).toString().trim(), mPath);
                return;
            }
            PopupMenu popupMenu = new PopupMenu(this, mMenu);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.share));
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.delete));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        Uri uriFile = FileProvider.getUriForFile(this,
                                BuildConfig.APPLICATION_ID + ".provider", mFile);
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("*/*");
                        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                        share.putExtra(Intent.EXTRA_STREAM, uriFile);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(share, getString(R.string.share_with)));
                        break;
                    case 1:
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.delete_question, mFile.getName()))
                                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                    mFile.delete();
                                    finish();
                                }).show();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        mSave.setOnClickListener(v -> saveDialog(Objects.requireNonNull(mText.getText()).toString().trim(), (mFile != null  && mFile.exists()? mFile.getAbsolutePath() : mPath)));

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
                    APKEditorUtils.create(text, path);
                    if (mExternalFile == null && path.contains("classes") && path.contains(".dex")) {
                        String parentPath = path.split(".dex")[0] + ".dex";
                        if (!APKEditorUtils.exist(new File(parentPath, "edited").getAbsolutePath())) {
                            APKEditorUtils.create("# Edited", new File(parentPath, "edited").getAbsolutePath());
                        }
                    }
                    finish();
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1 && Build.VERSION.SDK_INT < 30 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }
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