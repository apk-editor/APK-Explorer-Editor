package com.apk.editor.utils.dialogs;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.ResourceTableParser;
import com.apk.axml.serializables.ResEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.ResViewerAdapter;
import com.apk.editor.utils.Serializables.ResViewerOptionsItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 03, 2025
 */
public class ResViewerDialog extends BottomSheetDialog {

    private static List<String> mTypes = null;
    private static String mTypeDefault = null;

    public ResViewerDialog(String filePath, Activity activity) {
        super(activity);
        View rootView = View.inflate(activity, R.layout.layout_resviewer, null);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialButton menu = rootView.findViewById(R.id.menu_button);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        menu.setVisibility(VISIBLE);

        loadUI(recyclerView, filePath, mTypeDefault, activity).execute();

        menu.setOnClickListener(v -> {
            List<ResViewerOptionsItems> data = new ArrayList<>();
            for (int i=0; i<mTypes.size(); i++) {
                data.add(new ResViewerOptionsItems(i, mTypes.get(i)));
            }

            new ResViewerOptionsDialog(data, mTypeDefault, v.getContext()) {
                @Override
                public void OnSelected(String text) {
                    loadUI(recyclerView, filePath, text, activity).execute();
                }
            };
        });

        cancel.setOnClickListener(v -> dismiss());

        setContentView(rootView);
        show();
    }

    private sExecutor loadUI(RecyclerView recyclerView, String path, String typeDefault, Activity activity) {
        return new sExecutor() {
            private boolean mSuccess;
            private List<ResEntry> mResourceMap = null;
            private ProgressDialog mProgressDialog;

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setTitle(activity.getString(R.string.decompiling, "resources.arsc"));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
            }

            private List<ResEntry> getResourceMap() {
                try (FileInputStream fis = new FileInputStream(path)) {
                    ResourceTableParser parser = new ResourceTableParser(fis);
                    return parser.parse();
                } catch (Exception ignore) {
                    mSuccess = false;
                    return null;
                }
            }

            private List<String> extractTypes(List<ResEntry> resMap) {
                if (resMap == null || resMap.isEmpty()) {
                    return new ArrayList<>();
                }
                List<String> types = new ArrayList<>();
                for (ResEntry entry : resMap) {
                    if (entry.getName() == null) continue;
                    int slashIndex = entry.getName().indexOf('/');
                    if (slashIndex == -1) continue;
                    String type = entry.getName().substring(1, slashIndex);
                    if (!type.isEmpty() && !types.contains(type)) {
                        types.add(type);
                    }
                }
                return types;
            }

            private List<ResEntry> getData() {
                List<ResEntry> resItems = new CopyOnWriteArrayList<>();
                if (mResourceMap == null || mResourceMap.isEmpty()) {
                    return resItems;
                }
                if (mTypes == null) {
                    mTypes = extractTypes(mResourceMap);
                }
                if (mTypes.isEmpty()) {
                    return resItems;
                }
                String defaultType = typeDefault != null ? typeDefault : mTypes.get(0);
                for (ResEntry entry : mResourceMap) {
                    if (entry.getName() != null && entry.getName().startsWith("@" + defaultType)) {
                        resItems.add(entry);
                    }
                }
                return resItems;
            }

            @Override
            public void doInBackground() {
                if (mResourceMap == null) {
                    mResourceMap = getResourceMap();
                }
                if (mResourceMap == null || mResourceMap.isEmpty()) {
                    mSuccess = false;
                    return;
                }
                mTypes = extractTypes(mResourceMap);
                if (mTypes.isEmpty()) {
                    mSuccess = false;
                    return;
                }
                mSuccess = true;
                mTypeDefault = typeDefault != null ? typeDefault : mTypes.get(0);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                if (mSuccess) {
                    recyclerView.setAdapter(new ResViewerAdapter(getData(), path.replace("/resources.arsc", ""), false,newValue ->
                            sPermissionUtils.requestPermission(new String[] {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, activity)));
                } else {
                    sCommonUtils.toast(activity.getString(R.string.xml_decode_failed, "resources.arsc"), activity).show();
                    dismiss();
                }
            }
        };
    }

}