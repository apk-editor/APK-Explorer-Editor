package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.ARSCDecoder;
import com.apk.editor.R;
import com.apk.editor.adapters.ResViewerAdapter;
import com.apk.editor.utils.SerializableItems.ResItems;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 22, 2025
 */
public class ResViewerActivity extends AppCompatActivity {

    private static List<String> mType = null;
    private static String mTypeDefault = "string";
    public static final String PATH_INTENT = "path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resviewer);

        AppCompatImageButton mBack = findViewById(R.id.back);
        MaterialButton mMenu = findViewById(R.id.menu_button);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        String path = getIntent().getStringExtra(PATH_INTENT);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUI(mRecyclerView, path, mTypeDefault, this).execute();

        mBack.setOnClickListener(v -> finish());

        mMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            Menu menu = popupMenu.getMenu();
            for (int i=0; i<mType.size(); i++) {
                menu.add(0, i, Menu.NONE, mType.get(i)).setChecked(true).setChecked(Objects.equals(mType.get(i), mTypeDefault));
            }
            menu.setGroupCheckable(0, true, true);
            popupMenu.setOnMenuItemClickListener(item -> {
                loadUI(mRecyclerView, path, mType.get(item.getItemId()), this).execute();
                return false;
            });
            popupMenu.show();
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
               finish();
            }
        });
    }

    private static sExecutor loadUI(RecyclerView recyclerView, String path, String typeDefault, Activity activity) {
        return new sExecutor() {
            private boolean mSuccess = true;
            private List<ResItems> mData;
            private ProgressDialog mProgressDialog;
            private ResViewerAdapter mAdapter;
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setTitle(activity.getString(R.string.decompiling, "resources.arsc"));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
                mData = new ArrayList<>();
                mType = new ArrayList<>();
                mTypeDefault = typeDefault;
            }

            private List<ResItems> getRawData() {
                List<ResItems> rawData = new ArrayList<>();
                File mResFileDecoded = new File(Objects.requireNonNull(path).replace("resources.arsc", ".aeeBackup/resources.txt"));
                if (!mResFileDecoded.exists()) {
                    try (FileInputStream fis = new FileInputStream(path)) {
                        String resStringDecoded = new ARSCDecoder(fis).getPublicXML();
                        if (resStringDecoded != null) {
                            sFileUtils.create(resStringDecoded, mResFileDecoded);
                            mSuccess = true;
                        } else {
                            mSuccess = false;
                        }
                    } catch (IOException ignored) {}
                }
                if (mSuccess) {
                    for (String lines : sFileUtils.read(mResFileDecoded).trim().trim().split("\\r?\\n")) {
                        if (lines.contains("<public id=") && lines.contains("type=\"") && lines.contains("name=\"") && lines.contains("data=\"")) {
                            String[] splitString = lines.trim().split("\" ");
                            String publicID = splitString[0].replace("<public id=\"", "");
                            String type = splitString[1].replace("type=\"", "");
                            String name = splitString[2].replace("name=\"", "");
                            String value = splitString[3].replace("data=\"", "").replace("\"/>\"", "");

                            if (!mType.contains(type)) {
                                mType.add(type);
                            }
                            rawData.add(new ResItems(publicID, type, name, value));
                        }
                    }
                }
                return rawData;
            }

            @Override
            public void doInBackground() {
                if (!getRawData().isEmpty()) {
                    for (ResItems items : getRawData()) {
                        if (Objects.equals(items.getType(), typeDefault)) {
                            mData.add(items);
                        }
                    }
                    mAdapter = new ResViewerAdapter(mData);
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                if (mSuccess) {
                    recyclerView.setAdapter(mAdapter);
                } else {
                    sCommonUtils.toast(activity.getString(R.string.xml_decode_failed, "resources.arsc"), activity).show();
                    activity.finish();
                }
            }
        };
    }

}