package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.XMLEditorAdapter;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 27, 2024
 */
public class XMLEditorActivity extends AppCompatActivity {

    private ContentLoadingProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private String mPath = null, mSearchText = null;
    public static final String PATH_INTENT = "path";
    private XMLEditorAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmleditor);

        mProgress = findViewById(R.id.progress);
        MaterialAutoCompleteTextView mSearch = findViewById(R.id.search);
        MaterialTextView mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPath = getIntent().getStringExtra(PATH_INTENT);

        if (mPath != null && sFileUtils.exist(new File(mPath))) {
            mTitle.setText(new File(mPath).getName());
            mTitle.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.GONE);
        }

        loadUI(mSearchText).execute();

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadUI(s.toString().toLowerCase()).execute();
            }
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Common.isBusy()) return;
                if (mSearchText != null) {
                    mSearch.setText(null);
                }
                finish();
            }
        });
    }

    private sExecutor loadUI(String string) {
        return new sExecutor() {
            private boolean mInvalid = false;
            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                mRecyclerView.removeAllViews();
            }

            @Override
            public void doInBackground() {
                ArrayList<String> mData = APKExplorer.getXMLData(mPath);
                if (mData != null && !mData.isEmpty()) {
                    mAdapter = new XMLEditorAdapter(mData, mPath, string);
                } else {
                    mInvalid = true;
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (mInvalid) {
                    sCommonUtils.toast(getString(R.string.xml_decode_failed, new File(mPath).getName()), XMLEditorActivity.this).show();
                } else {
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                mSearchText = string;
                mProgress.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Common.isReloading()) {
            Common.isReloading(false);
            loadUI(mSearchText).execute();
        }
    }

}