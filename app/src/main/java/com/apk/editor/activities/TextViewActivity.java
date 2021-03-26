package com.apk.editor.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Projects;
import com.apk.editor.utils.TextEditor;
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
    private List<String> mData = new ArrayList<>();
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

        if (APKEditorUtils.isFullVersion(this)) {
            mEdit.setVisibility(View.VISIBLE);
        }

        mPath = getIntent().getStringExtra(PATH_INTENT);

        assert mPath != null;
        mTitle.setText(new File(mPath).getName());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new RecycleViewAdapter(getData()));

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
                TextEditor.mSearchText = s.toString();
                mRecyclerView.setAdapter(new RecycleViewAdapter(getData()));

            }
        });

        mEdit.setOnClickListener(v -> {
            Intent textEditor = new Intent(this, TextEditorActivity.class);
            textEditor.putExtra(TextEditorActivity.PATH_INTENT, mPath);
            startActivity(textEditor);
            finish();
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
                        APKEditorUtils.mkdir(Projects.getExportPath(this) + "/" + APKExplorer.mAppID);
                        APKEditorUtils.copy(mPath, Projects.getExportPath(this) + "/" + APKExplorer.mAppID + "/" + new File(mPath).getName());
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.export_complete_message, Projects.getExportPath(this) + "/" + APKExplorer.mAppID))
                                .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                                }).show();
                    }).show();
        });

        mBack.setOnClickListener(v -> finish());
    }

    private List<String> getData() {
        mData.clear();
        String text;
        if (APKExplorer.mAppID != null && TextEditor.isBinaryXML(mPath)) {
            text = APKExplorer.readXMLFromAPK(AppData.getSourceDir(APKExplorer.mAppID, this), mPath.replace(
                    getCacheDir().getPath() + "/" + APKExplorer.mAppID + "/", ""));
        } else {
            text = APKEditorUtils.read(mPath);
        }
        if (text != null) {
            for (String line : text.split("\\r?\\n")) {
                if (TextEditor.mSearchText == null) {
                    mData.add(line);
                } else if (line.contains(TextEditor.mSearchText)) {
                    mData.add(line);
                }
            }
        }
        return mData;
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private static List<String> data;

        public RecycleViewAdapter(List<String> data) {
            RecycleViewAdapter.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_textview, parent, false);
            return new RecycleViewAdapter.ViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            if (TextEditor.mSearchText != null && data.get(position).contains(TextEditor.mSearchText)) {
                holder.mText.setText(APKEditorUtils.fromHtml(data.get(position).replace(TextEditor.mSearchText,
                        "<b><i><font color=\"" + Color.RED + "\">" + TextEditor.mSearchText + "</font></i></b>")));
            } else {
                holder.mText.setText(data.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private MaterialTextView mText;

            public ViewHolder(View view) {
                super(view);
                this.mText = view.findViewById(R.id.text);
            }
        }
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