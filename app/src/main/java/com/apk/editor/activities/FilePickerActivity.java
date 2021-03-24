package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.RecycleViewFilePickerAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class FilePickerActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewFilePickerAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        AppCompatImageButton mSortButton = findViewById(R.id.sort);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, APKExplorer.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewFilePickerAdapter(APKExplorer.getData(getFilesList(), this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(APKExplorer.mFilePath.equals("/storage/emulated/0/") ? getString(R.string.sdcard) : new File(APKExplorer.mFilePath).getName());

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (new File(APKExplorer.getData(getFilesList(), this).get(position)).isDirectory()) {
                APKExplorer.mFilePath = APKExplorer.getData(getFilesList(), this).get(position);
                reload(this);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(APKExplorer.mFileToReplace != null ? getString(R.string.replace_question, new File(APKExplorer
                                .mFileToReplace).getName()) + " " +
                                new File(APKExplorer.getData(getFilesList(), this).get(position)).getName() + "?" : getString(R.string.signing_question,
                                new File(APKExplorer.getData(getFilesList(), this).get(position)).getName()) + " " + getString(APKExplorer.mPrivateKey ?
                                R.string.private_key : R.string.rsa_template))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(APKExplorer.mFileToReplace != null ? R.string.replace : R.string.select, (dialog, id) -> {
                            if (APKExplorer.mFileToReplace != null) {
                                APKEditorUtils.copy(APKExplorer.getData(getFilesList(), this).get(position), APKExplorer.mFileToReplace);
                                APKExplorer.mFileToReplace = null;
                            }  else {
                                new File(getFilesDir(), "signing").mkdirs();
                                if (APKExplorer.mPrivateKey) {
                                    APKEditorUtils.saveString("PrivateKey", APKExplorer.getData(getFilesList(), this).get(position), this);
                                    APKEditorUtils.copy(APKExplorer.getData(getFilesList(), this).get(position), getFilesDir()+ "/signing/APKEditor.pk8");
                                    APKExplorer.mPrivateKey = false;
                                } else {
                                    APKEditorUtils.saveString("RSATemplate", APKExplorer.getData(getFilesList(), this).get(position), this);
                                    APKEditorUtils.copy(APKExplorer.getData(getFilesList(), this).get(position), getFilesDir()+ "/signing/APKEditor");
                                    APKExplorer.mRSATemplate = false;
                                }
                            }
                            finish();
                        }).show();
            }
        });

        mSortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mSortButton);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.sort_order)).setCheckable(true)
                    .setChecked(APKEditorUtils.getBoolean("az_order", true, this));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    if (APKEditorUtils.getBoolean("az_order", true, this)) {
                        APKEditorUtils.saveBoolean("az_order", false, this);
                    } else {
                        APKEditorUtils.saveBoolean("az_order", true, this);
                    }
                    reload(this);
                }
                return false;
            });
            popupMenu.show();
        });

        mBack.setOnClickListener(v -> {
            super.onBackPressed();
        });
    }

    private File[] getFilesList() {
        if (APKExplorer.mFilePath == null) {
            APKExplorer.mFilePath = Environment.getExternalStorageDirectory().toString();
        }
        if (!APKExplorer.mFilePath.endsWith(File.separator)) {
            APKExplorer.mFilePath = APKExplorer.mFilePath + File.separator;
        }
        return new File(APKExplorer.mFilePath).listFiles();
    }

    private void reload(Activity activity) {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            APKExplorer.getData(getFilesList(), activity).clear();
                            mRecyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewFilePickerAdapter(APKExplorer.getData(getFilesList(), activity));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mTitle.setText(APKExplorer.mFilePath.equals("/storage/emulated/0/") ? getString(R.string.sdcard)
                                    : new File(APKExplorer.mFilePath).getName());
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onBackPressed() {
        if (APKExplorer.mFilePath.equals("/storage/emulated/0/")) {
            super.onBackPressed();
        } else {
            APKExplorer.mFilePath = Objects.requireNonNull(new File(APKExplorer.mFilePath).getParentFile()).getPath();
            reload(this);
        }
    }

}