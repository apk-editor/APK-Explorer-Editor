package com.apk.editor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.apk.editor.R;
import com.apk.editor.fragments.APKExplorerFragment;
import com.apk.editor.fragments.ExploredInfoFragment;
import com.apk.editor.fragments.StringViewFragment;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.tasks.SignAPK;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import navView.NavView;
import navView.serializables.NavViewEntry;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends BaseActivity {

    public static final String BACKUP_PATH_INTENT = "backup_path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer, R.id.layout_root);

        AppCompatImageButton mBuild = findViewById(R.id.build);
        AppCompatImageView mApplicationIcon = findViewById(R.id.app_image);
        NavView mNavView = findViewById(R.id.nav_view);
        MaterialTextView mApplicationName = findViewById(R.id.app_title);
        MaterialTextView mPackageName = findViewById(R.id.package_id);

        String mBackupFilePath = getIntent().getStringExtra(BACKUP_PATH_INTENT);
        File mRootFile = new File(Objects.requireNonNull(mBackupFilePath).replace("/.aeeBackup/appData", ""));
        if (APKExplorer.getAppIcon(mBackupFilePath) != null) {
            mApplicationIcon.setImageBitmap(APKExplorer.getAppIcon(mBackupFilePath));
        }
        mApplicationName.setText(APKExplorer.getAppName(mBackupFilePath));
        mPackageName.setText(APKExplorer.getPackageName(mBackupFilePath));
        mPackageName.setVisibility(View.VISIBLE);

        if (APKEditorUtils.isFullVersion(this)) {
            mBuild.setVisibility(View.VISIBLE);
        }

        mBuild.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.save_apk_message)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.build), (dialog, id) -> {
                    if (!sCommonUtils.getBoolean("firstSigning", false, this)) {
                        new sSingleItemDialog(0, null, new String[] {
                                getString(R.string.signing_default),
                                getString(R.string.signing_custom)
                        }, this) {

                            @Override
                            public void onItemSelected(int itemPosition) {
                                sCommonUtils.saveBoolean("firstSigning", true, APKExploreActivity.this);
                                if (itemPosition == 0) {
                                    new SignAPK(mRootFile, APKExploreActivity.this).execute();
                                } else {
                                    Intent signing = new Intent(APKExploreActivity.this, APKSignActivity.class);
                                    startActivity(signing);
                                }
                            }
                        }.show();
                    } else {
                        new SignAPK(mRootFile, this).execute();
                    }
                }).show()
        );

        List<NavViewEntry> data = new CopyOnWriteArrayList<>();
        data.add(new NavViewEntry(() -> ExploredInfoFragment.newInstance(mBackupFilePath), R.drawable.ic_info));
        data.add(new NavViewEntry(() -> APKExplorerFragment.newInstance(mBackupFilePath, mPackageName.getText().toString().trim()), R.drawable.ic_folder));
        if (sFileUtils.exist(mBackupFilePath.replace("/.aeeBackup/appData", "/resources.arsc"))) {
            data.add(new NavViewEntry(() -> StringViewFragment.newInstance(mBackupFilePath), R.drawable.ic_string));
        }

        mNavView.setNavigationItems(data);

        mNavView.setOnItemSelectedListener((fragment, position) -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    ExploredInfoFragment.newInstance(mBackupFilePath)).commit();
        }
    }

}