package com.apk.editor.utils.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.ResourceTableParser;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.ResViewerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 03, 2025
 */
public class ResViewerDialog extends MaterialAlertDialogBuilder {

    private static AlertDialog mAlertDialog = null;
    private static List<String> mTypes = null;
    private static String mTypeDefault = null;

    public ResViewerDialog(String filePath, Activity activity) {
        super(activity);
        View rootView = View.inflate(activity, R.layout.layout_resviewer, null);
        MaterialButton menu = rootView.findViewById(R.id.menu_button);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        loadUI(recyclerView, filePath, mTypeDefault, activity).execute();

        menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            Menu pMenu = popupMenu.getMenu();
            for (int i=0; i<mTypes.size(); i++) {
                pMenu.add(0, i, Menu.NONE, mTypes.get(i)).setChecked(true).setChecked(Objects.equals(mTypes.get(i), mTypeDefault));
            }
            pMenu.setGroupCheckable(0, true, true);
            popupMenu.setOnMenuItemClickListener(item -> {
                loadUI(recyclerView, filePath, mTypes.get(item.getItemId()), activity).execute();
                return false;
            });
            popupMenu.show();
        });

        setView(rootView);
        setCancelable(false);
        setPositiveButton(R.string.cancel, (dialog, id) -> {
        });
        mAlertDialog = create();
        mAlertDialog.show();
    }

    private static sExecutor loadUI(RecyclerView recyclerView, String path, String typeDefault, Activity activity) {
        return new sExecutor() {
            private boolean mSuccess;
            private List<ResEntry> mResourceMap = null;
            private ProgressDialog mProgressDialog;
            private ResViewerAdapter adapter;

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
                } catch (IOException ignore) {
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
                adapter = new ResViewerAdapter(getData(), path.replace("/resources.arsc", ""), false);
                mSuccess = true;
                mTypeDefault = typeDefault != null ? typeDefault : mTypes.get(0);
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                if (mSuccess) {
                    recyclerView.setAdapter(adapter);
                } else {
                    sCommonUtils.toast(activity.getString(R.string.xml_decode_failed, "resources.arsc"), activity).show();
                    mAlertDialog.dismiss();
                }
            }
        };
    }

}