package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.APKPickerAdapter;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Serializables.APKPickerItems;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 22, 2025
 */
public class BundleInstallDialog extends BottomSheetDialog {

    private final APKPickerAdapter adapter = new APKPickerAdapter(new ArrayList<>());

    public BundleInstallDialog(List<APKPickerItems> data, boolean finish, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_bundle_install, null);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialButton select = rootView.findViewById(R.id.select);
        MaterialButton selectAll = rootView.findViewById(R.id.select_all);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(activity, APKExplorer.getSpanCount(activity)));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        adapter.updateData(data);

        setContentView(rootView);
        setCancelable(false);

        selectAll.setOnClickListener(view -> {
            List<APKPickerItems> newData = new ArrayList<>();
            for (APKPickerItems items : data) {
                newData.add(new APKPickerItems(items.getApkFile(), true));
            }
            adapter.updateData(newData);
        });

        cancel.setOnClickListener(v -> {
            if (finish) {
                activity.finish();
            }
            dismiss();
        });

        select.setOnClickListener(v -> new sExecutor() {
            private final List<String> selectedAPKs = new ArrayList<>();
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                for (APKPickerItems apkPickerItems : adapter.getData()) {
                    if (apkPickerItems.isSelected()) {
                        selectedAPKs.add(apkPickerItems.getAPKPath());
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (!selectedAPKs.isEmpty()) {
                    if (selectedAPKs.size() == 1) {
                        if (sAPKUtils.getPackageName(selectedAPKs.get(0), activity) != null) {
                            SplitAPKInstaller.installAPK(finish, new File(selectedAPKs.get(0)), activity);
                        } else {
                            sCommonUtils.toast(R.string.installation_status_bad_apks, activity).show();
                        }
                    } else {
                        SplitAPKInstaller.installSplitAPKs(finish, selectedAPKs, activity);
                    }
                }
                if (finish) {
                    activity.finish();
                }
                dismiss();
            }
        }.execute());

        show();
    }

}