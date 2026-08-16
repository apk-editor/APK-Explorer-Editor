package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.ResViewerAdapter;
import com.apk.editor.utils.XMLEditor;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 03, 2025
 */
public abstract class ResEditorDialog extends BottomSheetDialog {

    public ResEditorDialog(XMLEntry xmlEntry, List<ResEntry> resourceMap, String rootPath, Activity activity) {
        super(activity);
        View rootView = View.inflate(activity, R.layout.layout_resviewer, null);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialTextView title = rootView.findViewById(R.id.title);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        title.setText(activity.getString(R.string.res_choose_new, xmlEntry.getTag().trim()));

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        cancel.setOnClickListener(v -> dismiss());

        loadUI(xmlEntry, recyclerView, resourceMap, rootPath, activity).execute();

        setContentView(rootView);
        show();
    }

    private sExecutor loadUI(XMLEntry xmlEntry, RecyclerView recyclerView, List<ResEntry> resourceMap, String rootPath, Activity activity) {
        return new sExecutor() {
            private List<ResEntry> resItems;
            private ProgressDialog mProgressDialog;

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
                resItems = new CopyOnWriteArrayList<>();
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
            }

            @Override
            public void onPostExecute() {
                mProgressDialog.dismiss();
                recyclerView.setAdapter(new ResViewerAdapter(resItems, rootPath, true,newValue -> {
                    apply(newValue);
                    dismiss();
                }));
            }
        };
    }

    public abstract void apply(String newValue);

}