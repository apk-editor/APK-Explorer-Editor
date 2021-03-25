package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;

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
import com.apk.editor.utils.SplitAPKInstaller;
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
    private Handler mHandler = new Handler();
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
        APKExplorer.mSelect = findViewById(R.id.select);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewInstallerFilePickerAdapter(APKExplorer.getData(getFilesList(), this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        if (getIntent().getStringExtra(TITLE_INTENT) != null) {
            mTitle.setText(getIntent().getStringExtra(TITLE_INTENT));
        } else {
            mTitle.setText(APKExplorer.mPath.equals("/storage/emulated/0/") ? getString(R.string.sdcard) : new File(APKExplorer.mPath).getName());
        }

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(APKExplorer.getData(getFilesList(), this).get(position)).isDirectory()) {
                APKExplorer.mPath = APKExplorer.getData(getFilesList(), this).get(position);
                reload(this);
            } else if (APKExplorer.getData(getFilesList(), this).get(position).endsWith(".apks") || APKExplorer.getData(getFilesList(),
                    this).get(position).endsWith(".apkm") || APKExplorer.getData(getFilesList(), this).get(position).endsWith(".xapk")) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.bundle_install_question, new File(APKExplorer.getData(getFilesList(), this).get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                            SplitAPKInstaller.handleAppBundle(APKExplorer.getData(getFilesList(), this).get(position), this);
                            finish();
                        }).show();
            } else if (APKExplorer.getData(getFilesList(), this).get(position).endsWith(".apk")) {
                if (APKExplorer.mAPKList.contains(APKExplorer.getData(getFilesList(), this).get(position))) {
                    APKExplorer.mAPKList.remove(APKExplorer.getData(getFilesList(), this).get(position));
                } else {
                    APKExplorer.mAPKList.add(APKExplorer.getData(getFilesList(), this).get(position));
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                APKExplorer.mSelect.setVisibility(APKExplorer.mAPKList.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                APKEditorUtils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".apks/.apkm/.xapk"));
            }
        });

        APKExplorer.mSelect.setOnClickListener(v -> handleAPKs(this));

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(APKEditorUtils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    if (APKEditorUtils.getBoolean("az_order", true, this)) {
                        APKEditorUtils.saveBoolean("az_order", false, this);
                    } else {
                        APKEditorUtils.saveBoolean("az_order", true, this);
                    }
                    reload(this);
                }
                return false;
            });
            popupMenu.show();
        });

        mBack.setOnClickListener(v -> {
            super.onBackPressed();
        });
    }

    private void installAPKs() {
        if (APKData.findPackageName(this) != null) {
            if (APKExplorer.mAPKList.size() > 1) {
                SplitAPKInstaller.installSplitAPKs(APKExplorer.mAPKList, null, this);
            } else {
                SplitAPKInstaller.installAPK(new File(APKExplorer.mAPKList.get(0)), this);
            }
            finish();
        } else {
            APKEditorUtils.snackbar(mRecyclerView, getString(R.string.installation_status_bad_apks));
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
        if (!APKExplorer.mPath.endsWith(File.separator)) {
            APKExplorer.mPath = APKExplorer.mPath + File.separator;
        }
        return new File(APKExplorer.mPath).listFiles();
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
                            APKExplorer.getData(getFilesList(), activity).clear();
                            mRecyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewInstallerFilePickerAdapter(APKExplorer.getData(getFilesList(), activity));
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
                                mTitle.setText(APKExplorer.mPath.equals("/storage/emulated/0/") ? getString(R.string.sdcard)
                                        : new File(APKExplorer.mPath).getName());
                            }
                            if (APKExplorer.mAPKList.isEmpty()) {
                                APKExplorer.mSelect.setVisibility(View.GONE);
                            } else {
                                APKExplorer.mSelect.setVisibility(View.VISIBLE);
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

        if (APKExplorer.mFinish) {
            APKExplorer.mFinish = false;
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (APKExplorer.mPath.equals(getCacheDir().getPath() + "/splits/")) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.installation_cancel_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        finish();
                    }).show();
        } else if (APKExplorer.mPath.equals("/storage/emulated/0/")) {
            super.onBackPressed();
        } else {
            APKExplorer.mPath = Objects.requireNonNull(new File(APKExplorer.mPath).getParentFile()).getPath();
            APKExplorer.mAPKList.clear();
            reload(this);
        }
    }

}