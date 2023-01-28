package com.apk.editor.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.InstallerFilePickerActivity;
import com.apk.editor.adapters.APKsAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.ExternalAPKData;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsFragment extends Fragment {

    private LinearLayoutCompat mProgress;
    private RecyclerView mRecyclerView;
    private APKsAdapter mRecycleViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apks, container, false);

        Common.initializeAPKsTitle(mRootView, R.id.app_title);
        Common.initializeAPKsSearchWord(mRootView, R.id.search_word);
        mProgress = mRootView.findViewById(R.id.progress_layout);
        AppCompatImageButton mSearchButton = mRootView.findViewById(R.id.search_button);
        AppCompatImageButton mSortButton = mRootView.findViewById(R.id.sort_button);
        AppCompatImageButton mAddButton = mRootView.findViewById(R.id.add_button);
        MaterialCardView mInstall = mRootView.findViewById(R.id.add);
        MaterialTextView mSelectText = mRootView.findViewById(R.id.select_text);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mSelectText.setText(getString(APKEditorUtils.isFullVersion(requireActivity()) ? R.string.select_storage : R.string.install_storage));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        Common.getAPKsTitle().setText(getString(R.string.apps_exported));
        mTabLayout.setVisibility(View.VISIBLE);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.apks)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.bundles)));

        Objects.requireNonNull(mTabLayout.getTabAt(getTabPosition(requireActivity()))).select();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String mStatus = sUtils.getString("apkTypes", "apks", requireActivity());
                switch (tab.getPosition()) {
                    case 0:
                        if (!mStatus.equals("apks")) {
                            sUtils.saveString("apkTypes", "apks", requireActivity());
                            loadAPKs(requireActivity());
                        }
                        break;
                    case 1:
                        if (!mStatus.equals("bundles")) {
                            sUtils.saveString("apkTypes", "bundles", requireActivity());
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
            if (Common.getAPKsSearchWord().getVisibility() == View.VISIBLE) {
                Common.getAPKsSearchWord().setVisibility(View.GONE);
                Common.getAPKsTitle().setVisibility(View.VISIBLE);
                if (Common.getAPKsSearchWord() != null) {
                    Common.getAPKsSearchWord().setText(null);
                }
                AppData.toggleKeyboard(0, Common.getAPKsSearchWord(), requireActivity());
            } else {
                Common.getAPKsSearchWord().setVisibility(View.VISIBLE);
                Common.getAPKsSearchWord().requestFocus();
                Common.getAPKsTitle().setVisibility(View.GONE);
                AppData.toggleKeyboard(1, Common.getAPKsSearchWord(), requireActivity());
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
                    loadAPKs(requireActivity());
                }
                return false;
            });
            popupMenu.show();
        });

        loadAPKs(requireActivity());

        Common.getAPKsSearchWord().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Common.setSearchWord(s.toString().toLowerCase());
                loadAPKs(requireActivity());
            }
        });

        mAddButton.setOnClickListener(v -> launchInstallerFilePicker());
        mInstall.setOnClickListener(v -> launchInstallerFilePicker());

        return mRootView;
    }

    private int getTabPosition(Activity activity) {
        String mStatus = sUtils.getString("apkTypes", "apks", activity);
        if (mStatus.equals("bundles")) {
            return 1;
        } else {
            return 0;
        }
    }

    private void launchInstallerFilePicker() {
        if (!sUtils.getBoolean("firstInstall", false, requireActivity())) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.split_apk_installer)
                    .setMessage(getString(R.string.installer_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                        sUtils.saveBoolean("firstInstall", true, requireActivity());
                        launchAEEInstaller();
                    }).show();
        } else {
            launchAEEInstaller();
        }
    }

    private void launchAEEInstaller() {
        if (Build.VERSION.SDK_INT >= 29) {
            Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
            installer.setType("*/*");
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

    private void loadAPKs(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                Common.setProgress(true, mProgress);
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new APKsAdapter(APKData.getData(activity));
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);
                Common.setProgress(false, mProgress);
            }
        }.execute();
    }

    private sExecutor handleSingleInstallationEvent(Uri uriFile, Activity activity) {
        return new sExecutor() {
            private File mFile = null;
            private String mExtension = null;

            @Override
            public void onPreExecute() {
                Common.setProgress(true, mProgress);
                sUtils.delete(activity.getExternalFilesDir("APK"));
                Common.getAPKList().clear();
            }

            @Override
            public void doInBackground() {
                mExtension = ExternalAPKData.getExtension(uriFile, requireActivity());
                mFile = new File(activity.getExternalFilesDir("APK"), "tmp." + mExtension);
                sUtils.copy(uriFile, mFile, activity);
            }

            @Override
            public void onPostExecute() {
                if (mExtension.equals("apk")) {
                    Common.getAPKList().add(mFile.getAbsolutePath());
                    Common.setFinishStatus(true);
                    APKExplorer.handleAPKs(activity);
                } else if (mExtension.equals("apkm") || mExtension.equals("apks") || mExtension.equals("xapk")) {
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.split_apk_installer)
                            .setMessage(getString(R.string.install_bundle_question))
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .setPositiveButton(R.string.install, (dialogInterface, i) ->
                                    SplitAPKInstaller.handleAppBundle(mFile.getAbsolutePath(), activity)).show();
                } else {
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.split_apk_installer)
                            .setMessage(getString(R.string.wrong_extension, ".apks/.apkm/.xapk"))
                            .setCancelable(false)
                            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                            }).show();
                }
                Common.setProgress(false, mProgress);
            }
        };
    }

    private sExecutor handleMultipleAPKs(ClipData uriFiles, Activity activity) {
        return new sExecutor() {

            @Override
            public void onPreExecute() {
                Common.setProgress(true, mProgress);
                sUtils.delete(activity.getExternalFilesDir("APK"));
                Common.getAPKList().clear();
            }

            @Override
            public void doInBackground() {
                for (int i = 0; i < uriFiles.getItemCount(); i++) {
                    String mExtension = ExternalAPKData.getExtension(uriFiles.getItemAt(i).getUri(), requireActivity());
                    File mFile = new File(activity.getExternalFilesDir("APK"), "APK" + i + "." + mExtension);
                    try (FileOutputStream outputStream = new FileOutputStream(mFile, false)) {
                        InputStream inputStream = activity.getContentResolver().openInputStream(uriFiles.getItemAt(i).getUri());
                        int read;
                        byte[] bytes = new byte[8192];
                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                        // In this case, we don't really care about app bundles!
                        if (Objects.equals(mExtension, "apk")) {
                            Common.getAPKList().add(mFile.getAbsolutePath());
                        }
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onPostExecute() {
                APKExplorer.handleAPKs(activity);
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
                        handleSingleInstallationEvent(data.getData(), requireActivity()).execute();
                    }
                }
            }
    );

    @Override
    public void onResume() {
        super.onResume();

        if (Common.isReloading()) {
            Common.isReloading(false);
            loadAPKs(requireActivity());
        }
    }
    
}