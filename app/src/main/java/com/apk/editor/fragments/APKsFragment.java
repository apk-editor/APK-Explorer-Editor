package com.apk.editor.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import com.apk.editor.utils.AsyncTasks;
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

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsFragment extends Fragment {

    private AppCompatEditText mSearchWord;
    private boolean mExit;
    private final Handler mHandler = new Handler();
    private LinearLayout mProgress;
    private MaterialTextView mAppTitle;
    private RecyclerView mRecyclerView;
    private APKsAdapter mRecycleViewAdapter;

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
        MaterialTextView mSelectText = mRootView.findViewById(R.id.select_text);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mSelectText.setText(getString(APKEditorUtils.isFullVersion(requireActivity()) ? R.string.select_storage : R.string.install_storage));

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
                    APKEditorUtils.saveBoolean("az_order", !APKEditorUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
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
                Common.setSearchWord(s.toString().toLowerCase());
                loadAPKs(requireActivity());
            }
        });

        mAddButton.setOnClickListener(v -> launchInstallerFilePicker());
        mInstall.setOnClickListener(v -> launchInstallerFilePicker());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.GONE) {
                    if (Common.getSearchWord() != null) {
                        mSearchWord.setText(null);
                        Common.setSearchWord(null);
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

    private void launchAEEInstaller() {
        if (Build.VERSION.SDK_INT >= 29) {
            Intent installer = new Intent(Intent.ACTION_GET_CONTENT);
            installer.setType("*/*");
            installer.addCategory(Intent.CATEGORY_OPENABLE);
            installer.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(installer, 0);
        } else {
            Common.getAPKList().clear();
            Common.setPath(Environment.getExternalStorageDirectory().toString());
            Intent installer = new Intent(requireActivity(), InstallerFilePickerActivity.class);
            startActivity(installer);
        }
    }

    private void loadAPKs(Activity activity) {
        new AsyncTasks() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
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
                mProgress.setVisibility(View.GONE);
            }
        }.execute();
    }

    private AsyncTasks handleSingleInstallationEvent(Uri uriFile, Activity activity) {
        return new AsyncTasks() {
            private File mFile = null;
            private String mExtension = null;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                APKEditorUtils.delete(activity.getExternalFilesDir("APK").getAbsolutePath());
                Common.getAPKList().clear();
            }

            @Override
            public void doInBackground() {
                mExtension = ExternalAPKData.getExtension(uriFile, requireActivity());
                mFile = new File(activity.getExternalFilesDir("APK"), "APK." + mExtension);
                try (FileOutputStream outputStream = new FileOutputStream(mFile, false)) {
                    InputStream inputStream = activity.getContentResolver().openInputStream(uriFile);
                    int read;
                    byte[] bytes = new byte[8192];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                } catch (IOException ignored) {
                }
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
                mProgress.setVisibility(View.GONE);
            }
        };
    }

    private AsyncTasks handleMultipleAPKs(ClipData uriFiles, Activity activity) {
        return new AsyncTasks() {

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                APKEditorUtils.delete(activity.getExternalFilesDir("APK").getAbsolutePath());
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
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onPostExecute() {
                APKExplorer.handleAPKs(activity);
                mProgress.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uriFile = data.getData();

            if (data.getClipData() != null) {
                handleMultipleAPKs(data.getClipData(), requireActivity()).execute();
            } else if (uriFile != null) {
                handleSingleInstallationEvent(uriFile, requireActivity()).execute();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Common.isReloading()) {
            Common.isReloading(false);
            loadAPKs(requireActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Common.getSearchWord() != null) {
            mSearchWord.setText(null);
            Common.setSearchWord(null);
        }
    }
    
}