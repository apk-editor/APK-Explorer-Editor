package com.apk.editor.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKInstallerActivity;
import com.apk.editor.activities.InstallerFilePickerActivity;
import com.apk.editor.adapters.APKsAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.CommonViews;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsFragment extends Fragment {

    private AppCompatAutoCompleteTextView mSearchWord;
    private LinearLayoutCompat mProgress;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private APKsAdapter mRecycleViewAdapter;
    private String mSearchText = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apks, container, false);

        mTitle = mRootView.findViewById(R.id.app_title);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress_layout);
        AppCompatImageButton mSearchButton = mRootView.findViewById(R.id.search_button);
        AppCompatImageButton mSortButton = mRootView.findViewById(R.id.sort_button);
        AppCompatImageButton mAddButton = mRootView.findViewById(R.id.add_button);
        LinearLayoutCompat mBottomLayout = mRootView.findViewById(R.id.layout_bottom);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mTitle.setText(getString(R.string.apps_exported));
        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.apks)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.bundles)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sCommonUtils.getString("apkTypes", "apks", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("apks")) {
                            sCommonUtils.saveString("apkTypes", "apks", requireActivity());
                            loadAPKs(mSearchText, requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("bundles")) {
                            sCommonUtils.saveString("apkTypes", "bundles", requireActivity());
                            loadAPKs(mSearchText, requireActivity());
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
                mTitle.setVisibility(View.VISIBLE);
                if (mSearchWord != null) {
                    mSearchWord.setText(null);
                }
                AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mSearchWord.requestFocus();
                mTitle.setVisibility(View.GONE);
                AppData.toggleKeyboard(1, mSearchWord, requireActivity());
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, requireActivity()));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                    loadAPKs(mSearchText, requireActivity());
                }
                return false;
            });
            popupMenu.show();
        });

        loadAPKs(mSearchText, requireActivity());

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSearchText = s.toString().toLowerCase();
                loadAPKs(mSearchText, requireActivity());
            }
        });

        mAddButton.setOnClickListener(v -> launchInstallerFilePicker());
        mBottomLayout.setOnClickListener(v -> launchInstallerFilePicker());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Common.isBusy()) return;
                if (mSearchText != null) {
                    if (mSearchWord != null && mSearchWord.getVisibility() == View.VISIBLE) {
                        mSearchWord.setVisibility(View.GONE);
                        mTitle.setVisibility(View.VISIBLE);
                        mSearchWord.setText(null);
                    }
                    mSearchText = null;
                    return;
                }
                CommonViews.navigateToFragment(requireActivity(), R.id.nav_projects);
            }
        });

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = sCommonUtils.getString("apkTypes", "apks", activity);
        if (mStatus.equals("bundles")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void launchInstallerFilePicker() {
        if (APKEditorUtils.isFullVersion(requireActivity())) {
            if (!sCommonUtils.getBoolean("firstInstall", false, requireActivity())) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.split_apk_installer)
                        .setMessage(getString(R.string.installer_message))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                            sCommonUtils.saveBoolean("firstInstall", true, requireActivity());
                            launchAEEInstaller();
                        }).show();
            } else {
                launchAEEInstaller();
            }
        } else {
            Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
            installer.setType("application/vnd.android.package-archive");
            installer.addCategory(Intent.CATEGORY_OPENABLE);
            installer.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            explorerFilePicker.launch(installer);
        }
    }

    private void launchAEEInstaller() {
        if (Build.VERSION.SDK_INT >= 29) {
            Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
            installer.setType("*/*");
            String[] mimeTypes = {
                    "application/vnd.android.package-archive",
                    "application/xapk-package-archive",
                    "application/octet-stream",
                    "application/vnd.apkm"
            };
            installer.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            installer.addCategory(Intent.CATEGORY_OPENABLE);
            installer.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            installerFilePicker.launch(installer);
        } else {
            Common.getAPKList().clear();
            Common.setPath(Environment.getExternalStorageDirectory().toString());
            Intent installer = new Intent(requireActivity(), InstallerFilePickerActivity.class);
            startActivity(installer);
        }
    }

    private void loadAPKs(String searchWord, Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                Common.setProgress(true, mProgress);
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new APKsAdapter(APKData.getData(searchWord, activity), searchWord);
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);
                Common.setProgress(false, mProgress);
            }
        }.execute();
    }

    private sExecutor handleMultipleAPKs(ClipData uriFiles, Activity activity) {
        return new sExecutor() {
            private final File mParentDir = new File(activity.getExternalCacheDir(), "APKs");

            @Override
            public void onPreExecute() {
                Common.setProgress(true, mProgress);
                if (mParentDir.exists()) {
                    sFileUtils.delete(mParentDir);
                }
                sFileUtils.mkdir(mParentDir);
                Common.getAPKList().clear();
            }

            @Override
            public void doInBackground() {
                for (int i = 0; i < uriFiles.getItemCount(); i++) {
                    String fileName = Objects.requireNonNull(DocumentFile.fromSingleUri(activity, uriFiles.getItemAt(i).getUri())).getName();
                    File mFile = new File(mParentDir, Objects.requireNonNull(fileName));
                    try (FileOutputStream outputStream = new FileOutputStream(mFile, false)) {
                        InputStream inputStream = activity.getContentResolver().openInputStream(uriFiles.getItemAt(i).getUri());
                        int read;
                        byte[] bytes = new byte[8192];
                        while ((read = Objects.requireNonNull(inputStream).read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                        // In this case, we don't really care about app bundles!
                        if (mFile.getName().endsWith(".apk")) {
                            Common.getAPKList().add(mFile.getAbsolutePath());
                        }
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onPostExecute() {
                APKExplorer.handleAPKs(false, activity);
                Common.setProgress(false, mProgress);
            }
        };
    }

    ActivityResultLauncher<Intent> installerFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    if (data.getClipData() != null) {
                        handleMultipleAPKs(data.getClipData(), requireActivity()).execute();
                    } else if (data.getData() != null) {
                        Intent intent = new Intent(requireActivity(), APKInstallerActivity.class);
                        intent.putExtra("apkFileUri", data.getData().toString());
                        startActivity(intent);
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> explorerFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    if (data.getData() != null) {
                        APKExplorer.exploreApps(null, null, data.getData(), false, requireActivity());
                    }
                }
            }
    );

    @Override
    public void onResume() {
        super.onResume();

        if (Common.isReloading()) {
            Common.isReloading(false);
            loadAPKs(mSearchText, requireActivity());
        }
    }
    
}