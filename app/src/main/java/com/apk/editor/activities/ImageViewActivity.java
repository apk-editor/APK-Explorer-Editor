package com.apk.editor.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ImageViewActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";
    private String mExternalFile = null;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mMenu = findViewById(R.id.export);
        AppCompatImageView mImage = findViewById(R.id.image);
        MaterialTextView mTitle = findViewById(R.id.title);

        String path = getIntent().getStringExtra(PATH_INTENT);

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
                mImage.setImageURI(APKExplorer.getIconFromPath(mExternalFile));
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.image_viewer)
                        .setMessage(getString(R.string.file_path_error))
                        .setCancelable(false)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                            finish();
                        }).show();
            }
        } else if (path != null) {
            mTitle.setText(new File(path).getName());
            mImage.setImageURI(APKExplorer.getIconFromPath(path));
        } else {
            mTitle.setText(AppData.getAppName(APKExplorer.mAppID, this));
            mImage.setImageDrawable(AppData.getAppIcon(APKExplorer.mAppID, this));
        }

        if (mExternalFile != null) {
            mMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings));
        } else {
            mMenu.setImageDrawable(getResources().getDrawable(R.drawable.ic_export));
        }

        mMenu.setOnClickListener(v -> {
            if (mExternalFile != null) {
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
                            share.setType("image/*");
                            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                            share.putExtra(Intent.EXTRA_STREAM, uriFile);
                            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(share, getString(R.string.share_with)));
                            break;
                        case 1:
                            new MaterialAlertDialogBuilder(this)
                                    .setMessage(getString(R.string.delete_question, new File(mExternalFile).getName()))
                                    .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
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
            } else {
                if (!APKEditorUtils.isWritePermissionGranted(this)) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    APKEditorUtils.snackbar(findViewById(android.R.id.content), getString(R.string.permission_denied_message));
                    return;
                }
                new MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.export_question)
                        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(getString(R.string.export), (dialog, id) -> {
                            APKEditorUtils.mkdir(Projects.getExportPath(this) + "/" + APKExplorer.mAppID);
                            if (path != null) {
                                APKExplorer.saveImage(BitmapFactory.decodeFile(path), Projects.getExportPath(this) + "/" + APKExplorer.mAppID + "/" + new File(path).getName());
                            } else {
                                APKExplorer.saveImage(APKExplorer.drawableToBitmap(mImage.getDrawable()), Projects.getExportPath(this) + "/" + APKExplorer.mAppID + "/icon.png");
                            }
                            new MaterialAlertDialogBuilder(this)
                                    .setMessage(getString(R.string.export_complete_message, Projects.getExportPath(this) + "/" + APKExplorer.mAppID))
                                    .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                                    }).show();
                        }).show();
            }
        });

        mBack.setOnClickListener(v -> finish());
    }

}