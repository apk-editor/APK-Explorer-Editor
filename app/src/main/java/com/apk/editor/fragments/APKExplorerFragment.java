package com.apk.editor.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.adapters.APKExplorerAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.DexToSmali;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.tasks.DeleteFile;
import com.apk.editor.utils.tasks.DeleteProject;
import com.apk.editor.utils.tasks.ExportToStorage;
import com.apk.editor.utils.tasks.SignAPK;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class APKExplorerFragment extends androidx.fragment.app.Fragment {

    private APKExplorerAdapter mRecycleViewAdapter;
    private ContentLoadingProgressBar mProgressLayout;
    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private static File mFile, mRootFile = null;
    private static List<File> mFiles = new ArrayList<>();
    private static List<String> mData;
    private static String mBackupFilePath = null, mSearchText = null, mPackageName = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) return;

        mBackupFilePath = arguments.getString("backupFilePath");
        mPackageName = arguments.getString("packageName");
    }

    @SuppressLint({"SetTextI18n", "StringFormatInvalid"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apkexplorer, container, false);

        AppCompatImageButton mBack = mRootView.findViewById(R.id.back);
        AppCompatImageButton mBuild = mRootView.findViewById(R.id.build);
        MaterialButton mMenuButton = mRootView.findViewById(R.id.menu);
        mTitle = mRootView.findViewById(R.id.title);
        MaterialTextView mError = mRootView.findViewById(R.id.error_status);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        mProgressLayout = mRootView.findViewById(R.id.progress);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        String mAppName = APKExplorer.getAppName(mBackupFilePath);
        mFile = new File(mBackupFilePath.replace("/.aeeBackup/appData", ""));
        mRootFile = new File(mBackupFilePath.replace("/.aeeBackup/appData", ""));
        mTitle.setText(getString(R.string.root));

        mBack.setOnClickListener(v -> retainDialog());

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    loadUI(mFile);
                } else {
                    loadUI(s.toString().toLowerCase());
                }
            }
        });

        mBuild.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(R.string.save_apk_message)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.build), (dialog, id) -> {
                    if (!sCommonUtils.getBoolean("firstSigning", false, requireActivity())) {
                        new sSingleItemDialog(0, null, new String[] {
                                getString(R.string.signing_default),
                                getString(R.string.signing_custom)
                        }, requireActivity()) {

                            @Override
                            public void onItemSelected(int itemPosition) {
                                sCommonUtils.saveBoolean("firstSigning", true, requireActivity());
                                if (itemPosition == 0) {
                                    new SignAPK(mRootFile, requireActivity()).execute();
                                } else {
                                    Intent signing = new Intent(requireActivity(), APKSignActivity.class);
                                    startActivity(signing);
                                }
                            }
                        }.show();
                    } else {
                        new SignAPK(mRootFile, requireActivity()).execute();
                    }
                }).show());

        if (APKEditorUtils.isFullVersion(requireActivity())) {
            mBuild.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), APKExplorer.getSpanCount(requireActivity())));

        try {
            mRecycleViewAdapter = new APKExplorerAdapter(APKExplorer.getData(mFile, true, requireActivity()), activityResultLauncher, mFiles, mPackageName, mBackupFilePath, requireActivity());
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(View.GONE);
            mError.setText(getString(R.string.explore_error_status, mAppName));
            mError.setVisibility(View.VISIBLE);
        }

        mMenuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mMenuButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setIcon(R.drawable.ic_sort_az).setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, requireActivity()));
            if (mSearchWord.getVisibility() == View.GONE && Objects.requireNonNull(mFile.getParentFile()).getName().equals(requireActivity().getCacheDir().getName())) {
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.search_files)).setIcon(R.drawable.ic_search_folder);
            }
            if (mFiles != null && !mFiles.isEmpty()) {
                menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.export_selected_files)).setIcon(R.drawable.ic_export_file);
                if (APKEditorUtils.isFullVersion(requireActivity())) {
                    menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.delete_selected_files)).setIcon(R.drawable.ic_delete_file);
                }
            }
            if (APKEditorUtils.isFullVersion(requireActivity()) && !Objects.requireNonNull(mFile.getParentFile()).getName().equals(requireActivity().getCacheDir().getName())) {
                menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.delete_folder)).setIcon(R.drawable.ic_delete_folder);
            }
            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                        if (mSearchText != null) {
                            loadUI(mSearchText);
                        } else {
                            loadUI(mFile);
                        }
                        break;
                    case 1:
                        mSearchWord.setVisibility(View.VISIBLE);
                        mSearchWord.requestFocus();
                        loadUI(mSearchText);
                        break;
                    case 2:
                        if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
                            sPermissionUtils.requestPermission(
                                    new String[] {
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, requireActivity());
                        } else {
                            new ExportToStorage(null, mFiles, mRootFile.getName(), requireActivity()).execute();
                        }
                        break;
                    case 3:
                        new DeleteFile(null, mFiles, mBackupFilePath, requireActivity()) {

                            @Override
                            public void onPostExecute() {
                                if (mSearchText != null) {
                                    loadUI(mSearchText);
                                } else {
                                    loadUI(mFile);
                                }
                            }
                        }.execute();
                        break;
                    case 4:
                        new DeleteFile(mFile, null, mBackupFilePath, requireActivity()) {

                            @Override
                            public void onPostExecute() {
                                mFiles = new ArrayList<>();
                                loadUI(mFile.getParentFile());
                            }
                        }.execute();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        mRecycleViewAdapter.setOnItemClickListener(filePath -> {
            if (new File(filePath).isFile() && filePath.endsWith(".dex")) {
                decompileDexToSmali(new File(filePath)).execute();
            } else {
                mFiles = new ArrayList<>();
                loadUI(new File(filePath));
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    if (mSearchText != null) {
                        mSearchText = null;
                        mSearchWord.setText(null);
                    }
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                if (Objects.equals(mFile.getParentFile(), requireActivity().getCacheDir())) {
                    retainDialog();
                } else {
                    mFiles = new ArrayList<>();
                    loadUI(mFile.getParentFile());
                }
            }
        });

        return mRootView;
    }

    private sExecutor decompileDexToSmali(File inputFile) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;
            private File mBackUpPath, mExplorePath;
            private String mDexName = null;

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(requireActivity());
                mProgressDialog.setTitle(getString(R.string.decompiling, inputFile.getName()));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
                mExplorePath = inputFile.getParentFile();
                mBackUpPath = new File(mExplorePath,".aeeBackup");
                mDexName = inputFile.getName();
            }

            @Override
            public void doInBackground() {
                sFileUtils.mkdir(mBackUpPath);
                sFileUtils.copy(inputFile, new File(mBackUpPath, inputFile.getName()));
                sFileUtils.delete(inputFile);
                sFileUtils.mkdir(new File(mExplorePath, mDexName));
                new DexToSmali(false, new File(mBackUpPath, inputFile.getName()), new File(mExplorePath, mDexName), 0, mDexName).execute();
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                mFiles = new ArrayList<>();
                loadUI(new File(mExplorePath, mDexName));
            }
        };
    }

    private void retainDialog() {
        if (sCommonUtils.getString("projectAction", null, requireActivity()) == null) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.save_projects_question)
                    .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setNegativeButton(getString(R.string.discard), (dialog, id) -> new DeleteProject(new File(requireActivity().getCacheDir(), mRootFile.getName()), requireActivity(), true).execute())
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> requireActivity().finish()).show();
        } else if (sCommonUtils.getString("projectAction", null, requireActivity()).equals(getString(R.string.delete))) {
            new DeleteProject(new File(requireActivity().getCacheDir(), mRootFile.getName()), requireActivity(), true).execute();
        } else {
            requireActivity().finish();
        }
    }

    private void loadUI(File file) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.VISIBLE);
                mData = new CopyOnWriteArrayList<>();
            }

            @Override
            public void doInBackground() {
                mData = APKExplorer.getData(file, true, requireActivity());
                mRecycleViewAdapter = new APKExplorerAdapter(mData, activityResultLauncher, mFiles, mPackageName, mBackupFilePath, requireActivity());
            }

            @Override
            public void onPostExecute() {
                mFile = file;
                mTitle.setText(Objects.equals(mFile.getParentFile(), requireActivity().getCacheDir()) ? getString(R.string.root) : file.getName());
                if (!Objects.requireNonNull(file.getParentFile()).getName().equals(requireActivity().getCacheDir().getName())) {
                    mSearchWord.setVisibility(View.GONE);
                }
                requireActivity().findViewById((R.id.info_button)).setVisibility(Objects.requireNonNull(file.getParentFile()).getName().equals(requireActivity().getCacheDir().getName()) ? View.VISIBLE : View.GONE);
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private void loadUI(String searchText) {
        new sExecutor() {
            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mData = new CopyOnWriteArrayList<>();
            }

            @Override
            public void doInBackground() {
                getData(mRootFile);
                Collections.sort(mData, String.CASE_INSENSITIVE_ORDER);
                if (!sCommonUtils.getBoolean("az_order", true, requireActivity())) {
                    Collections.reverse(mData);
                }
                mRecycleViewAdapter = new APKExplorerAdapter(mData, null, mFiles, mPackageName, mBackupFilePath, requireActivity());
            }

            private void getData(File path) {
                for (File mFile : Objects.requireNonNull(path.listFiles())) {
                    if (mFile.isFile()) {
                        if (searchText == null) {
                            mData.add(mFile.getAbsolutePath());
                        } else if (mFile.getName().contains(searchText)) {
                            mData.add(mFile.getAbsolutePath());
                        }
                    } else if (mFile.isDirectory() && !mFile.getName().matches(".aeeBackup|.aeeBuild")) {
                        getData(mFile);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (searchText == null) {
                    AppData.toggleKeyboard(1, mSearchWord, requireActivity());
                } else {
                    mSearchText = searchText;
                }
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uriFile = data.getData();

                    if (uriFile != null) {
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(getString(R.string.replace_file_question, new File(Common.getFileToReplace()).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.replace, (dialog, id) -> {
                                    sFileUtils.copy(uriFile, new File(Common.getFileToReplace()), requireActivity());
                                    if (Common.getFileToReplace().endsWith(".smali")) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(sFileUtils.read(new File(mBackupFilePath)));
                                            jsonObject.put("smali_edited", true);
                                            sFileUtils.create(jsonObject.toString(), new File(mBackupFilePath));
                                        } catch (JSONException ignored) {
                                        }
                                    }
                                    loadUI(mFile);
                                }).show();
                    } else {
                        APKExplorer.setSuccessIntent(true, requireActivity());
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    requireActivity().finish();
                }
            }
    );
    
}