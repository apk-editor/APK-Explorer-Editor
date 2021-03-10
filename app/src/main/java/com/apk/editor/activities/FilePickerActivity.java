package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.RecycleViewFilePickerAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class FilePickerActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private List<String> mData = new ArrayList<>();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewFilePickerAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewFilePickerAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(APKExplorer.mFilePath.equals("/storage/emulated/0/") ? getString(R.string.sdcard) : new File(APKExplorer.mFilePath).getName());

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(mData.get(position)).isDirectory()) {
                APKExplorer.mFilePath = mData.get(position);
                reload();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.replace_question, new File(APKExplorer.mFileToReplace).getName()) + " " +
                                new File(mData.get(position)).getName() + "?")
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.replace, (dialog, id) -> {
                            APKEditorUtils.copy(mData.get(position), APKExplorer.mFileToReplace);
                            APKExplorer.mFileToReplace = null;
                            finish();
                        }).show();
            }
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
        if (APKExplorer.mFilePath == null) {
            APKExplorer.mFilePath = Environment.getExternalStorageDirectory().toString();;
        }
        if (!APKExplorer.mFilePath.endsWith(File.separator)) {
            APKExplorer.mFilePath = APKExplorer.mFilePath + File.separator;
        }
        return new File(APKExplorer.mFilePath).listFiles();
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
                            mRecycleViewAdapter = new RecycleViewFilePickerAdapter(getData());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mTitle.setText(APKExplorer.mFilePath.equals("/storage/emulated/0/") ? getString(R.string.sdcard)
                                    : new File(APKExplorer.mFilePath).getName());
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
        if (APKExplorer.mFilePath.equals("/storage/emulated/0/")) {
            super.onBackPressed();
        } else {
            APKExplorer.mFilePath = Objects.requireNonNull(new File(APKExplorer.mFilePath).getParentFile()).getPath();
            reload();
        }
    }

}