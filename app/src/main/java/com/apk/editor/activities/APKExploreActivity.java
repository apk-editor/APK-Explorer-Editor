package com.apk.editor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.apk.editor.R;
import com.apk.editor.fragments.APKExplorerFragment;
import com.apk.editor.fragments.ExploredInfoFragment;
import com.apk.editor.fragments.StringViewFragment;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.tasks.SignAPK;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends AppCompatActivity {

    public static final String BACKUP_PATH_INTENT = "backup_path";
    private Fragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer);

        AppCompatImageButton mBuild = findViewById(R.id.build);
        AppCompatImageView mApplicationIcon = findViewById(R.id.app_image);
        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);
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

        Menu menu = mBottomNav.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, null).setIcon(R.drawable.ic_info);
        menu.add(Menu.NONE, 1, Menu.NONE, null).setIcon(R.drawable.ic_folder);
        if (sFileUtils.exist(mBackupFilePath.replace("/.aeeBackup/appData", "/resources.arsc"))) {
            menu.add(Menu.NONE, 2, Menu.NONE, null).setIcon(R.drawable.ic_string);
        }

        mBottomNav.setOnItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case 0:
                            mFragment = getExploreInfoFragment(mBackupFilePath);
                            break;
                        case 1:
                            mFragment = getAPKExplorerFragment(mBackupFilePath, mPackageName.getText().toString().trim());
                            break;
                        case 2:
                            mFragment = getStringFragment(mBackupFilePath.replace("/.aeeBackup/appData", "/resources.arsc"));
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mFragment).commit();
                    return true;
                }
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    getExploreInfoFragment(mBackupFilePath)).commit();
        }
    }

    private Fragment getAPKExplorerFragment(String backupFilePath, String packageName) {
        Bundle bundle = new Bundle();
        bundle.putString("backupFilePath", backupFilePath);
        bundle.putString("packageName", packageName);

        Fragment fragment = new APKExplorerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Fragment getExploreInfoFragment(String backupFilePath) {
        Bundle bundle = new Bundle();
        bundle.putString("backupFilePath", backupFilePath);

        Fragment fragment = new ExploredInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Fragment getStringFragment( String resFilePath) {
        Bundle bundle = new Bundle();
        bundle.putString("resFilePath", resFilePath);

        Fragment fragment = new StringViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

}