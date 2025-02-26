package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.FilePickerAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKExplorer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 21, 2021
 */
public class FilePickerActivity extends AppCompatActivity {

    private ContentLoadingProgressBar mProgressLayout;
    private FilePickerAdapter mRecycleViewAdapter;
    private MaterialButton mSelect;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    public static final String TITLE_INTENT = "title", PATH_INTENT = "path";
    private static File mFile;
    private static List<String> mAPKList;
    private static String mTitleText = null;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        MaterialButton mSortButton = findViewById(R.id.sort);
        mSelect = findViewById(R.id.select);
        mProgressLayout = findViewById(R.id.progress);
        mRecyclerView = findViewById(R.id.recycler_view);

        mAPKList = new ArrayList<>();

        String path = getIntent().getStringExtra(PATH_INTENT);
        mTitleText = getIntent().getStringExtra(TITLE_INTENT);

        if (path != null) {
            mFile = new File(path);
        }
        if (mTitleText != null) {
            mTitle.setText(mTitleText);
        } else {
            mTitle.setText(getString(R.string.sdcard));
        }

        mBack.setOnClickListener(v -> exit());

        if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,this)) {
            LinearLayoutCompat mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            mPermissionLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mPermissionGrant.setOnClickListener(v -> sPermissionUtils.requestPermission(
                    new String[] {
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },this));
            return;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new FilePickerAdapter(APKExplorer.getData(mFile, false, this), mAPKList, this);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((filePath, position) -> {
            if (new File(filePath).isDirectory()) {
                reload(new File(filePath), this);
            } else if (filePath.endsWith(".apk")) {
                if (mAPKList.contains(filePath)) {
                    mAPKList.remove(filePath);
                } else {
                    mAPKList.add(filePath);
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                mSelect.setVisibility(mAPKList.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                sCommonUtils.snackBar(findViewById(android.R.id.content), getString(R.string.wrong_extension, ".apk")).show();
            }
        });

        mSelect.setOnClickListener(v -> {
            if (APKData.findPackageName(mAPKList,  this) != null) {
                if (mAPKList.size() > 1) {
                    APKExplorer.handleAPKs(true, mAPKList, this);
                } else {
                    Intent intent = new Intent(this, APKInstallerActivity.class);
                    intent.putExtra("apkFilePath", mAPKList.get(0));
                    activityResultLauncher.launch(intent);
                }
            } else {
                sCommonUtils.snackBar(findViewById(android.R.id.content), getString(R.string.installation_status_bad_apks)).show();
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true).setIcon(R.drawable.ic_sort_az)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, this), this);
                    reload(mFile, this);
                }
                return false;
            });
            popupMenu.setForceShowIcon(true);
            popupMenu.show();
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                exit();
            }
        });
    }

    private void reload(File file, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new FilePickerAdapter(APKExplorer.getData(file, false, activity), mAPKList, activity);
            }

            @Override
            public void onPostExecute() {
                mFile = file;
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                if (mTitleText != null) {
                    mTitle.setText(mTitleText);
                } else {
                    mTitle.setText(Objects.equals(mFile, Environment.getExternalStorageDirectory()) ? getString(R.string.sdcard) : file.getName());
                }
                if (mAPKList.isEmpty()) {
                    mSelect.setVisibility(View.GONE);
                } else {
                    mSelect.setVisibility(View.VISIBLE);
                }
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && Build.VERSION.SDK_INT < 30 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.recreate();
        }
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    APKExplorer.setSuccessIntent(true, this);
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    finish();
                }
            }
    );

    private void exit() {
        if (Objects.equals(mFile, new File(getExternalCacheDir(), "splits"))) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(getString(R.string.installation_cancel_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> APKExplorer.setCancelIntent(this)).show();
        } else if (Objects.equals(mFile, Environment.getExternalStorageDirectory())) {
            finish();
        } else {
            mAPKList.clear();
            reload(mFile.getParentFile(), this);
        }
    }

}