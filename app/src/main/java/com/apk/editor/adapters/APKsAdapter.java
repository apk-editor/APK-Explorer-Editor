package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKPicker;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SerializableItems.APKItems;
import com.apk.editor.utils.SerializableItems.APKPickerItems;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.BundleInstallDialog;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.dialogs.SignatureMismatchDialog;
import com.apk.editor.utils.menus.BundleOptionsMenu;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsAdapter extends RecyclerView.Adapter<APKsAdapter.ViewHolder> {

    private final Activity activity;
    private final List<APKItems> data;
    private final String searchWord;

    public APKsAdapter(List<APKItems> data, String searchWord, Activity activity) {
        this.data = data;
        this.searchWord = searchWord;
        this.activity = activity;
    }

    @NonNull
    @Override
    public APKsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull APKsAdapter.ViewHolder holder, int position) {
        try {
            if (data.get(position).getVersionName(holder.mVersion.getContext()) != null) {
                holder.mVersion.setText(data.get(position).getVersionName(holder.mVersion.getContext()));
            }

            this.data.get(position).loadAppIcon(holder.mAppIcon);

            if (data.get(position).getAppName(holder.mAppName.getContext()) != null) {
                if (searchWord != null && Common.isTextMatched(Objects.requireNonNull(data.get(position).getAppName(holder.mAppName.getContext())).toString(), searchWord)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(Objects.requireNonNull(data.get(position).getAppName(holder.mAppName.getContext())).toString().replace(searchWord,
                            "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(data.get(position).getAppName(holder.mAppName.getContext()));
                }
            } else {
                if (searchWord != null && Common.isTextMatched(data.get(position).getName(),searchWord)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(data.get(position).getName().replace(searchWord,
                            "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(data.get(position).getName());
                }
                holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                holder.mCard.setOnClickListener(v -> sCommonUtils.snackBar(v, v.getContext().getString(R.string.apk_corrupted)).show());
            }
            if (data.get(position).getPackageName(holder.mAppName.getContext()) == null) {
                holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                holder.mCard.setOnClickListener(v -> sCommonUtils.snackBar(v, v.getContext().getString(R.string.apk_corrupted)).show());
            }
            holder.mSize.setText(data.get(position).getSize(holder.mSize.getContext()));
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mVersion.setVisibility(View.VISIBLE);

            holder.mCard.setOnClickListener(v -> {
                if (APKEditorUtils.isFullVersion(v.getContext())) {
                    if (data.get(position).getName().contains("_aee-signed") && !sCommonUtils.getBoolean("signature_warning", false, v.getContext())) {
                        new SignatureMismatchDialog(v.getContext());
                    } else {
                        if (data.get(position).isDirectory()) {
                            bundleInstaller(data.get(position).getAPKFile(), v.getContext()).execute();
                        } else {
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setIcon(holder.mAppIcon.getDrawable())
                                .setTitle(v.getContext().getString(R.string.install_question, data.get(position).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) ->
                                        SplitAPKInstaller.installAPK(data.get(position).getAPKFile(), activity)
                                ).show();
                        }
                    }
                } else {
                    if (data.get(position).isDirectory()) {
                        new BundleOptionsMenu(data.get(position).getPath(), v);
                    } else {
                        APKData.shareFile(data.get(position).getAPKFile(), "application/java-archive", v.getContext());
                    }
                }
            });

            holder.mCard.setOnLongClickListener(v -> {
                if (APKEditorUtils.isFullVersion(v.getContext())) {
                    if (data.get(position).isDirectory()) {
                        new BundleOptionsMenu(data.get(position).getPath(), v);
                    } else {
                        APKData.shareFile(data.get(position).getAPKFile(), "application/java-archive", v.getContext());
                    }
                }
                return false;
            });

            holder.mDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(v.getContext().getString(R.string.delete_question, data.get(position).getName()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.delete, (dialog, id) -> {
                        sFileUtils.delete(data.get(position).getAPKFile());
                        data.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, data.size());
                    }).show());
        } catch (NullPointerException ignored) {
        }
    }

    private sExecutor bundleInstaller(File apkFile, Context context) {
        return new sExecutor() {
            private final List<APKPickerItems> mAPKs = new ArrayList<>();
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setTitle(context.getString(R.string.loading));
                mProgressDialog.setIcon(R.mipmap.ic_launcher);
                mProgressDialog.setIndeterminate(true);
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    mProgressDialog.show();
                }
            }

            @Override
            public void doInBackground() {
                mProgressDialog.setMax(Objects.requireNonNull(apkFile.listFiles()).length);
                for (File files : Objects.requireNonNull(apkFile.listFiles())) {
                    if (files.isFile() && files.getName().endsWith("apk")) {
                        mAPKs.add(new APKPickerItems(files, APKPicker.isSelectedAPK(files, context)));
                    }
                    mProgressDialog.updateProgress(1);
                }
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {}
                new BundleInstallDialog(mAPKs, false, activity);
            }
        };
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mAppIcon;
        private final MaterialButton mDelete;
        private final MaterialCardView mCard;
        private final MaterialTextView mAppName, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            this.mCard = view.findViewById(R.id.card);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mDelete = view.findViewById(R.id.delete);
            this.mAppName = view.findViewById(R.id.title);
            this.mSize = view.findViewById(R.id.size);
            this.mVersion = view.findViewById(R.id.version);
        }
    }

}