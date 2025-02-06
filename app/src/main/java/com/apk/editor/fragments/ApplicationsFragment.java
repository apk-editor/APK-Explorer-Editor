package com.apk.editor.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ApplicationsAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SerializableItems.PackageItems;
import com.apk.editor.utils.dialogs.BatchSigningOptionsDialog;
import com.apk.editor.utils.dialogs.ExportOptionsDialog;
import com.apk.editor.utils.tasks.ExportApp;
import com.apk.editor.utils.tasks.ResignBatchAPKs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsFragment extends Fragment {

    private ApplicationsAdapter mRecycleViewAdapter;
    private boolean mExit = false, mLongClicked = false, mSelectAll = false;
    private final Handler mHandler = new Handler();
    private final List<String> mPackageNames = new ArrayList<>();
    private ContentLoadingProgressBar mProgress;
    private MaterialButton mBatchButton, mMenuButton;
    private RecyclerView mRecyclerView;
    private String mSearchText = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_applications, container, false);

        MaterialAutoCompleteTextView mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatchButton = mRootView.findViewById(R.id.batch_options);
        mProgress = mRootView.findViewById(R.id.progress);
        MaterialButton mSearchButton = mRootView.findViewById(R.id.search_button);
        mMenuButton = mRootView.findViewById(R.id.menu_button);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.all)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.system)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.user)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition())).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sCommonUtils.getString("appTypes", "all", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("all")) {
                            sCommonUtils.saveString("appTypes", "all", requireActivity());
                            loadApps(mSearchText);
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("system")) {
                            sCommonUtils.saveString("appTypes", "system", requireActivity());
                            loadApps(mSearchText);
                        }
                        break;
                    case 2:
                        if (!mStatus.equals("user")) {
                            sCommonUtils.saveString("appTypes", "user", requireActivity());
                            loadApps(mSearchText);
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

        mBatchButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.select_all)).setIcon(R.drawable.ic_select_all)
                    .setCheckable(true).setChecked(mSelectAll);
            menu.add(Menu.NONE, 1, Menu.NONE, getExportOptionsTitle()).setIcon(R.drawable.ic_export_file);
            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        mSelectAll = !mSelectAll;
                        loadApps(mSearchText);
                        break;
                    case 1:
                        if (sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity()) && sCommonUtils.getString("exportAPKsPath", "externalFiles",
                                requireActivity()).equals("internalStorage")) {
                            sPermissionUtils.requestPermission(
                                    new String[] {
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, requireActivity());
                        } else {
                            if (APKEditorUtils.isFullVersion(requireActivity())) {
                                if (sCommonUtils.getString("exportAPKs", null, requireActivity()) == null) {
                                    new ExportOptionsDialog(mPackageNames, requireActivity());
                                } else if (sCommonUtils.getString("exportAPKs", null, requireActivity()).equals(getString(R.string.export_storage))) {
                                    new ExportApp(mPackageNames, requireActivity()).execute();
                                } else {
                                    if (!sCommonUtils.getBoolean("firstSigning", false, requireActivity())) {
                                        new BatchSigningOptionsDialog(mPackageNames, requireActivity()).show();
                                    } else {
                                        new ResignBatchAPKs(mPackageNames, requireActivity()).execute();
                                    }
                                }
                            } else {
                                new ExportApp(mPackageNames, requireActivity()).execute();
                            }
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        mSearchButton.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
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

        mMenuButton.setOnClickListener(v -> sortMenu());

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadApps(s.toString().trim().toLowerCase());
            }
        });

        loadApps(mSearchText);

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
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
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                if (mBatchButton.getVisibility() == View.VISIBLE) {
                    mPackageNames.clear();
                    loadApps(mSearchText);
                    return;
                }
                if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    sCommonUtils.toast(getString(R.string.press_back), requireActivity()).show();
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    private int getTabPosition() {
        String mStatus = sCommonUtils.getString("appTypes", "all", requireActivity());
        if (mStatus.equals("user")) {
            return 2;
        } else if (mStatus.equals("system")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void loadApps(String searchWord) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                Common.setProgress(true, mProgress);
                mRecyclerView.removeAllViews();
                mLongClicked = !mPackageNames.isEmpty();
            }

            @Override
            public void doInBackground() {
                List<PackageItems> mData = AppData.getData(searchWord, requireActivity());
                mRecycleViewAdapter = new ApplicationsAdapter(mData, mPackageNames, searchWord, mLongClicked, requireActivity());
                if (mSelectAll) {
                    mPackageNames.clear();
                    for (PackageItems items : mData) {
                        mPackageNames.add(items.getPackageName());
                    }
                }
            }

            @Override
            public void onPostExecute() {
                mSearchText = searchWord;
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);
                mBatchButton.setVisibility(!mPackageNames.isEmpty() ? View.VISIBLE : View.GONE);
                Common.setProgress(false, mProgress);
            }
        }.execute();
    }

    private void sortMenu() {
        PopupMenu popupMenu = new PopupMenu(requireActivity(), mMenuButton);
        Menu menu = popupMenu.getMenu();
        SubMenu sort = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_by)).setIcon(R.drawable.ic_sort);

        sort.add(0, 1, Menu.NONE, getString(R.string.sort_by_name)).setCheckable(true)
                .setChecked(sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 0);
        sort.add(0, 2, Menu.NONE, getString(R.string.sort_by_id)).setCheckable(true)
                .setChecked(sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sort.add(0, 3, Menu.NONE, getString(R.string.sort_by_installed)).setCheckable(true)
                    .setChecked(sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 2);
            sort.add(0, 4, Menu.NONE, getString(R.string.sort_by_updated)).setCheckable(true)
                    .setChecked(sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 3);
            sort.add(0, 5, Menu.NONE, getString(R.string.sort_by_size)).setCheckable(true)
                    .setChecked(sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 4);
        }
        menu.add(Menu.NONE, 6, Menu.NONE, getSortTitle()).setIcon(getSortIcon()).setCheckable(true).setChecked(sCommonUtils.getBoolean("az_order", true, requireActivity()));
        sort.setGroupCheckable(0, true, true);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) != 0) {
                        sCommonUtils.saveInt("sort_apps", 0, requireActivity());
                        loadApps(mSearchText);
                    }
                    break;
                case 2:
                    if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) != 1) {
                        sCommonUtils.saveInt("sort_apps", 1, requireActivity());
                        loadApps(mSearchText);
                    }
                    break;
                case 3:
                    if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) != 2) {
                        sCommonUtils.saveInt("sort_apps", 2, requireActivity());
                        loadApps(mSearchText);
                    }
                    break;
                case 4:
                    if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) != 3) {
                        sCommonUtils.saveInt("sort_apps", 3, requireActivity());
                        loadApps(mSearchText);
                    }
                    break;
                case 5:
                    if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) != 4) {
                        sCommonUtils.saveInt("sort_apps", 4, requireActivity());
                        loadApps(mSearchText);
                    }
                    break;
                case 6:
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                    loadApps(mSearchText);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private int getSortIcon() {
        if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 4) {
            return R.drawable.ic_sort_size;
        } else if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 2 ||
                sCommonUtils.getInt("sort_apps", 0, requireActivity()) == 3) {
            return R.drawable.ic_sort_time;
        } else {
            return R.drawable.ic_sort_az;
        }
    }

    private String getExportOptionsTitle() {
        if (APKEditorUtils.isFullVersion(requireActivity())) {
            if (sCommonUtils.getString("exportAPKs", null, requireActivity()) == null) {
                return getString(R.string.export_options_title);
            } else if (sCommonUtils.getString("exportAPKs", null, requireActivity()).equals(getString(R.string.export_storage))) {
                return getString(R.string.export_storage);
            } else {
                return getString(R.string.export_resign);
            }
        } else {
            return getString(R.string.export_storage);
        }
    }

    private String getSortTitle() {
        if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 4) {
            return getString(R.string.sort_size);
        } else if (sCommonUtils.getInt("sort_apps", 1, requireActivity()) == 2 ||
                sCommonUtils.getInt("sort_apps", 0, requireActivity()) == 3) {
            return getString(R.string.sort_time);
        } else {
            return getString(R.string.sort_order);
        }
    }
    
}