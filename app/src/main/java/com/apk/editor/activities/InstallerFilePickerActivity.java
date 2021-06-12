package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.RecycleViewInstallerFilePickerAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 21, 2021
 */
public class InstallerFilePickerActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, List<String>> mLoader;
    private final Handler mHandler = new Handler();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewInstallerFilePickerAdapter mRecycleViewAdapter;
    public static final String TITLE_INTENT = "title";

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installerfilepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        AppCompatImageButton mSortButton = findViewById(R.id.sort);
        Common.initializeView(findViewById(android.R.id.content), R.id.select);
        mRecyclerView = findViewById(R.id.recycler_view);

        mBack.setOnClickListener(v -> super.onBackPressed());

        if (APKExplorer.isPermissionDenied(this)) {
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = findViewById(R.id.permission_text);
            mPermissionText.setText(Build.VERSION.SDK_INT >= 30 ? getString(R.string.file_permission_request_message,
                    getString(R.string.app_name)) : getString(R.string.permission_denied_message));
            mPermissionLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mPermissionGrant.setOnClickListener(v -> {
                APKExplorer.requestPermission(this);
                if (Build.VERSION.SDK_INT < 30) finish();
            });
            return;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewInstallerFilePickerAdapter(APKExplorer.getData(getFilesList(), false, this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        if (getIntent().getStringExtra(TITLE_INTENT) != null) {
            mTitle.setText(getIntent().getStringExtra(TITLE_INTENT));
        } else {
            mTitle.setText(Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator) ? getString(R.string.sdcard) : new File(Common.getPath()).getName());
        }

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(APKExplorer.getData(getFilesList(), false, this).get(position)).isDirectory()) {
                Common.setPath(APKExplorer.getData(getFilesList(), false, this).get(position));
                reload(this);
            } else if (APKExplorer.getData(getFilesList(), false, this).get(position).endsWith(".apks") || APKExplorer.getData(getFilesList(), false,
                    this).get(position).endsWith(".apkm") || APKExplorer.getData(getFilesList(), false, this).get(position).endsWith(".xapk")) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.bundle_install_question, new File(APKExplorer.getData(getFilesList(), false, this).get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                            SplitAPKInstaller.handleAppBundle(APKExplorer.getData(getFilesList(), false, this).get(position), this);
                            finish();
                        }).show();
            } else if (APKExplorer.getData(getFilesList(), false, this).get(position).endsWith(".apk")) {
                if (Common.getAPKList().contains(APKExplorer.getData(getFilesList(), false, this).get(position))) {
                    Common.getAPKList().remove(APKExplorer.getData(getFilesList(), false, this).get(position));
                } else {
                    Common.getAPKList().add(APKExplorer.getData(getFilesList(), false, this).get(position));
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                Common.getSelectCard().setVisibility(Common.getAPKList().isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                APKEditorUtils.snackbar(findViewById(android.R.id.content), getString(R.string.wrong_extension, ".apks/.apkm/.xapk"));
            }
        });

        Common.getSelectCard().setOnClickListener(v -> handleAPKs(this));

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(APKEditorUtils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    APKEditorUtils.saveBoolean("az_order", !APKEditorUtils.getBoolean("az_order", true, this), this);
                    reload(this);
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private void installAPKs() {
        if (APKData.findPackageName(this) != null) {
            if (Common.getAPKList().size() > 1) {
                SplitAPKInstaller.installSplitAPKs(Common.getAPKList(), null, this);
            } else {
                SplitAPKInstaller.installAPK(new File(Common.getAPKList().get(0)), this);
            }
            finish();
        } else {
            APKEditorUtils.snackbar(findViewById(android.R.id.content), getString(R.string.installation_status_bad_apks));
        }
    }

    private void handleAPKs(Activity activity) {
        if (APKEditorUtils.isFullVersion(activity)) {
            if (APKEditorUtils.getString("installerAction", null, activity) == null) {
                new MaterialAlertDialogBuilder(activity).setItems(getResources().getStringArray(
                        R.array.install_options), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            installAPKs();
                            break;
                        case 1:
                            if (!APKEditorUtils.getBoolean("firstSigning", false, activity)) {
                                new MaterialAlertDialogBuilder(activity).setItems(activity.getResources().getStringArray(
                                        R.array.signing), (dialogInterfacei, ii) -> {
                                    APKEditorUtils.saveBoolean("firstSigning", true, activity);
                                    switch (ii) {
                                        case 0:
                                            APKData.reSignAndInstall(activity);
                                            break;
                                        case 1:
                                            Intent signing = new Intent(activity, APKSignActivity.class);
                                            startActivity(signing);
                                            break;
                                    }
                                }).setCancelable(false)
                                        .setOnDismissListener(dialogInterfacei -> {
                                        }).show();
                            } else {
                                APKData.reSignAndInstall(activity);
                            }
                            break;
                        case 2:
                            if (!APKEditorUtils.getBoolean("firstSigning", false, activity)) {
                                new MaterialAlertDialogBuilder(activity).setItems(activity.getResources().getStringArray(
                                        R.array.signing), (dialogInterfacei, ii) -> {
                                    APKEditorUtils.saveBoolean("firstSigning", true, activity);
                                    switch (ii) {
                                        case 0:
                                            APKData.reSignAPKs(activity);
                                            break;
                                        case 1:
                                            Intent signing = new Intent(activity, APKSignActivity.class);
                                            startActivity(signing);
                                            break;
                                    }
                                }).setCancelable(false)
                                        .setOnDismissListener(dialogInterfacei -> {
                                        }).show();
                            } else {
                                APKData.reSignAPKs(activity);
                            }
                            break;
                    }
                }).setOnDismissListener(dialogInterface -> {
                }).show();
            } else if (APKEditorUtils.getString("installerAction", null, activity).equals(getString(R.string.install))) {
                installAPKs();
            } else {
                if (!APKEditorUtils.getBoolean("firstSigning", false, activity)) {
                    new MaterialAlertDialogBuilder(activity).setItems(activity.getResources().getStringArray(
                            R.array.signing), (dialogInterface, i) -> {
                        APKEditorUtils.saveBoolean("firstSigning", true, activity);
                        switch (i) {
                            case 0:
                                APKData.reSignAndInstall(activity);
                                break;
                            case 1:
                                Intent signing = new Intent(activity, APKSignActivity.class);
                                startActivity(signing);
                                break;
                        }
                    }).setCancelable(false)
                            .setOnDismissListener(dialogInterface -> {
                            }).show();
                } else {
                    APKData.reSignAndInstall(activity);
                }
            }
        } else {
            installAPKs();
        }
    }

    private File[] getFilesList() {
        if (!Common.getPath().endsWith(File.separator)) {
            Common.setPath(Common.getPath() + File.separator);
        }
        return new File(Common.getPath()).listFiles();
    }

    private void reload(Activity activity) {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            APKExplorer.getData(getFilesList(), false, activity).clear();
                            mRecyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewInstallerFilePickerAdapter(APKExplorer.getData(getFilesList(), false, activity));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            if (getIntent().getStringExtra(TITLE_INTENT) != null) {
                                mTitle.setText(getIntent().getStringExtra(TITLE_INTENT));
                            } else {
                                mTitle.setText(Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator) ? getString(R.string.sdcard)
                                        : new File(Common.getPath()).getName());
                            }
                            if (Common.getAPKList().isEmpty()) {
                                Common.getSelectCard().setVisibility(View.GONE);
                            } else {
                                Common.getSelectCard().setVisibility(View.VISIBLE);
                            }
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Common.isFinished()) {
            Common.setFinishStatus(false);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (Common.getPath().equals(getCacheDir().getPath() + "/splits/")) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.installation_cancel_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        finish();
                    }).show();
        } else if (Common.getPath().equals(Environment.getExternalStorageDirectory().toString() + File.separator)) {
            super.onBackPressed();
        } else {
            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
            Common.getAPKList().clear();
            reload(this);
        }
    }

}