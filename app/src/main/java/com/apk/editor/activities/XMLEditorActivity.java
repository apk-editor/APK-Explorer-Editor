package com.apk.editor.activities;

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

import com.apk.axml.ResourceTableParser;
import com.apk.axml.aXMLDecoder;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.XMLEditorAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 27, 2024
 */
public class XMLEditorActivity extends AppCompatActivity {

    private List<XMLEntry> mData;
    private List<ResEntry> mResourceMap;
    private ContentLoadingProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private String mPath = null, mResPath = null, mSearchText = null;
    public static final String PATH_INTENT = "path", RESOURCE_PATH_INTENT = "resource_path";
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
        mResPath = getIntent().getStringExtra(RESOURCE_PATH_INTENT);

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

    private sExecutor loadUI(String searchText) {
        return new sExecutor() {
            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
            }

            private List<ResEntry> getResourceMap() {
                try (FileInputStream fis = new FileInputStream(mResPath)) {
                    ResourceTableParser parser = new ResourceTableParser(fis);
                    return parser.parse();
                } catch (IOException ignored) {
                    return null;
                }
            }

            @Override
            public void doInBackground() {
                mResourceMap = getResourceMap();
                try (FileInputStream fis = new FileInputStream(mPath)) {
                    if (mResourceMap != null) {
                        mData = new aXMLDecoder(fis, mResourceMap).decode();
                    } else {
                        mData = new aXMLDecoder(fis).decode();
                    }
                } catch (IOException | XmlPullParserException ignored) {
                }
                mAdapter = new XMLEditorAdapter(mData, mResourceMap, mPath, mResPath.replace("/resources.arsc", ""), searchText);
            }

            @Override
            public void onPostExecute() {
                mSearchText = searchText;
                mRecyclerView.setAdapter(mAdapter);
                mProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        };
    }

}