package com.apk.editor.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apk.editor.R;
import com.apk.editor.fragments.APKExplorerFragment;
import com.apk.editor.utils.Common;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new APKExplorerFragment()).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uriFile = data.getData();

            if (uriFile != null) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.replace_file_question, new File(Common.getFileToReplace()).getName()))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.replace, (dialog, id) -> {
                            sUtils.copy(uriFile, new File(Common.getFileToReplace()), this);
                            recreate();
                        }).show();
            }
        }
    }

}