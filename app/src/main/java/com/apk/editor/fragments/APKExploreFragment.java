package com.apk.editor.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.activities.TextViewActivity;
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
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class APKExploreFragment extends androidx.fragment.app.Fragment {

    private List<String> mData = new ArrayList<>();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewAPKExplorerAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apkexplorer, container, false);

        AppCompatImageButton mBack = mRootView.findViewById(R.id.back);
        AppCompatImageButton mSave = mRootView.findViewById(R.id.save);
        mTitle = mRootView.findViewById(R.id.title);
        MaterialTextView mError = mRootView.findViewById(R.id.error_status);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mTitle.setText(APKExplorer.mAppID != null ? AppData.getAppName(APKExplorer.mAppID, requireActivity()) : new File(APKExplorer.mPath).getName());

        mBack.setOnClickListener(v -> retainDialog());

        mSave.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setMessage(R.string.save_apk_message)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.save), (dialog, id) -> APKData.prepareSignedAPK(requireActivity()))
                .show());

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), APKExplorer.getSpanCount(requireActivity())));

        try {
            mRecycleViewAdapter = new RecycleViewAPKExplorerAdapter(getData());
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(View.GONE);
            mError.setText(getString(R.string.explore_error_status, AppData.getAppName(APKExplorer.mAppID, requireActivity())));
            mError.setVisibility(View.VISIBLE);
        }

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(mData.get(position)).isDirectory()) {
                APKExplorer.mPath = mData.get(position);
                reload(requireActivity());
            } else if (APKExplorer.isImageFile(mData.get(position))) {
                Intent imageView = new Intent(requireActivity(), ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PATH_INTENT, mData.get(position));
                startActivity(imageView);
            } else if (APKExplorer.isTextFile(mData.get(position))) {
                Intent textView = new Intent(requireActivity(), TextViewActivity.class);
                textView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                startActivity(textView);
            } else {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setMessage(getString(R.string.unknown_file_message, new File(mData.get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setPositiveButton(getString(R.string.open_as_text), (dialog, id) -> {
                            Intent textView = new Intent(requireActivity(), TextViewActivity.class);
                            textView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                            startActivity(textView);
                        }).show();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Objects.requireNonNull(new File(APKExplorer.mPath).getParentFile()).getPath().equals(requireActivity().getCacheDir().getPath())) {
                    retainDialog();
                } else {
                    APKExplorer.mPath = Objects.requireNonNull(new File(APKExplorer.mPath).getParentFile()).getPath();
                    reload(requireActivity());
                }
            }
        });

        return mRootView;
    }

    private void retainDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setMessage(R.string.save_projects_question)
                .setNeutralButton(getString(R.string.delete), (dialog, id) -> {
                    APKEditorUtils.delete(requireActivity().getCacheDir().getPath() + "/" + (APKExplorer.mAppID != null ?
                            APKExplorer.mAppID : new File(APKExplorer.mPath).getName()));
                    requireActivity().finish();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.save), (dialog, id) -> {
                    requireActivity().finish();
                }).show();
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
            requireActivity().finish();
        }
        return mData;
    }

    private File[] getFilesList() {
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
                mTitle.setText(APKExplorer.mPath.equals(requireActivity().getCacheDir().getPath() + "/" + (APKExplorer.mAppID != null ?
                        APKExplorer.mAppID : new File(APKExplorer.mPath).getName()) + File.separator) ? AppData.getAppName(APKExplorer.mAppID, activity)
                        : new File(APKExplorer.mPath).getName());
                mRecyclerView.setAdapter(mRecycleViewAdapter);
            }
        }.execute();
    }
    
}