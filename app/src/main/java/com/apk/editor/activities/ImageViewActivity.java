package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Projects;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.IOException;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ImageViewActivity extends AppCompatActivity {

    private Bitmap mBitmap = null;
    private File mFile = null;
    public static final String PATH_INTENT = "path";

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mMenu = findViewById(R.id.export);
        AppCompatImageView mImage = findViewById(R.id.image);
        LinearLayout mMainLayout = findViewById(R.id.main_layout);
        MaterialTextView mTitle = findViewById(R.id.title);
        String path = getIntent().getStringExtra(PATH_INTENT);

        if (getIntent().getData() != null) {
            if (APKExplorer.isPermissionDenied(this)) {
                LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
                MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
                mPermissionLayout.setVisibility(View.VISIBLE);
                mMainLayout.setVisibility(View.GONE);
                mPermissionGrant.setOnClickListener(v -> APKExplorer.requestPermission(this));
                return;
            }
            Uri uri = getIntent().getData();

            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), getIntent().getData());
            } catch (IOException ignored) {
            }

            assert uri != null;
            if (APKEditorUtils.isDocumentsUI(uri)) {
                @SuppressLint("Recycle")
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
                }
            } else {
                mFile = new File(APKEditorUtils.getPath(uri.getPath()));
            }

            if (mBitmap != null) {
                mImage.setImageBitmap(mBitmap);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.image_viewer)
                        .setMessage(getString(R.string.file_path_error))
                        .setCancelable(false)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
            }

            if (mFile != null && mFile.exists()) {
                mTitle.setText(mFile.getName());
                if (Build.VERSION.SDK_INT >= 29) {
                    mMenu.setVisibility(View.GONE);
                } else {
                    mMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_settings));
                }
            } else {
                mMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_export));
            }
        } else {
            if (path != null) {
                mTitle.setText(new File(path).getName());
                mImage.setImageURI(APKExplorer.getIconFromPath(path));
            } else {
                mTitle.setText(AppData.getAppName(Common.getAppID(), this));
                mImage.setImageDrawable(AppData.getAppIcon(Common.getAppID(), this));
            }
            mMenu.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_export));
        }

        mMenu.setOnClickListener(v -> {
            if (mFile != null && mFile.exists()) {
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
                            share.setType("image/*");
                            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                            share.putExtra(Intent.EXTRA_STREAM, uriFile);
                            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(share, getString(R.string.share_with)));
                            break;
                        case 1:
                            new MaterialAlertDialogBuilder(this)
                                    .setMessage(getString(R.string.delete_question, mFile))
                                    .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> {
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
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.export_question)
                        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(getString(R.string.export), (dialog, id) -> {
                            if (APKExplorer.isPermissionDenied(this)) {
                                APKExplorer.requestPermission(this);
                            } else {
                                String mExportPath;
                                if (Build.VERSION.SDK_INT < 29) {
                                    APKEditorUtils.mkdir(Projects.getExportPath(this) + "/" + Common.getAppID());
                                    mExportPath = Projects.getExportPath(this) + "/" + Common.getAppID();
                                } else {
                                    mExportPath = Projects.getExportPath(this);
                                }
                                if (path != null) {
                                    APKExplorer.saveImage(BitmapFactory.decodeFile(path), mExportPath + "/" + new File(path).getName(), this);
                                } else {
                                    APKExplorer.saveImage(APKExplorer.drawableToBitmap(mImage.getDrawable()), mExportPath + "/icon.png", this);
                                }
                                new MaterialAlertDialogBuilder(this)
                                        .setMessage(getString(R.string.export_complete_message, mExportPath))
                                        .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                                        }).show();
                            }
                        }).show();
            }
        });

        mBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1 && Build.VERSION.SDK_INT < 30 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }
    }

}