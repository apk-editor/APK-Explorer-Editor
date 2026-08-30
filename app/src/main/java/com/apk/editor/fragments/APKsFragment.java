package com.apk.editor.fragments;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
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
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKInstallerActivity;
import com.apk.editor.adapters.APKsAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.menu.ExploreOptionsMenu;
import com.apk.editor.viewModels.FragmentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsFragment extends BaseFragment {

    private APKsAdapter mRecycleViewAdapter;
    private int mTabPosition = 0;
    private ContentLoadingProgressBar mProgress;
    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialButton mBatchButton;
    private String mSearchText = null;
    private final List<String> mAPKPaths = new CopyOnWriteArrayList<>();

    public APKsFragment() {
    }

    public static APKsFragment newInstance(int position) {
        APKsFragment fragment = new APKsFragment();

        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTabPosition = getArguments().getInt("tabPosition");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apks, container, false);

        mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatchButton = mRootView.findViewById(R.id.batch_options);
        MaterialButton mAddButton = mRootView.findViewById(R.id.add_button);
        mProgress = mRootView.findViewById(R.id.progress);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        FragmentViewModel viewModel = new ViewModelProvider(requireActivity()).get(FragmentViewModel.class);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecycleViewAdapter = new APKsAdapter(new CopyOnWriteArrayList<>(), mAPKPaths, mBatchButton, requireActivity());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mBatchButton.setOnClickListener(v -> new sExecutor() {
                    private ProgressDialog mProgressDialog;

                    @SuppressLint("StringFormatInvalid")
                    @Override
                    public void onPreExecute() {
                        mProgressDialog = new ProgressDialog(requireActivity());
                        mProgressDialog.setTitle(getString(R.string.deleting, getString(R.string.apks)));
                        mProgressDialog.setIcon(R.mipmap.ic_launcher);
                        mProgressDialog.setIndeterminate(true);
                        mProgressDialog.show();
                    }

                    @Override
                    public void doInBackground() {
                        for (String apkPaths : mAPKPaths) {
                            sFileUtils.delete(new File(apkPaths));
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        try {
                            mProgressDialog.dismiss();
                        } catch (IllegalArgumentException ignored) {
                        }
                        mAPKPaths.clear();
                        mBatchButton.setVisibility(GONE);
                        loadAPKs(mSearchText);
                    }
                }.execute()
        );

        viewModel.getSearchTrigger().observe(getViewLifecycleOwner(), isVisible -> {
            if (isVisible) {
                mSearchWord.setVisibility(View.VISIBLE);
                if (!mSearchWord.hasFocus()) {
                    mSearchWord.requestFocus();
                    AppData.toggleKeyboard(1, mSearchWord, requireActivity());
                }
            } else {
                mSearchWord.setVisibility(GONE);
                AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            }
        });

        viewModel.getSortTrigger().observe(getViewLifecycleOwner(), timestamp -> loadAPKs(mSearchText));

        loadAPKs(mSearchText);

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadAPKs(s.toString().trim().toLowerCase());
            }
        });

        mAddButton.setOnClickListener(v -> launchInstallerFilePicker());

        AppSettings.applyMargin(mRecyclerView, requireActivity());

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
                    mSearchWord.setVisibility(View.GONE);
                    return;
                }
                if (mBatchButton.getVisibility() == View.VISIBLE) {
                    mAPKPaths.clear();
                    mBatchButton.setVisibility(GONE);
                    loadAPKs(mSearchText);
                    return;
                }

                AppSettings.navigateToFragment(requireActivity(), 1);
            }
        };

        return mRootView;
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
        Intent installer = filePickerIntent();
        installerFilePicker.launch(installer);
    }

    @NonNull
    private static Intent filePickerIntent() {
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
        return installer;
    }

    private void loadAPKs(String searchWord) {
        new sExecutor() {

            private List<File> data;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                data = APKData.getData(mSearchText, mTabPosition, requireActivity());
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

    private sExecutor handleMultipleAPKs(ClipData uriFiles, Activity activity) {
        return new sExecutor() {
            private final File mParentDir = new File(activity.getExternalCacheDir(), "APKs");
            private final List<String> mAPKList = new ArrayList<>();

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                if (mParentDir.exists()) {
                    sFileUtils.delete(mParentDir);
                }
                sFileUtils.mkdir(mParentDir);
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
                            mAPKList.add(mFile.getAbsolutePath());
                        }
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            @Override
            public void onPostExecute() {
                APKExplorer.handleAPKs(false, mAPKList, activity);
                mProgress.setVisibility(View.GONE);
            }
        };
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    APKExplorer.setSuccessIntent(false, requireActivity());
                    loadAPKs(mSearchText);
                }
            }
    );

    private final ActivityResultLauncher<Intent> explorerFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    if (data.getData() != null) {
                        ExploreOptionsMenu.getMenu(null, null, data.getData(), false, requireActivity());
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> installerFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    if (data.getClipData() != null) {
                        handleMultipleAPKs(data.getClipData(), requireActivity()).execute();
                    } else if (data.getData() != null) {
                        Intent intent = new Intent(requireActivity(), APKInstallerActivity.class);
                        intent.putExtra("apkFileUri", data.getData().toString());
                        activityResultLauncher.launch(intent);
                    }
                }
            }
    );

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSearchText != null) {
            mSearchWord.setText(null);
            mSearchWord.setVisibility(GONE);
        }

        mAPKPaths.clear();
    }
    
}