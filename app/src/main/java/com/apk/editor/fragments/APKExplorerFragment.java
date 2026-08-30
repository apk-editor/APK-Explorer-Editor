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
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.APKExplorerAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.DexToSmali;
import com.apk.editor.utils.Serializables.MenuItems;
import com.apk.editor.utils.dialogs.BottomMenuDialog;
import com.apk.editor.utils.dialogs.FileActionDialog;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.tasks.DeleteFiles;
import com.apk.editor.utils.tasks.ExportToStorage;
import com.google.android.material.button.MaterialButton;
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
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class APKExplorerFragment extends BaseFragment {

    private APKExplorerAdapter mRecycleViewAdapter;
    private ContentLoadingProgressBar mProgressLayout;
    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialTextView mTitle;
    private static File mFile = null, mRootFile = null;
    private static final List<File> mFiles = new ArrayList<>();
    private static String mBackupFilePath = null, mFileToReplace = null, mPackageName = null, mSearchText = null;

    public static APKExplorerFragment newInstance(String backupFilePath, String packageName) {
        APKExplorerFragment fragment = new APKExplorerFragment();

        Bundle args = new Bundle();
        args.putString("backupFilePath", backupFilePath);
        args.putString("packageName", packageName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mBackupFilePath = arguments.getString("backupFilePath");
            mPackageName = arguments.getString("packageName");
        }
    }

    @SuppressLint({"SetTextI18n", "StringFormatInvalid"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apkexplorer, container, false);

        AppCompatImageButton mBack = mRootView.findViewById(R.id.back);
        MaterialButton mMenuButton = mRootView.findViewById(R.id.menu);
        mTitle = mRootView.findViewById(R.id.title);
        mSearchWord = mRootView.findViewById(R.id.search_word);
        mProgressLayout = mRootView.findViewById(R.id.progress);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecycleViewAdapter = new APKExplorerAdapter(new CopyOnWriteArrayList<>(), mFiles, mPackageName, mBackupFilePath, clickListener(), requireActivity());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        if (mFile == null || !mBackupFilePath.contains(mRootFile.getName()) || !mFile.exists()) {
            mFile = new File(mBackupFilePath.replace("/.aeeBackup/appData", ""));
        }
        mRootFile = new File(mBackupFilePath.replace("/.aeeBackup/appData", ""));
        mTitle.setText(getString(R.string.root));

        mBack.setOnClickListener(v -> {
            if (Objects.equals(mFile.getParentFile(), requireActivity().getCacheDir())) {
                AppSettings.navigateToFragment(requireActivity(), 0);
            } else {
                mFiles.clear();
                loadUI(mFile.getParentFile());
            }
        });

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

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), APKExplorer.getSpanCount(requireActivity())));

        loadUI(mFile);

        mMenuButton.setOnClickListener(v -> {
            List<MenuItems> menuItem = new CopyOnWriteArrayList<>();
            menuItem.add(new MenuItems(R.drawable.ic_sort_az, getString(R.string.sort_order), true, sCommonUtils.getBoolean("az_order", true, requireActivity()), 0));
            if (mSearchWord.getVisibility() == View.GONE && Objects.requireNonNull(mFile.getParentFile()).getName().equals(requireActivity().getCacheDir().getName())) {
                menuItem.add(new MenuItems(R.drawable.ic_search_folder, getString(R.string.search_files), 1));
            }
            if (mFiles != null && !mFiles.isEmpty()) {
                menuItem.add(new MenuItems(R.drawable.ic_export_file, getString(R.string.export_selected_files), 2));
                if (APKEditorUtils.isFullVersion(requireActivity())) {
                    menuItem.add(new MenuItems(R.drawable.ic_delete_file, getString(R.string.delete_selected_files), 3));
                }
            }
            if (APKEditorUtils.isFullVersion(requireActivity()) && !Objects.requireNonNull(mFile.getParentFile()).getName().equals(requireActivity().getCacheDir().getName())) {
                menuItem.add(new MenuItems(R.drawable.ic_delete_folder, getString(R.string.delete_folder), 4));
            }
            new BottomMenuDialog(menuItem, APKExplorer.getAppIcon(mBackupFilePath), APKExplorer.getAppName(mBackupFilePath), requireContext()) {
                @Override
                public void onMenuItemClicked(int id) {
                    switch (id) {
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
                            new DeleteFiles(null, mFiles, mBackupFilePath, requireActivity()) {

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
                            new DeleteFiles(mFile, null, mBackupFilePath, requireActivity()) {

                                @Override
                                public void onPostExecute() {
                                    if (mFiles != null) {
                                        mFiles.clear();
                                    }
                                    loadUI(mFile.getParentFile());
                                }
                            }.execute();
                            break;
                    }
                }
            };
        });

        AppSettings.applyMargin(mRecyclerView, requireActivity());

        onBackPressedCallback = new OnBackPressedCallback(false) {
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
                    AppSettings.navigateToFragment(requireActivity(), 0);
                } else {
                    mFiles.clear();
                    loadUI(mFile.getParentFile());
                }
            }
        };

        return mRootView;
    }

    private APKExplorerAdapter.OnItemClickListener clickListener() {
        return (filePath, replace) -> {
            if (replace) {
                mFileToReplace = filePath;
                Intent replaceLauncher = new Intent(Intent.ACTION_GET_CONTENT);
                replaceLauncher.setType("*/*");
                activityResultLauncher.launch(replaceLauncher);
            } else {
                if (new File(filePath).isFile() && filePath.endsWith(".dex")) {
                    decompileDexToSmali(new File(filePath)).execute();
                } else {
                    mFiles.clear();
                    loadUI(new File(filePath));
                }
            }
        };
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
                mFiles.clear();
                loadUI(new File(mExplorePath, mDexName));
            }
        };
    }

    private void loadUI(File file) {
        new sExecutor() {
            private List<String> data;

            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                mFiles.clear();
            }

            @Override
            public void doInBackground() {
                data = APKExplorer.getData(file, true, requireActivity());
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }
                mProgressLayout.setVisibility(View.GONE);

                if (mFileToReplace != null) {
                    mFileToReplace = null;
                }

                mFile = file;
                String name = Objects.requireNonNull(file.getParentFile()).getName();
                mTitle.setText(Objects.equals(mFile.getParentFile(), requireActivity().getCacheDir()) ? getString(R.string.root) : file.getName());
                if (!name.equals(requireActivity().getCacheDir().getName())) {
                    mSearchWord.setVisibility(View.GONE);
                }
                mRecycleViewAdapter.updateData(data);
            }
        }.execute();
    }

    private void loadUI(String searchText) {
        new sExecutor() {
            private List<String> data;

            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                data = new CopyOnWriteArrayList<>();
            }

            @Override
            public void doInBackground() {
                getData(mRootFile);
                Collections.sort(data, String.CASE_INSENSITIVE_ORDER);
                if (!sCommonUtils.getBoolean("az_order", true, requireActivity())) {
                    Collections.reverse(data);
                }
            }

            private void getData(File path) {
                for (File mFile : Objects.requireNonNull(path.listFiles())) {
                    if (mFile.isFile()) {
                        if (searchText == null) {
                            data.add(mFile.getAbsolutePath());
                        } else if (mFile.getName().contains(searchText)) {
                            data.add(mFile.getAbsolutePath());
                        }
                    } else if (mFile.isDirectory() && !mFile.getName().matches(".aeeBackup|.aeeBuild")) {
                        getData(mFile);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }
                mProgressLayout.setVisibility(View.GONE);

                if (searchText == null) {
                    AppData.toggleKeyboard(1, mSearchWord, requireActivity());
                } else {
                    mSearchText = searchText;
                }
                mRecycleViewAdapter.updateData(data);
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
                        new FileActionDialog(APKExplorer.getImageDrawable(APKExplorer.getFileNameFromUri(uriFile, requireActivity()),requireActivity()), getString(R.string.replace_file_question, new File(mFileToReplace).getName()), requireActivity()) {
                            @Override
                            public void onPositiveAction() {
                                sFileUtils.copy(uriFile, new File(mFileToReplace), requireActivity());
                                if (mFileToReplace.endsWith(".smali")) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(sFileUtils.read(new File(mBackupFilePath)));
                                        jsonObject.put("smali_edited", true);
                                        sFileUtils.create(jsonObject.toString(), new File(mBackupFilePath));
                                    } catch (JSONException ignored) {
                                    }
                                }
                                loadUI(mFile);
                            }
                        };
                    } else {
                        APKExplorer.setSuccessIntent(true, requireActivity());
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    requireActivity().finish();
                }
            }
    );
    
}