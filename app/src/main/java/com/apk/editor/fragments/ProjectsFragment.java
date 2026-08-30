package com.apk.editor.fragments;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.adapters.ProjectsAdapter;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Projects;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 06, 2021
 */
public class ProjectsFragment extends BaseFragment {

    private ContentLoadingProgressBar mProgress;
    private MaterialButton mBatchButton;
    private ProjectsAdapter mRecycleViewAdapter;
    private String mSearchText = null;
    private final List<String> mProjectNames = new CopyOnWriteArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_projects, container, false);

        MaterialAutoCompleteTextView mSearchWord = mRootView.findViewById(R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress);
        mBatchButton = mRootView.findViewById(R.id.batch_options);
        MaterialButton mSearchButton = mRootView.findViewById(R.id.search_button);
        MaterialButton mSortButton = mRootView.findViewById(R.id.sort_button);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecycleViewAdapter = new ProjectsAdapter(new CopyOnWriteArrayList<>(), mProjectNames, mBatchButton, backupPath -> {
            Intent explorer = new Intent(requireActivity(), APKExploreActivity.class);
            if (backupPath != null) {
                explorer.putExtra(APKExploreActivity.BACKUP_PATH_INTENT, backupPath);
            }
            activityResultLauncher.launch(explorer);
        }, requireActivity());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mSearchButton.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(GONE);
                if (mSearchText != null) {
                    mSearchText = null;
                    mSearchWord.setText(null);
                }
                AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mSearchWord.requestFocus();
                AppData.toggleKeyboard(1, mSearchWord, requireActivity());
            }
        });

        mBatchButton.setOnClickListener(v -> new sExecutor() {
                    private ProgressDialog mProgressDialog;

                    @SuppressLint("StringFormatInvalid")
                    @Override
                    public void onPreExecute() {
                        mProgressDialog = new ProgressDialog(requireActivity());
                        mProgressDialog.setTitle(getString(R.string.deleting, getString(R.string.projects)));
                        mProgressDialog.setIcon(R.mipmap.ic_launcher);
                        mProgressDialog.setIndeterminate(true);
                        mProgressDialog.show();
                    }

                    @Override
                    public void doInBackground() {
                        for (String projectPath : mProjectNames) {
                            sFileUtils.delete(new File(projectPath));
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        try {
                            mProgressDialog.dismiss();
                        } catch (IllegalArgumentException ignored) {
                        }
                        mProjectNames.clear();
                        mBatchButton.setVisibility(GONE);
                        loadProjects(mSearchText);
                    }
                }.execute()
        );

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true).setIcon(R.drawable.ic_sort_az).setChecked(
                    sCommonUtils.getBoolean("az_order", true, requireActivity()));
            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                    loadProjects(mSearchText);
                }
                return false;
            });
            popupMenu.show();
        });

        loadProjects(mSearchText);

        AppSettings.applyMargin(mRecyclerView, requireActivity());

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadProjects(s.toString().trim().toLowerCase());
            }
        });

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    if (mSearchText != null) {
                        mSearchText = null;
                        mSearchWord.setText(null);
                    }
                    mSearchWord.setVisibility(GONE);
                    return;
                }
                if (mBatchButton.getVisibility() == View.VISIBLE) {
                    mProjectNames.clear();
                    mBatchButton.setVisibility(GONE);
                    loadProjects(mSearchText);
                    return;
                }

                AppSettings.navigateToFragment(requireActivity(), 0);
            }
        };

        return mRootView;
    }

    private void loadProjects(String searchWord) {
        new sExecutor() {

            private List<String> data;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                data = Projects.getData(searchWord, requireActivity());
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }

                mProgress.setVisibility(View.GONE);
                mSearchText = searchWord;
                mRecycleViewAdapter.updateData(data);
            }
        }.execute();
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    loadProjects(mSearchText);
                }
            }
    );
    
}