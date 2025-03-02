package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.BatchOptionsAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.tasks.ExportApp;
import com.apk.editor.utils.tasks.ResignBatchAPKs;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public abstract class ExportOptionsDialog {

    private AlertDialog alertDialog = null;

    public ExportOptionsDialog(List<String> packageNames, boolean checked, Activity activity) {
        boolean full = APKEditorUtils.isFullVersion(activity);
        View rootView = View.inflate(activity, R.layout.layout_batchoptions, null);
        LinearLayoutCompat linearLayoutCompat = rootView.findViewById(R.id.select_all_layout);
        MaterialCheckBox checkBox = rootView.findViewById(R.id.checkbox);
        checkBox.setChecked(checked);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        BatchOptionsAdapter adapter = new BatchOptionsAdapter(packageNames);
        recyclerView.setAdapter(adapter);

        linearLayoutCompat.setOnClickListener(view -> {
            selectAllLister(checkBox.isChecked());
            alertDialog.dismiss();
        });

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(activity)
                .setIcon(R.drawable.ic_export_file)
                .setTitle(R.string.export_app_batch_question)
                .setView(rootView)
                .setNeutralButton(getNeutralButtonTitle(full, activity), (dialog, id) -> {
                    if (full && sCommonUtils.getString("exportAPKs", null, activity) == null) {
                        new ExportApp(packageNames, activity).execute();
                    }
                })
                .setPositiveButton(getPositiveButtonTitle(full, activity), (dialog, id) -> {
                    if (full && (sCommonUtils.getString("exportAPKs", null, activity) == null || sCommonUtils.getString(
                            "exportAPKs", null, activity).equals(activity.getString(R.string.export_resign)))) {
                        resign(packageNames, activity);
                    } else {
                        new ExportApp(packageNames, activity).execute();
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private static String getNeutralButtonTitle(boolean full, Activity activity) {
        if (full && sCommonUtils.getString("exportAPKs", null, activity) == null) {
            return activity.getString(R.string.export_storage);
        } else {
            return activity.getString(R.string.cancel);
        }
    }

    private static String getPositiveButtonTitle(boolean full, Activity activity) {
        if (full) {
            if (sCommonUtils.getString("exportAPKs", null, activity) == null) {
                return activity.getString(R.string.export_resign);
            } else if (sCommonUtils.getString("exportAPKs", null, activity).equals(activity.getString(R.string.export_storage))) {
                return activity.getString(R.string.export_storage);
            } else {
                return activity.getString(R.string.export_resign);
            }
        } else {
            return activity.getString(R.string.export_storage);
        }
    }

    private void resign(List<String> packageNames, Activity activity) {
        if (!sCommonUtils.getBoolean("firstSigning", false, activity)) {
            new BatchSigningOptionsDialog(packageNames, activity).show();
        } else {
            new ResignBatchAPKs(packageNames, activity).execute();
        }
    }

    public abstract void selectAllLister(boolean checked);

}