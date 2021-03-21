package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.RecycleViewInstallerFilePickerAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 21, 2021
 */
public class InstallerFilePickerActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private List<String> mData = new ArrayList<>();
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
        APKExplorer.mSelect = findViewById(R.id.select);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewInstallerFilePickerAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        if (getIntent().getStringExtra(TITLE_INTENT) != null) {
            mTitle.setText(getIntent().getStringExtra(TITLE_INTENT));
        } else {
            mTitle.setText(APKExplorer.mPath.equals("/storage/emulated/0/") ? getString(R.string.sdcard) : new File(APKExplorer.mPath).getName());
        }

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(mData.get(position)).isDirectory()) {
                APKExplorer.mPath = mData.get(position);
                reload();
            } else if (mData.get(position).endsWith(".apks") || mData.get(position).endsWith(".apkm") || mData.get(position).endsWith(".xapk")) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.bundle_install_question, new File(mData.get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.install), (dialogInterface, i) -> {
                            SplitAPKInstaller.handleAppBundle(mData.get(position), this);
                            finish();
                        }).show();
            } else if (mData.get(position).endsWith(".apk")) {
                if (APKExplorer.mAPKList.contains(mData.get(position))) {
                    APKExplorer.mAPKList.remove(mData.get(position));
                } else {
                    APKExplorer.mAPKList.add(mData.get(position));
                }
                mRecycleViewAdapter.notifyItemChanged(position);
                APKExplorer.mSelect.setVisibility(APKExplorer.mAPKList.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                APKEditorUtils.snackbar(mRecyclerView, getString(R.string.wrong_extension, ".apks/.apkm/.xapk"));
            }
        });

        APKExplorer.mSelect.setOnClickListener(v -> {
            if (APKExplorer.mAPKList.size() > 1) {
                SplitAPKInstaller.installSplitAPKs(null, this);
            } else {
                SplitAPKInstaller.installAPK(new File(APKExplorer.mAPKList.get(0)), this);
            }
            finish();
        });

        mBack.setOnClickListener(v -> {
            super.onBackPressed();
        });
    }

    private List<String> getData() {
        try {
            mData.clear();
            // Add directories
            for (File mFile : getFilesList()) {
                if (mFile.isDirectory()) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
            // Add files
            for (File mFile : getFilesList()) {
                if (mFile.isFile() && isSupportedFile(mFile.getAbsolutePath())) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        } catch (NullPointerException ignored) {
            finish();
        }
        return mData;
    }

    private boolean isSupportedFile(String path) {
        return path.endsWith(".apk") || path.endsWith(".apks") || path.endsWith(".apkm") || path.endsWith(".xapk");
    }

    private File[] getFilesList() {
        if (!APKExplorer.mPath.endsWith(File.separator)) {
            APKExplorer.mPath = APKExplorer.mPath + File.separator;
        }
        return new File(APKExplorer.mPath).listFiles();
    }

    private void reload() {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mData.clear();
                            mRecyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewInstallerFilePickerAdapter(getData());
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
            reload();
        }
    }

}