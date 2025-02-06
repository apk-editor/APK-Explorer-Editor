package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.BatchOptionsAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.tasks.ExportApp;
import com.apk.editor.utils.tasks.ResignBatchAPKs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public class ExportOptionsDialog {

    public ExportOptionsDialog(List<String> packageNames, Activity activity) {
        boolean full = APKEditorUtils.isFullVersion(activity);

        View rootView = View.inflate(activity, R.layout.layout_recyclerview, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        BatchOptionsAdapter adapter = new BatchOptionsAdapter(packageNames);
        recyclerView.setAdapter(adapter);

        new MaterialAlertDialogBuilder(activity)
                .setIcon(R.drawable.ic_export_file)
                .setTitle(R.string.export_app_batch_question)
                .setView(rootView)
                .setNeutralButton(full ? R.string.export_resign : R.string.cancel, (dialog, id) -> {
                    if (full) {
                        if (!sCommonUtils.getBoolean("firstSigning", false, activity)) {
                            new BatchSigningOptionsDialog(packageNames, activity).show();
                        } else {
                            new ResignBatchAPKs(packageNames, activity).execute();
                        }
                    }
                })
                .setPositiveButton(R.string.export_storage, (dialog, id) -> new ExportApp(packageNames, activity).execute()
                ).show();
    }

}