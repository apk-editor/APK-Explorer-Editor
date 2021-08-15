package com.apk.editor.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.activities.TextViewActivity;
import com.apk.editor.adapters.RecycleViewAPKExplorerAdapter;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class APKExplorerFragment extends androidx.fragment.app.Fragment {

    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewAPKExplorerAdapter mRecycleViewAdapter;

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
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mTitle.setText(Common.getAppID() != null ? AppData.getAppName(Common.getAppID(), requireActivity()) : new File(Common.getPath()).getName());

        mBack.setOnClickListener(v -> retainDialog());

        mSave.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireActivity())
                .setMessage(R.string.save_apk_message)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.build), (dialog, id) -> {
                    if (!APKEditorUtils.getBoolean("firstSigning", false, requireActivity())) {
                        new MaterialAlertDialogBuilder(requireActivity()).setItems(requireActivity().getResources().getStringArray(
                                R.array.signing), (dialogInterface, i) -> {
                            APKEditorUtils.saveBoolean("firstSigning", true, requireActivity());
                            switch (i) {
                                case 0:
                                    APKData.prepareSignedAPK(requireActivity());
                                    break;
                                case 1:
                                    Intent signing = new Intent(requireActivity(), APKSignActivity.class);
                                    startActivity(signing);
                                    break;
                            }}).setCancelable(false)
                                .setOnDismissListener(dialogInterface -> {
                                }).show();
                    } else {
                        APKData.prepareSignedAPK(requireActivity());
                    }
                })
                .show());

        if (APKEditorUtils.isFullVersion(requireActivity())) {
            mSave.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), APKExplorer.getSpanCount(requireActivity())));

        try {
            mRecycleViewAdapter = new RecycleViewAPKExplorerAdapter(APKExplorer.getData(getFilesList(), true, requireActivity()));
            mRecyclerView.setAdapter(mRecycleViewAdapter);
        } catch (NullPointerException ignored) {
            mRecyclerView.setVisibility(View.GONE);
            mError.setText(getString(R.string.explore_error_status, AppData.getAppName(Common.getAppID(), requireActivity())));
            mError.setVisibility(View.VISIBLE);
        }

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(APKEditorUtils.getBoolean("az_order", true, requireActivity()));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    APKEditorUtils.saveBoolean("az_order", !APKEditorUtils.getBoolean("az_order", true, requireActivity()), requireActivity());
                    reload(requireActivity());
                }
                return false;
            });
            popupMenu.show();
        });

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position)).isDirectory()) {
                Common.setPath(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                reload(requireActivity());
            } else if (APKExplorer.isImageFile(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position))) {
                Intent imageView = new Intent(requireActivity(), ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                startActivity(imageView);
            } else if (APKExplorer.isTextFile(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position))) {
                Intent textView = new Intent(requireActivity(), TextViewActivity.class);
                textView.putExtra(TextViewActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                startActivity(textView);
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
                            if (APKExplorer.isPermissionDenied(requireActivity())) {
                                APKExplorer.launchPermissionDialog(requireActivity());
                            } else {
                                APKEditorUtils.mkdir(Projects.getExportPath(requireActivity()) + "/" + Common.getAppID());
                                APKEditorUtils.copy(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position), Projects.getExportPath(requireActivity()) + "/" + Common.getAppID() + "/"
                                        + new File(APKExplorer.getData(getFilesList(), true, requireActivity()).get(position)).getName());
                                new MaterialAlertDialogBuilder(requireActivity())
                                        .setMessage(getString(R.string.export_complete_message, Projects.getExportPath(requireActivity()) + "/" + Common.getAppID()))
                                        .setPositiveButton(getString(R.string.cancel), (dialog2, id2) -> {
                                        }).show();
                            }
                        })
                        .setPositiveButton(getString(R.string.open_as_text), (dialog1, id1) -> {
                            Intent textView = new Intent(requireActivity(), TextViewActivity.class);
                            textView.putExtra(TextViewActivity.PATH_INTENT, APKExplorer.getData(getFilesList(), true, requireActivity()).get(position));
                            startActivity(textView);
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
                    reload(requireActivity());
                }
            }
        });

        return mRootView;
    }

    private void retainDialog() {
        if (APKEditorUtils.getString("projectAction", null, requireActivity()) == null) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setMessage(R.string.save_projects_question)
                    .setNeutralButton(getString(R.string.delete), (dialog, id) -> {
                        Projects.deleteProject(new File(requireActivity().getCacheDir().getPath(), Common.getAppID() != null ? Common.getAppID() :
                                new File(Common.getPath()).getName()), requireActivity());
                        requireActivity().finish();
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> requireActivity().finish()).show();
        } else if (APKEditorUtils.getString("projectAction", null, requireActivity()).equals(getString(R.string.delete))) {
            Projects.deleteProject(new File(requireActivity().getCacheDir().getPath(), Common.getAppID() != null ? Common.getAppID() :
                    new File(Common.getPath()).getName()), requireActivity());
            requireActivity().finish();
        } else {
            requireActivity().finish();
        }
    }

    private File[] getFilesList() {
        return new File(Common.getPath()).listFiles();
    }

    @SuppressLint("StaticFieldLeak")
    private void reload(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mRecycleViewAdapter = new RecycleViewAPKExplorerAdapter(APKExplorer.getData(getFilesList(), true, activity));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (Common.getAppID() != null) {
                    mTitle.setText(Common.getAppID().equals(requireActivity().getCacheDir().getPath() + "/" + (Common.getAppID() != null ?
                            Common.getAppID() : new File(Common.getPath()).getName()) + File.separator) ? AppData.getAppName(Common.getAppID(), activity)
                            : new File(Common.getPath()).getName());
                }
                mRecyclerView.setAdapter(mRecycleViewAdapter);
            }
        }.execute();
    }
    
}