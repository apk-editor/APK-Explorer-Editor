package com.apk.editor.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.activities.TextEditorActivity;
import com.apk.editor.activities.TextViewActivity;
import com.apk.editor.adapters.APKExplorerAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.tasks.DeleteFile;
import com.apk.editor.utils.tasks.DeleteProject;
import com.apk.editor.utils.tasks.ExportToStorage;
import com.apk.editor.utils.tasks.SignAPK;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class APKExplorerFragment extends androidx.fragment.app.Fragment {

    private MaterialTextView mTitle;
    private LinearLayoutCompat mProgressLayout;
    private RecyclerView mRecyclerView;
    private APKExplorerAdapter mRecycleViewAdapter;

    @SuppressLint("StringFormatInvalid")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_apkexplorer, container, false);

        AppCompatImageButton mBack = mRootView.findViewById(R.id.back);
        AppCompatImageButton mSave = mRootView.findViewById(R.id.save);
        AppCompatImageButton mSortButton = mRootView.findViewById(R.id.sort);
        mTitle = mRootView.findViewById(R.id.title);
        MaterialTextView mError = mRootView.findViewById(R.id.error_status);
        mProgressLayout = mRootView.findViewById(R.id.progress_layout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        String mAppName = APKExplorer.getAppName(Common.getPath() + "/.aeeBackup/appData");
        Common.setAppID(APKExplorer.getPackageName(Common.getPath() + "/.aeeBackup/appData"));
        mTitle.setText(getString(R.string.root));

        mBack.setOnClickListener(v -> retainDialog());

        mSave.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
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
                                    new SignAPK(requireActivity()).execute();
                                } else {
                                    Intent signing = new Intent(requireActivity(), APKSignActivity.class);
                                    startActivity(signing);
                                }
                            }
                        }.show();
                    } else {
                        new SignAPK(requireActivity()).execute();
                    }
                }).show());

        if (APKEditorUtils.isFullVersion(requireActivity())) {
            mSave.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), APKExplorer.getSpanCount(requireActivity())));

        try {
            mRecycleViewAdapter = new APKExplorerAdapter(APKExplorer.getData(getFilesList(), true, requireActivity()), replaceFile);
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(View.GONE);
            mError.setText(getString(R.string.explore_error_status, mAppName));
            mError.setVisibility(View.VISIBLE);
        }

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(sCommonUtils.getBoolean("az_order", true, requireActivity()));
            if (Common.getFiles() != null && Common.getFiles().size() > 0) {
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.export_selected_files));
                if (APKEditorUtils.isFullVersion(requireActivity())) {
                    menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.delete_selected_files));
                }
            }
            if (APKEditorUtils.isFullVersion(requireActivity()) && !Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath().equals(requireActivity().getCacheDir().getPath())) {
                menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.delete_folder));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    sCommonUtils.saveBoolean("az_order", !sCommonUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                    reload(requireActivity());
                } else if (item.getItemId() == 1) {
                    if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext())) {
                        sPermissionUtils.requestPermission(
                                new String[] {
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                }, (Activity) v.getContext());
                    } else {
                        new ExportToStorage(null, Common.getFiles(), Common.getAppID(), requireActivity()).execute();
                    }
                } else if (item.getItemId() == 2) {
                    for (File file : Common.getFiles()) {
                        if (file.exists()) {
                            new DeleteFile(file, v.getContext()) {

                                @Override
                                public void onPostExecute() {
                                    reload(requireActivity());
                                }
                            }.execute();
                        }
                    }
                } else if (item.getItemId() == 3) {
                    new DeleteFile(new File(Common.getPath()), v.getContext()) {

                        @Override
                        public void onPostExecute() {
                            Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
                            Common.clearFilesList();
                            reload(requireActivity());
                        }
                    }.execute();
                }
                return false;
            });
            popupMenu.show();
        });

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position)).isDirectory()) {
                Common.setPath(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                Common.clearFilesList();
                reload(requireActivity());
            } else if (APKExplorer.isTextFile(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position))) {
                Intent intent;
                if (APKEditorUtils.isFullVersion(requireActivity())) {
                    intent = new Intent(requireActivity(), TextEditorActivity.class);
                    intent.putExtra(TextEditorActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                } else {
                    intent = new Intent(requireActivity(), TextViewActivity.class);
                    intent.putExtra(TextViewActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                }
                startActivity(intent);
            } else if (APKExplorer.isImageFile(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position))) {
                Intent imageView = new Intent(requireActivity(), ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                startActivity(imageView);
            } else if (APKExplorer.getData(getFilesList(), true, requireActivity()).get(position).endsWith(".dex") || APKExplorer.getData(getFilesList(), true,
                    requireActivity()).get(position).endsWith("resources.arsc")) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.unsupported_file)
                        .setMessage(getString(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position).endsWith("resources.arsc") ?
                                R.string.unsupported_file_arsc :R.string.unsupported_file_dex))
                        .setPositiveButton(getString(R.string.cancel), (dialog, id) -> {
                        }).show();
            } else {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(getString(R.string.unknown_file_message, new File(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position)).getName()))
                        .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                        })
                        .setNegativeButton(getString(R.string.export), (dialog, id) -> {
                            if (sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, requireActivity())) {
                                sPermissionUtils.requestPermission(
                                        new String[] {
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        }, requireActivity());
                            } else {
                                new ExportToStorage(new File(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position)),
                                        null, Common.getAppID(), requireActivity()).execute();
                            }
                        })
                        .setPositiveButton(getString(R.string.open_as_text), (dialog1, id1) -> {
                            Intent intent;
                            if (APKEditorUtils.isFullVersion(requireActivity())) {
                                intent = new Intent(requireActivity(), TextEditorActivity.class);
                                intent.putExtra(TextEditorActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                            } else {
                                intent = new Intent(requireActivity(), TextViewActivity.class);
                                intent.putExtra(TextViewActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                            }
                            startActivity(intent);
                        }).show();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath().equals(requireActivity().getCacheDir().getPath())) {
                    retainDialog();
                } else {
                    Common.setPath(Objects.requireNonNull(new File(Common.getPath()).getParentFile()).getPath());
                    Common.clearFilesList();
                    reload(requireActivity());
                }
            }
        });

        return mRootView;
    }

    private void retainDialog() {
        if (sCommonUtils.getString("projectAction", null, requireActivity()) == null) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.save_projects_question)
                    .setNeutralButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setNegativeButton(getString(R.string.discard), (dialog, id) -> {
                        new DeleteProject(new File(requireActivity().getCacheDir(), Common.getAppID()), requireActivity()).execute();
                        requireActivity().finish();
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> requireActivity().finish()).show();
        } else if (sCommonUtils.getString("projectAction", null, requireActivity()).equals(getString(R.string.delete))) {
            new DeleteProject(new File(requireActivity().getCacheDir(), Common.getAppID()), requireActivity()).execute();
            requireActivity().finish();
        } else {
            requireActivity().finish();
        }
    }

    private File[] getFilesList() {
        return new File(Common.getPath()).listFiles();
    }

    private void reload(Activity activity) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                mRecycleViewAdapter = new APKExplorerAdapter(APKExplorer.getData(getFilesList(), true, activity), replaceFile);
            }

            @Override
            public void onPostExecute() {
                if (Common.getAppID() != null) {
                    mTitle.setText(Common.getPath().equals(new File(activity.getCacheDir(), Objects.requireNonNull(Common.getAppID()))
                                    .getAbsolutePath()) ? getString(R.string.root) : new File(Common.getPath()).getName());
                }
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                mProgressLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    ActivityResultLauncher<Intent> replaceFile = registerForActivityResult(
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
                                    reload(requireActivity());
                                }).show();
                    }
                }
            }
    );
    
}