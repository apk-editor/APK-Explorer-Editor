package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.RecycleViewAPKExplorerAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends AppCompatActivity {

    private List<String> mData = new ArrayList<>();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewAPKExplorerAdapter mRecycleViewAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        mTitle = findViewById(R.id.title);
        MaterialTextView mError = findViewById(R.id.error_status);
        mRecyclerView = findViewById(R.id.recycler_view);

        mTitle.setText(AppData.getAppName(APKExplorer.mAppID, this));

        mBack.setOnClickListener(v -> {
            APKEditorUtils.delete(getCacheDir().getPath() + "/" + APKExplorer.mAppID);
            super.onBackPressed();
        });

        mSave.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.save_apk_message)
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> {
                        APKData.prepareSignedAPK(this);
                    })
                    .show();
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));

        try {
            mRecycleViewAdapter = new RecycleViewAPKExplorerAdapter(getData());
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(View.GONE);
            mError.setText(getString(R.string.explore_error_status, AppData.getAppName(APKExplorer.mAppID, this)));
            mError.setVisibility(View.VISIBLE);
        }

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(mData.get(position)).isDirectory()) {
                APKExplorer.mPath = mData.get(position);
                reload(this);
            } else if (APKExplorer.isImageFile(mData.get(position))) {
                Intent imageView = new Intent(this, ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PATH_INTENT, mData.get(position));
                startActivity(imageView);
            } else if (APKExplorer.isTextFile(mData.get(position))) {
                Intent textView = new Intent(this, TextViewActivity.class);
                textView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                startActivity(textView);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.unknown_file_message, new File(mData.get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(getString(R.string.open_as_text), (dialog, id) -> {
                            Intent textView = new Intent(this, TextViewActivity.class);
                            textView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                            startActivity(textView);
                        }).show();
            }
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
                if (mFile.isFile()) {
                    mData.add(mFile.getAbsolutePath());
                }
            }
        } catch (NullPointerException ignored) {
            finish();
        }
        return mData;
    }

    private File[] getFilesList() {
        if (APKExplorer.mPath == null) {
            APKExplorer.mPath = getCacheDir().getPath() + "/" + APKExplorer.mAppID;
        }
        if (!APKExplorer.mPath.endsWith(File.separator)) {
            APKExplorer.mPath = APKExplorer.mPath + File.separator;
        }
        return new File(APKExplorer.mPath).listFiles();
    }

    @SuppressLint("StaticFieldLeak")
    private void reload(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mRecycleViewAdapter = new RecycleViewAPKExplorerAdapter(getData());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mTitle.setText(APKExplorer.mPath.equals(getCacheDir().getPath() + "/" + APKExplorer.mAppID + File.separator) ? AppData.getAppName(APKExplorer.mAppID, activity)
                        : new File(APKExplorer.mPath).getName());
                mRecyclerView.setAdapter(mRecycleViewAdapter);
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        if (APKExplorer.mPath.equals(getCacheDir().getPath() + "/" + APKExplorer.mAppID + File.separator)) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.retain_question)
                    .setNegativeButton(getString(R.string.delete), (dialog, id) -> {
                        APKEditorUtils.delete(getCacheDir().getPath() + "/" + APKExplorer.mAppID);
                        finish();
                    })
                    .setPositiveButton(getString(R.string.retain), (dialog, id) -> {
                        finish();
                    }).show();
        } else {
            APKExplorer.mPath = Objects.requireNonNull(new File(APKExplorer.mPath).getParentFile()).getPath();
            reload(this);
        }
    }

}