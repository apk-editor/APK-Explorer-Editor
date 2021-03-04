package com.apk.editor.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ImageViewActivity extends AppCompatActivity {

    public static final String PATH_INTENT = "path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mExport = findViewById(R.id.export);
        AppCompatImageView mImage = findViewById(R.id.image);
        MaterialTextView mTitle = findViewById(R.id.title);

        String path = getIntent().getStringExtra(PATH_INTENT);

        if (path != null) {
            mTitle.setText(new File(path).getName());
            mImage.setImageURI(APKExplorer.getIconFromPath(path));
        } else {
            mTitle.setText(AppData.getAppName(APKExplorer.mAppID, this));
            mImage.setImageDrawable(AppData.getAppIcon(APKExplorer.mAppID, this));
        }

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.export_question)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialog, id) -> {
                    APKEditorUtils.mkdir(getExternalFilesDir("") + "/" + APKExplorer.mAppID);
                    if (path != null) {
                        APKExplorer.saveImage(BitmapFactory.decodeFile(path), getExternalFilesDir("") + "/" + APKExplorer.mAppID + "/" + new File(path).getName());
                    } else {
                        APKExplorer.saveImage(APKExplorer.drawableToBitmap(mImage.getDrawable()),getExternalFilesDir("") + "/" + APKExplorer.mAppID + "/icon.png");
                    }
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(getString(R.string.export_complete_message, getExternalFilesDir("") + "/" + APKExplorer.mAppID))
                            .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                            }).show();
                }).show());

        mBack.setOnClickListener(v -> finish());
    }

}