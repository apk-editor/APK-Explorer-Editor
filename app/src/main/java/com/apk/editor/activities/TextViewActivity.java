package com.apk.editor.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.apk.editor.utils.AppSettings;
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
                        if (Build.VERSION.SDK_INT >= 30 && APKExplorer.isPermissionDenied() && Projects.getExportPath(this)
                                .startsWith(Environment.getExternalStorageDirectory().toString())) {
                            new MaterialAlertDialogBuilder(this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(getString(R.string.important))
                                    .setMessage(getString(R.string.file_permission_request_message, getString(R.string.app_name)))
                                    .setCancelable(false)
                                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                    })
                                    .setPositiveButton(getString(R.string.grant), (dialog1, id1) -> APKExplorer.requestPermission(this)).show();
                        } else {
                            APKEditorUtils.mkdir(Projects.getExportPath(this) + "/" + APKExplorer.mAppID);
                            APKEditorUtils.copy(mPath, Projects.getExportPath(this) + "/" + APKExplorer.mAppID + "/" + new File(mPath).getName());
                            new MaterialAlertDialogBuilder(this)
                                    .setMessage(getString(R.string.export_complete_message, Projects.getExportPath(this) + "/" + APKExplorer.mAppID))
                                    .setPositiveButton(getString(R.string.cancel), (dialog1, id1) -> {
                                    }).show();
                        }
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
                holder.mNumber.setText(String.valueOf(position + 1));
                holder.mText.setText(APKEditorUtils.fromHtml(data.get(position).replace(TextEditor.mSearchText,
                        "<b><i><font color=\"" + Color.RED + "\">" + TextEditor.mSearchText + "</font></i></b>")));
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