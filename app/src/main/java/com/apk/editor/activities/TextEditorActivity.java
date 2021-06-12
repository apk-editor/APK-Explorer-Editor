package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditorActivity extends AppCompatActivity {

    private AppCompatEditText mText;
    public static final String PATH_INTENT = "path";
    private String mExternalFile = null, mTextContents = null;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mMenu = findViewById(R.id.menu);
        AppCompatImageButton mSave = findViewById(R.id.save);
        LinearLayout mMainLayout = findViewById(R.id.main_layout);
        MaterialTextView mTitle = findViewById(R.id.title);
        mText = findViewById(R.id.text);

        AppData.toggleKeyboard(1, mText, this);

        String mPath = getIntent().getStringExtra(PATH_INTENT);

        mText.setTextColor(APKEditorUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        if (getIntent().getData() != null) {
            if (APKExplorer.isPermissionDenied(this)) {
                LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
                MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
                MaterialTextView mPermissionText = findViewById(R.id.permission_text);
                mPermissionText.setText(Build.VERSION.SDK_INT >= 30 ? getString(R.string.file_permission_request_message,
                        getString(R.string.app_name)) : getString(R.string.permission_denied_message));
                mPermissionLayout.setVisibility(View.VISIBLE);
                mMainLayout.setVisibility(View.GONE);
                mPermissionGrant.setOnClickListener(v -> {
                    APKExplorer.requestPermission(this);
                    if (Build.VERSION.SDK_INT < 30) finish();
                });
                return;
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
                mMenu.setVisibility(View.VISIBLE);
            }
            if (mExternalFile != null && APKEditorUtils.exist(mExternalFile)) {
                mTitle.setText(new File(mExternalFile).getName());
                mText.setText(APKEditorUtils.read(mExternalFile));
                mTextContents = APKEditorUtils.read(mExternalFile);
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
            mTextContents = APKEditorUtils.read(mPath);
        }

        if (mExternalFile != null) {
            mSave.setVisibility(View.VISIBLE);
            mSave.setImageDrawable(getResources().getDrawable(R.drawable.ic_save));
            mMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_dots));
        } else {
            mMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_save));
        }

        mMenu.setOnClickListener(v -> {
            if (mExternalFile == null) {
                saveDialog(Objects.requireNonNull(mText.getText()).toString(), mExternalFile != null ? mExternalFile : mPath);
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
                                BuildConfig.APPLICATION_ID + ".provider", new File(mExternalFile));
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("*/*");
                        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                        share.putExtra(Intent.EXTRA_STREAM, uriFile);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(share, getString(R.string.share_with)));
                        break;
                    case 1:
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.delete_question, new File(mExternalFile).getName()))
                                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                    APKEditorUtils.delete(mExternalFile);
                                    finish();
                                }).show();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        mSave.setOnClickListener(v -> saveDialog(Objects.requireNonNull(mText.getText()).toString(), mExternalFile != null ? mExternalFile : mPath));

        mBack.setOnClickListener(v -> onBackPressed());
    }

    private void saveDialog(String text, String path) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.save_question)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.save), (dialog, id) -> {
                    APKEditorUtils.create(text, path);
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