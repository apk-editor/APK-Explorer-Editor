package com.apk.editor.activities;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class TextViewActivity extends AppCompatActivity {

    private AppCompatEditText mSearchWord, mText;
    private MaterialTextView mTitle;
    public static final String PATH_INTENT = "path";
    private String mPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);

        mSearchWord = findViewById(R.id.search_word);
        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        AppCompatImageButton mSearch = findViewById(R.id.search);
        AppCompatImageButton mExport = findViewById(R.id.export);
        mTitle = findViewById(R.id.title);
        mText = findViewById(R.id.text);

        mPath = getIntent().getStringExtra(PATH_INTENT);

        mText.setTextColor(APKEditorUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        assert mPath != null;
        mTitle.setText(new File(mPath).getName());
        loadText(null);

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
                loadText(s.toString());

            }
        });

        mExport.setOnClickListener(v -> {
            if (!APKEditorUtils.isWritePermissionGranted(this)) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                APKEditorUtils.snackbar(findViewById(android.R.id.content), getString(R.string.permission_denied_message));
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.export_question)
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(getString(R.string.export), (dialog, id) -> {
                        APKEditorUtils.mkdir(Projects.getExportPath() + "/" + APKExplorer.mAppID);
                        APKEditorUtils.copy(mPath, Projects.getExportPath() + "/" + APKExplorer.mAppID + "/" + new File(mPath).getName());
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.export_complete_message, Projects.getExportPath() + "/" + APKExplorer.mAppID))
                                .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                                }).show();
                    }).show();
        });

        mSave.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.save_question)
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) -> {
                        APKEditorUtils.create(Objects.requireNonNull(mText.getText()).toString(), mPath);
                        finish();
                    }).show());

        if (APKEditorUtils.isFullVersion(this)) {
            mSave.setVisibility(View.VISIBLE);
        }

        mBack.setOnClickListener(v -> finish());
    }

    private void loadText(String searchText) {
        String text;
        if (APKExplorer.mAppID != null && mPath.endsWith(".xml")) {
            text = APKExplorer.readXMLFromAPK(AppData.getSourceDir(APKExplorer.mAppID, this), mPath.replace(
                    getCacheDir().getPath() + "/" + APKExplorer.mAppID + "/", ""));
        } else {
            text = APKEditorUtils.read(mPath);
        }
        StringBuilder sb = new StringBuilder();
        if (searchText != null) {
            if (text == null) return;
            for (String line : text.split("\\r?\\n")) {
                if (line.contains(searchText)) {
                    sb.append(line).append("\n");
                }
            }
            text = sb.toString();
        }
        mText.setText(searchText == null ? text : APKEditorUtils.fromHtml(text.replace(searchText,
                "<b><i><font color=\"" + Color.RED + "\">" + searchText + "</font></i></b>")));
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