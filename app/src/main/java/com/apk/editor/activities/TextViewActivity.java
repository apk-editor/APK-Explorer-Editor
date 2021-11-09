package com.apk.editor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.TextViewAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class TextViewActivity extends AppCompatActivity {

    private AppCompatEditText mSearchWord;
    private MaterialTextView mTitle;
    public static final String PATH_INTENT = "path";
    private String mPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);

        mSearchWord = findViewById(R.id.search_word);
        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSearch = findViewById(R.id.search);
        AppCompatImageButton mEdit = findViewById(R.id.edit);
        AppCompatImageButton mExport = findViewById(R.id.export);
        mTitle = findViewById(R.id.title);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        if (APKEditorUtils.isFullVersion(this) && AppSettings.isTextEditingEnabled(this)) {
            mEdit.setVisibility(View.VISIBLE);
        }

        mPath = getIntent().getStringExtra(PATH_INTENT);

        assert mPath != null;
        mTitle.setText(new File(mPath).getName());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new TextViewAdapter(APKExplorer.getTextViewData(mPath, this)));

        mSearch.setOnClickListener(v -> {
            if (mSearchWord.getVisibility() == View.VISIBLE) {
                mSearchWord.setVisibility(View.GONE);
                mTitle.setVisibility(View.VISIBLE);
                AppData.toggleKeyboard(0, mSearchWord, this);
            } else {
                mSearchWord.setVisibility(View.VISIBLE);
                mTitle.setVisibility(View.GONE);
                AppData.toggleKeyboard(1, mSearchWord, this);
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
                Common.setSearchText(s.toString());
                mRecyclerView.setAdapter(new TextViewAdapter(APKExplorer.getTextViewData(mPath, TextViewActivity.this)));

            }
        });

        mEdit.setOnClickListener(v -> {
            Intent textEditor = new Intent(this, TextEditorActivity.class);
            textEditor.putExtra(TextEditorActivity.PATH_INTENT, mPath);
            startActivity(textEditor);
            finish();
        });

        mExport.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.export_question)
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(getString(R.string.export), (dialog, id) -> {
                    if (APKExplorer.isPermissionDenied(this)) {
                        APKExplorer.requestPermission(this);
                    } else {
                        Projects.exportToStorage(mPath, new File(mPath).getName(), Common.getAppID(), this).execute();
                    }
                }).show());

        mBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        if (mSearchWord.getVisibility() == View.VISIBLE) {
            mSearchWord.setVisibility(View.GONE);
            mTitle.setVisibility(View.VISIBLE);
            mSearchWord.setText(null);
            return;
        }
        super.onBackPressed();
    }

}