package com.apk.editor.utils.dialogs;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.APKPickerAdapter;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.SerializableItems.APKPickerItems;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 22, 2025
 */
public class BundleInstallDialog extends MaterialAlertDialogBuilder {

    public BundleInstallDialog(List<APKPickerItems> data, boolean finish, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_recyclerview, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(activity, APKExplorer.getSpanCount(activity)));
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new APKPickerAdapter(data));

        setView(rootView);
        setIcon(R.mipmap.ic_launcher);
        setTitle(R.string.select_apk);
        setCancelable(false);
        setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
            if (finish) {
                activity.finish();
            }
        });
        setPositiveButton(R.string.select, (dialogInterface, i) ->
                new sExecutor() {
                    private final List<String> selectedAPKs = new ArrayList<>();
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        for (APKPickerItems apkPickerItems : data) {
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
                    }
                }.execute());
        show();
    }
}