package com.apk.editor.activities;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Projects;
import com.apk.editor.utils.Common;
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
    private final List<String> mData = new ArrayList<>();
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
                Common.setSearchText(s.toString());
                mRecyclerView.setAdapter(new RecycleViewAdapter(getData()));

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
                        APKExplorer.launchPermissionDialog(this);
                    } else {
                        APKEditorUtils.mkdir(Projects.getExportPath(this) + "/" + Common.getAppID());
                        APKEditorUtils.copy(mPath, Projects.getExportPath(this) + "/" + Common.getAppID() + "/" + new File(mPath).getName());
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.export_complete_message, Projects.getExportPath(this) + "/" + Common.getAppID()))
                                .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                                }).show();
                    }
                }).show());

        mBack.setOnClickListener(v -> finish());
    }

    private List<String> getData() {
        mData.clear();
        String text;
        if (Common.getAppID() != null && APKExplorer.isBinaryXML(mPath)) {
            text = APKExplorer.readXMLFromAPK(AppData.getSourceDir(Common.getAppID(), this), mPath.replace(
                    getCacheDir().getPath() + "/" + Common.getAppID() + "/", ""));
        } else {
            text = APKEditorUtils.read(mPath);
        }
        if (text != null) {
            for (String line : text.split("\\r?\\n")) {
                if (Common.getSearchText() == null) {
                    mData.add(line);
                } else if (Common.isTextMatched(line, Common.getSearchText())) {
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
            if (Common.getSearchText() != null && Common.isTextMatched(data.get(position), Common.getSearchText())) {
                holder.mNumber.setText(String.valueOf(position + 1));
                holder.mText.setText(APKEditorUtils.fromHtml(data.get(position).replace(Common.getSearchText(),
                        "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchText() + "</font></i></b>")));
            } else {
                holder.mNumber.setText(String.valueOf(position + 1));
                holder.mText.setText(data.get(position));
            }
            holder.mNumber.setTextColor(Color.MAGENTA);
            if (data.get(position).contains("<manifest") || data.get(position).contains("</manifest>")) {
                holder.mText.setTextColor(APKEditorUtils.getThemeAccentColor(holder.mText.getContext()));
            } else if (data.get(position).contains("<uses-permission")) {
                holder.mText.setTextColor(Color.RED);
            } else if (data.get(position).contains("<activity") || data.get(position).startsWith(".method") || data.get(position).startsWith(".annotation")) {
                holder.mText.setTextColor(APKEditorUtils.isDarkTheme(holder.mText.getContext()) ? Color.GREEN : Color.MAGENTA);
            } else if (data.get(position).contains("<service") || data.get(position).startsWith(".end method") || data.get(position).startsWith(".end annotation")) {
                holder.mText.setTextColor(APKEditorUtils.isDarkTheme(holder.mText.getContext()) ? Color.MAGENTA : Color.BLUE);
            } else if (data.get(position).contains("<provider") || data.get(position).contains("</provider>")) {
                holder.mText.setTextColor(APKEditorUtils.isDarkTheme(holder.mText.getContext()) ? Color.LTGRAY : Color.DKGRAY);
            } else {
                holder.mText.setTextColor(APKEditorUtils.isDarkTheme(holder.mText.getContext()) ? Color.WHITE : Color.BLACK);
            }
            if (data.get(position).startsWith("#")) {
                holder.mText.setAlpha((float) 0.5);
            } else {
                holder.mText.setAlpha(1);
            }
            holder.mNumber.setAlpha((float) 0.5);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final MaterialTextView mNumber, mText;

            public ViewHolder(View view) {
                super(view);
                this.mNumber = view.findViewById(R.id.number);
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