package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.ResViewerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 03, 2025
 */
public abstract class ResEditorDialog extends MaterialAlertDialogBuilder {

    private static AlertDialog alertDialog = null;

    public ResEditorDialog(XMLEntry xmlEntry, List<ResEntry> resourceMap, String rootPath, Context context) {
        super(context);
        View rootView = View.inflate(context, R.layout.layout_resviewer, null);
        MaterialTextView title = rootView.findViewById(R.id.title);
        MaterialButton menu = rootView.findViewById(R.id.menu_button);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        title.setText(context.getString(R.string.res_choose_new, xmlEntry.getTag().trim()));
        menu.setIcon(sCommonUtils.getDrawable(R.drawable.ic_add, context));

        menu.setOnClickListener(v -> {
            apply(true, null);
            alertDialog.dismiss();
        });

        setView(rootView);
        setPositiveButton(R.string.cancel, (dialog, id) -> {
        });

        alertDialog = create();
        alertDialog.show();

        loadUI(xmlEntry, recyclerView, resourceMap, rootPath, context).execute();
    }

    private sExecutor loadUI(XMLEntry xmlEntry, RecyclerView recyclerView, List<ResEntry> resourceMap, String rootPath, Context context) {
        return new sExecutor() {
            private ProgressDialog mProgressDialog;
            private ResViewerAdapter adapter;
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setTitle(context.getString(R.string.loading));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
            }

            private String getExt(String name) {
                if (name == null || name.isEmpty()) {
                    return null;
                }
                String normalized = name.replace("\\", "/");

                int lastSlash = normalized.lastIndexOf("/");
                String fileName = (lastSlash == -1) ? normalized : normalized.substring(lastSlash + 1);

                int lastDot = fileName.lastIndexOf(".");
                if (lastDot == -1 || lastDot == fileName.length() - 1) {
                    return null;
                }
                return fileName.substring(lastDot + 1);
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
                        if (!xmlEntry.getTag().trim().equals("android:label") && entry.getValue().startsWith(getParentDir(xmlEntry.getValue())) && entry.getValue().endsWith(getExt(xmlEntry.getValue()))) {
                            resItems.add(entry);
                        }
                    }
                }

                adapter = new ResViewerAdapter(resItems, rootPath, true);
            }

            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener((newValue, v) -> {
                    apply(false, newValue);
                    alertDialog.dismiss();
                });
            }
        };
    }

    public abstract void apply(boolean editor, String newValue);

}