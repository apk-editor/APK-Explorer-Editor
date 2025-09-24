package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.ResViewerAdapter;
import com.apk.editor.utils.XMLEditor;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 03, 2025
 */
public abstract class ResEditorDialog extends MaterialAlertDialogBuilder {

    private static AlertDialog alertDialog = null;

    public ResEditorDialog(XMLEntry xmlEntry, List<ResEntry> resourceMap, String rootPath, Activity activity) {
        super(activity);
        View rootView = View.inflate(activity, R.layout.layout_recyclerview, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        setView(rootView);setIcon(R.drawable.ic_edit);
        setTitle(activity.getString(R.string.res_choose_new, xmlEntry.getTag().trim()));
        setPositiveButton(R.string.cancel, (dialog, id) -> {
        });

        alertDialog = create();
        alertDialog.show();

        loadUI(xmlEntry, recyclerView, resourceMap, rootPath, activity).execute();
    }

    private sExecutor loadUI(XMLEntry xmlEntry, RecyclerView recyclerView, List<ResEntry> resourceMap, String rootPath, Activity activity) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;
            private ResViewerAdapter adapter;
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setTitle(activity.getString(R.string.loading));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
            }

            private String getParentDir(String name) {
                if (name == null || name.isEmpty()) {
                    return null;
                }
                String normalized = name.replace("\\", "/");

                int lastSlash = normalized.lastIndexOf("/");
                if (lastSlash == -1) {
                    return null;
                }
                return normalized.substring(0, lastSlash);
            }

            @Override
            public void doInBackground() {
                List<ResEntry> resItems = new CopyOnWriteArrayList<>();
                for (ResEntry entry : resourceMap) {
                    if (entry.getValue() != null) {
                        if (xmlEntry.getTag().trim().equals("android:label") && entry.getName().startsWith("@string/")) {
                            resItems.add(entry);
                        }
                        if (!xmlEntry.getTag().trim().equals("android:label") && entry.getValue().startsWith(getParentDir(xmlEntry.getValue())) && entry.getValue().endsWith(XMLEditor.getExt(xmlEntry.getValue()))) {
                            resItems.add(entry);
                        }
                    }
                }

                adapter = new ResViewerAdapter(resItems, rootPath, true, activity);
            }

            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener((newValue, v) -> {
                    apply(newValue);
                    alertDialog.dismiss();
                });
            }
        };
    }

    public abstract void apply(String newValue);

}