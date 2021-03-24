package com.apk.editor.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.InstallerFilePickerActivity;
import com.apk.editor.adapters.RecycleViewApksAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsFragment extends Fragment {

    private AppCompatEditText mSearchWord;
    private AsyncTask<Void, Void, List<String>> mLoader;
    private boolean mExit;
    private Handler mHandler = new Handler();
    private LinearLayout mProgress;
    private MaterialTextView mAppTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewApksAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apks, container, false);

        mAppTitle = mRootView.findViewById(R.id.app_title);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress_layout);
        AppCompatImageButton mSearchButton = mRootView.findViewById(R.id.search_button);
        AppCompatImageButton mSortButton = mRootView.findViewById(R.id.sort_button);
        AppCompatImageButton mAddButton = mRootView.findViewById(R.id.add_button);
        MaterialCardView mInstall = mRootView.findViewById(R.id.add);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAppTitle.setText(getString(R.string.apps_exported));
        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.apks)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.bundles)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = APKEditorUtils.getString("apkTypes", "apks", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("apks")) {
                            APKEditorUtils.saveString("apkTypes", "apks", requireActivity());
                            loadAPKs(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("bundles")) {
                            APKEditorUtils.saveString("apkTypes", "bundles", requireActivity());
                            loadAPKs(requireActivity());
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mSearchButton.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                mAppTitle.setVisibility(View.VISIBLE);
                AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mSearchWord.requestFocus();
                mAppTitle.setVisibility(View.GONE);
                AppData.toggleKeyboard(1, mSearchWord, requireActivity());
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(APKEditorUtils.getBoolean("az_order", true, requireActivity()));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    if (APKEditorUtils.getBoolean("az_order", true, requireActivity())) {
                        APKEditorUtils.saveBoolean("az_order", false, requireActivity());
                    } else {
                        APKEditorUtils.saveBoolean("az_order", true, requireActivity());
                    }
                    loadAPKs(requireActivity());
                }
                return false;
            });
            popupMenu.show();
        });

        loadAPKs(requireActivity());

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                APKData.mSearchText = s.toString().toLowerCase();
                loadAPKs(requireActivity());
            }
        });

        mAddButton.setOnClickListener(v -> launchInstallerFilePicker());
        mInstall.setOnClickListener(v -> launchInstallerFilePicker());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (APKData.mSearchText != null) {
                    mSearchWord.setText(null);
                    APKData.mSearchText = null;
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    mSearchWord.setVisibility(View.GONE);
                    mAppTitle.setVisibility(View.VISIBLE);
                    return;
                }
                if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    APKEditorUtils.snackbar(requireActivity().findViewById(android.R.id.content), getString(R.string.press_back));
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = APKEditorUtils.getString("apkTypes", "apks", activity);
        if (mStatus.equals("bundles")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void launchInstallerFilePicker() {
        if (!APKEditorUtils.isWritePermissionGranted(requireActivity())) {
            ActivityCompat.requestPermissions(requireActivity(), new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            APKEditorUtils.snackbar(requireActivity().findViewById(android.R.id.content), requireActivity().getString(R.string.permission_denied_message));
        } else {
            if (!APKEditorUtils.getBoolean("firstInstall", false, requireActivity())) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.split_apk_installer)
                        .setMessage(getString(R.string.installer_message))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                            APKEditorUtils.saveBoolean("firstInstall", true, requireActivity());
                            launchAEEInstaller();
                        }).show();
            } else {
                launchAEEInstaller();
            }
        }
    }

    private void launchAEEInstaller() {
        APKExplorer.mAPKList.clear();
        APKExplorer.mPath = Environment.getExternalStorageDirectory().toString();
        Intent installer = new Intent(requireActivity(), InstallerFilePickerActivity.class);
        startActivity(installer);
    }

    private void loadAPKs(Activity activity) {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mRecyclerView.setVisibility(View.GONE);
                            mProgress.setVisibility(View.VISIBLE);
                            mRecyclerView.removeAllViews();
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewApksAdapter(APKData.getData(activity));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mProgress.setVisibility(View.GONE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (APKData.mSearchText != null) {
            mSearchWord.setText(null);
            APKData.mSearchText = null;
        }
    }
    
}