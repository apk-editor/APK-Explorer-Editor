package com.apk.editor.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ProjectsAdapter;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Projects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 06, 2021
 */
public class ProjectsFragment extends Fragment {

    private LinearLayoutCompat mProgress;
    private RecyclerView mRecyclerView;
    private ProjectsAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        Common.initializeProjectsTitle(mRootView, R.id.app_title);
        Common.initializeProjectsSearchWord(mRootView, R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress_layout);
        AppCompatImageButton mSearchButton = mRootView.findViewById(R.id.search_button);
        AppCompatImageButton mSortButton = mRootView.findViewById(R.id.sort_button);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        Common.getProjectsTitle().setText(getString(R.string.projects));

        mSearchButton.setOnClickListener(v -> {
            if (Common.getProjectsSearchWord().getVisibility() == View.VISIBLE) {
                Common.getProjectsSearchWord().setVisibility(View.GONE);
                Common.getProjectsTitle().setVisibility(View.VISIBLE);
                if (Common.getProjectsSearchWord() != null) {
                    Common.getProjectsSearchWord().setText(null);
                }
                AppData.toggleKeyboard(0, Common.getProjectsSearchWord(), requireActivity());
            } else {
                Common.getProjectsSearchWord().setVisibility(View.VISIBLE);
                Common.getProjectsSearchWord().requestFocus();
                Common.getProjectsTitle().setVisibility(View.GONE);
                AppData.toggleKeyboard(1, Common.getProjectsSearchWord(), requireActivity());
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(sUtils.getBoolean("az_order", true, requireActivity()));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sUtils.saveBoolean("az_order", !sUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                    loadProjects(requireActivity());
                }
                return false;
            });
            popupMenu.show();
        });

        loadProjects(requireActivity());

        Common.getProjectsSearchWord().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Common.setSearchWord(s.toString().toLowerCase());
                loadProjects(requireActivity());
            }
        });

        return mRootView;
    }

    private void loadProjects(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                Common.setProgress(true, mProgress);
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new ProjectsAdapter(Projects.getData(activity));
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);
                Common.setProgress(false, mProgress);
            }
        }.execute();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Common.isReloading()) {
            Common.isReloading(false);
            loadProjects(requireActivity());
        }
    }
    
}