package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.BatchOptionsAdapter;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.Serializables.BatchItems;
import com.apk.editor.utils.tasks.ExportApp;
import com.apk.editor.utils.tasks.ResignBatchAPKs;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 30, 2023
 */
public abstract class BatchOptionsDialog extends BottomSheetDialog {

    private List<BatchItems> data;

    public BatchOptionsDialog(List<String> packageNames, boolean checked, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_batchoptions, null);
        LinearLayoutCompat linearLayoutCompat = rootView.findViewById(R.id.select_all_layout);
        MaterialButton buttonLeft = rootView.findViewById(R.id.button_left);
        MaterialButton buttonRight = rootView.findViewById(R.id.button_right);
        MaterialCheckBox checkBox = rootView.findViewById(R.id.checkbox);

        checkBox.setChecked(checked);
        buttonLeft.setText(getNeutralButtonTitle(activity));
        buttonLeft.setIconResource(getNeutralButtonIcon(activity));
        buttonRight.setIconResource(getPositiveButtonIcon(activity));
        buttonRight.setText(getPositiveButtonTitle(activity));
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        new sExecutor() {
            @Override
            public void onPreExecute() {
            }

            private List<BatchItems> getData() {
                List<BatchItems> data = new CopyOnWriteArrayList<>();
                for (String packageName : packageNames) {
                    data.add(new BatchItems(packageName, true));
                }
                return data;
            }

            @Override
            public void doInBackground() {
                data = getData();
            }

            @Override
            public void onPostExecute() {
                BatchOptionsAdapter adapter = new BatchOptionsAdapter(data);
                recyclerView.setAdapter(adapter);
            }
        }.execute();

        linearLayoutCompat.setOnClickListener(view -> {
            selectAllLister(checkBox.isChecked());
            dismiss();
        });

        setContentView(rootView);

        buttonLeft.setOnClickListener(v -> {
            if (isFullVersion(activity) && getExportOption(activity) == null) {
                new ExportApp(data, activity).execute();
            }
            dismiss();
        });

        buttonRight.setOnClickListener(v -> {
            if (isFullVersion(activity) && (getExportOption(activity) == null || sCommonUtils.getString(
                    "exportAPKs", null, activity).equals(activity.getString(R.string.export_resign)))) {
                resign(activity);
            } else {
                new ExportApp(data, activity).execute();
            }
            dismiss();
        });

        show();
    }

    private static boolean isFullVersion(Activity activity) {
        return APKEditorUtils.isFullVersion(activity);
    }

    private static int getNeutralButtonIcon(Activity activity) {
        if (isFullVersion(activity) && getExportOption(activity) == null) {
            return R.drawable.ic_export;
        } else {
            return R.drawable.ic_cancel;
        }
    }

    private static int getNeutralButtonTitle(Activity activity) {
        if (isFullVersion(activity) && getExportOption(activity) == null) {
            return R.string.export_storage;
        } else {
            return R.string.cancel;
        }
    }

    private static int getPositiveButtonIcon(Activity activity) {
        if (isFullVersion(activity)) {
            if (getExportOption(activity) == null) {
                return R.drawable.ic_key;
            } else if (getExportOption(activity).equals(activity.getString(R.string.export_storage))) {
                return R.drawable.ic_export;
            } else {
                return R.drawable.ic_key;
            }
        } else {
            return R.drawable.ic_export;
        }
    }

    private static int getPositiveButtonTitle(Activity activity) {
        if (isFullVersion(activity)) {
            if (getExportOption(activity) == null) {
                return R.string.export_resign;
            } else if (getExportOption(activity).equals(activity.getString(R.string.export_storage))) {
                return R.string.export_storage;
            } else {
                return R.string.export_resign;
            }
        } else {
            return R.string.export_storage;
        }
    }

    private static String getExportOption(Activity activity) {
        return sCommonUtils.getString("exportAPKs", null, activity);
    }

    private void resign(Activity activity) {
        if (!sCommonUtils.getBoolean("firstSigning", false, activity)) {
            new BatchSigningOptionsDialog(data, activity).show();
        } else {
            new ResignBatchAPKs(data, activity).execute();
        }
    }

    public abstract void selectAllLister(boolean checked);

}