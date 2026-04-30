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
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SerializableItems.APKItems;
import com.apk.editor.utils.SerializableItems.APKPickerItems;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.BundleInstallDialog;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.dialogs.SignatureMismatchDialog;
import com.apk.editor.utils.menus.BundleOptionsMenu;
import com.apk.editor.utils.tasks.DeleteFile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsAdapter extends RecyclerView.Adapter<APKsAdapter.ViewHolder> {

    private final Activity activity;
    private final List<APKItems> data;
    private final List<File> selectedAPKs;
    private final MaterialButton batchButton;
    private final String searchWord;

    public APKsAdapter(List<APKItems> data, List<File> selectedAPKs, MaterialButton batchButton, String searchWord, Activity activity) {
        this.data = data;
        this.selectedAPKs = selectedAPKs;
        this.batchButton = batchButton;
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
            APKItems apkItems = this.data.get(position);
            boolean isSelected = selectedAPKs.contains(apkItems.getAPKFile());

            if (apkItems.getVersionName(holder.mVersion.getContext()) != null) {
                holder.mVersion.setText(apkItems.getVersionName(holder.mVersion.getContext()));
            }

            apkItems.loadAppIcon(holder.mAppIcon);

            if (apkItems.getAppName(holder.mAppName.getContext()) != null) {
                if (searchWord != null && Common.isTextMatched(Objects.requireNonNull(apkItems.getAppName(holder.mAppName.getContext())).toString(), searchWord)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(Objects.requireNonNull(apkItems.getAppName(holder.mAppName.getContext())).toString().replace(searchWord,
                            "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(apkItems.getAppName(holder.mAppName.getContext()));
                }
            } else {
                if (searchWord != null && Common.isTextMatched(apkItems.getName(),searchWord)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(apkItems.getName().replace(searchWord,
                            "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(apkItems.getName());
                }
                holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (apkItems.getPackageName(holder.mAppName.getContext()) == null) {
                holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            holder.mSize.setText(apkItems.getSize(holder.mSize.getContext()));
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mVersion.setVisibility(View.VISIBLE);

            if (isSelected) {
                holder.mCheckBox.setVisibility(View.VISIBLE);
                holder.mAppIcon.setVisibility(View.GONE);
                holder.mCheckBox.setChecked(true);
            } else {
                holder.mCheckBox.setVisibility(View.GONE);
                holder.mAppIcon.setVisibility(View.VISIBLE);
                holder.mCheckBox.setChecked(false);
            }

            toggleBatchMenu();

            holder.mAppIcon.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    selectedAPKs.add(data.get(currentPos).getAPKFile());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.mCheckBox.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    selectedAPKs.remove(data.get(currentPos).getAPKFile());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            AppSettings.setSlideInAnimation(holder.itemView, position);

            holder.mDelete.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                APKItems itemToDelete = data.get(currentPos);
                new MaterialAlertDialogBuilder(v.getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(v.getContext().getString(R.string.delete_question, itemToDelete.getName()))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.delete, (dialog, id) -> {
                            new DeleteFile(itemToDelete.getAPKFile(), activity, false).execute();
                            selectedAPKs.remove(itemToDelete.getAPKFile());
                            data.remove(currentPos);
                            notifyItemRemoved(currentPos);
                            notifyItemRangeChanged(currentPos, data.size());
                            toggleBatchMenu();
                        }).show();
            });
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

    private void toggleBatchMenu() {
        if (selectedAPKs.isEmpty()) {
            batchButton.setVisibility(View.GONE);
        } else {
            batchButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mAppIcon;
        private final MaterialButton mDelete;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mAppName, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mDelete = view.findViewById(R.id.delete);
            this.mAppName = view.findViewById(R.id.title);
            this.mSize = view.findViewById(R.id.size);
            this.mVersion = view.findViewById(R.id.version);

            view.setOnLongClickListener(v -> {
                if (APKEditorUtils.isFullVersion(v.getContext())) {
                    if (data.get(getBindingAdapterPosition()).isDirectory()) {
                        new BundleOptionsMenu(data.get(getBindingAdapterPosition()).getPath(), v);
                    } else {
                        APKData.shareFile(data.get(getBindingAdapterPosition()).getAPKFile(), "application/java-archive", v.getContext());
                    }
                }
                return false;
            });
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            APKItems apkItems = data.get(currentPos);
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (selectedAPKs.contains(apkItems.getAPKFile())) {
                view.post(() -> {
                    selectedAPKs.remove(apkItems.getAPKFile());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                });
                return;
            }

            if (apkItems.getAppName(view.getContext()) == null || apkItems.getPackageName(view.getContext()) == null) {
                sCommonUtils.toast(view.getContext().getString(R.string.apk_corrupted), view.getContext()).show();
                return;
            }

            if (APKEditorUtils.isFullVersion(view.getContext())) {
                if (apkItems.getName().contains("_aee-signed") && !sCommonUtils.getBoolean("signature_warning", false, view.getContext())) {
                    new SignatureMismatchDialog(view.getContext());
                } else {
                    if (apkItems.isDirectory()) {
                        bundleInstaller(apkItems.getAPKFile(), view.getContext()).execute();
                    } else {
                        new MaterialAlertDialogBuilder(view.getContext())
                                .setIcon(mAppIcon.getDrawable())
                                .setTitle(view.getContext().getString(R.string.install_question, apkItems.getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) ->
                                        SplitAPKInstaller.installAPK(apkItems.getAPKFile(), activity)
                                ).show();
                    }
                }
            } else {
                if (apkItems.isDirectory()) {
                    new BundleOptionsMenu(apkItems.getPath(), view);
                } else {
                    APKData.shareFile(apkItems.getAPKFile(), "application/java-archive", view.getContext());
                }
            }
        }
    }

}