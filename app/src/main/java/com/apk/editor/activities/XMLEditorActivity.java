package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.XMLEditorAdapter;
import com.apk.editor.utils.APKExplorer;
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

    private ArrayList<String> mData;
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

        loadUI(mSearchText);

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadUI(s.toString().toLowerCase());
            }
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (mSearchText != null) {
                    mSearch.setText(null);
                }
                finish();
            }
        });
    }

    private void loadUI(String string) {
        loadUI(null, string).execute();
    }

    private sExecutor loadUI(Intent intent, String string) {
        return new sExecutor() {
            private boolean mRemoved = false, mInvalid = false;
            private int mPosition = RecyclerView.NO_POSITION;
            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                if (intent == null) {
                    mRecyclerView.removeAllViews();
                }
            }

            @Override
            public void doInBackground() {
                if (intent == null) {
                    mData = APKExplorer.getXMLData(mPath);
                    if (mData != null && !mData.isEmpty()) {
                        mAdapter = new XMLEditorAdapter(mData, mPath, string, resultLauncher);
                    } else {
                        mInvalid = true;
                    }
                } else {
                    mRemoved = intent.getBooleanExtra("removed", false);
                    mPosition = intent.getIntExtra("position", RecyclerView.NO_POSITION);
                    if (mRemoved) {
                        mData.remove(mPosition);
                    } else {
                        mData.set(mPosition, intent.getStringExtra("newString"));
                    }
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (intent != null) {
                    if (mRemoved) {
                        mAdapter.notifyItemRemoved(mPosition);
                    } else {
                        mAdapter.notifyItemChanged(mPosition);
                    }
                } else {
                    if (mInvalid) {
                        sCommonUtils.toast(getString(R.string.xml_decode_failed, new File(mPath).getName()), XMLEditorActivity.this).show();
                    } else {
                        mRecyclerView.setAdapter(mAdapter);
                    }
                    mSearchText = string;
                }
                mProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        };
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    loadUI(result.getData(), mSearchText).execute();
                }
            }
    );

}