package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKFile;
import com.apk.editor.utils.APKPicker;
import com.apk.editor.utils.AppSettings;
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
    private final List<File> data;
    private final List<String> selectedAPKs;
    private final MaterialButton batchButton;

    public APKsAdapter(List<File> data, List<String> selectedAPKs, MaterialButton batchButton, Activity activity) {
        this.data = data;
        this.selectedAPKs = selectedAPKs;
        this.batchButton = batchButton;
        this.activity = activity;
    }

    @NonNull
    @Override
    public APKsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull APKsAdapter.ViewHolder holder, int position) {
        try {
            APKFile apkItems = new APKFile(this.data.get(position));
            boolean isSelected = selectedAPKs.contains(apkItems.getPath());

            apkItems.load(holder.mAppIcon, holder.mAppName, holder.mPath, holder.mSize, holder.mVersion);

            holder.mDelete.setIcon(sCommonUtils.getDrawable(R.drawable.ic_delete, holder.mDelete.getContext()));
            holder.mDelete.setVisibility(VISIBLE);

            if (isSelected) {
                holder.mCheckBox.setVisibility(VISIBLE);
                holder.mAppIcon.setVisibility(GONE);
                holder.mCheckBox.setChecked(true);
            } else {
                holder.mCheckBox.setVisibility(GONE);
                holder.mAppIcon.setVisibility(VISIBLE);
                holder.mCheckBox.setChecked(false);
            }

            toggleBatchMenu();

            holder.mAppIcon.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    selectedAPKs.add(data.get(currentPos).getAbsolutePath());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.mCheckBox.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    selectedAPKs.remove(data.get(currentPos).getAbsolutePath());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            AppSettings.setSlideInAnimation(holder.itemView, position);

            holder.mDelete.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                File fileToDelete = data.get(currentPos);
                new MaterialAlertDialogBuilder(v.getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(v.getContext().getString(R.string.delete_question, fileToDelete.getName()))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.delete, (dialog, id) -> {
                            new DeleteFile(fileToDelete, activity, false).execute();
                            selectedAPKs.remove(fileToDelete.getAbsolutePath());
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
            batchButton.setVisibility(GONE);
        } else {
            batchButton.setVisibility(VISIBLE);
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
        private final MaterialTextView mAppName, mPath, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mDelete = view.findViewById(R.id.open);
            this.mAppName = view.findViewById(R.id.title);
            this.mPath = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
            this.mVersion = view.findViewById(R.id.version);

            view.setOnLongClickListener(v -> {
                if (APKEditorUtils.isFullVersion(v.getContext())) {
                    if (data.get(getBindingAdapterPosition()).isDirectory()) {
                        new BundleOptionsMenu(data.get(getBindingAdapterPosition()).getPath(), v);
                    } else {
                        APKData.shareFile(data.get(getBindingAdapterPosition()), "application/java-archive", v.getContext());
                    }
                }
                return false;
            });
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            APKFile apkItems = new APKFile(data.get(currentPos));
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (selectedAPKs.contains(apkItems.getPath())) {
                view.post(() -> {
                    selectedAPKs.remove(apkItems.getPath());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                });
                return;
            }

            if (apkItems.getPackageName(view.getContext()) == null) {
                sCommonUtils.toast(view.getContext().getString(R.string.apk_corrupted), view.getContext()).show();
                return;
            }

            if (APKEditorUtils.isFullVersion(view.getContext())) {
                if (apkItems.getName().contains("_aee-signed") && !sCommonUtils.getBoolean("signature_warning", false, view.getContext())) {
                    new SignatureMismatchDialog(view.getContext());
                } else {
                    if (apkItems.isDirectory()) {
                        bundleInstaller(apkItems, view.getContext()).execute();
                    } else {
                        new MaterialAlertDialogBuilder(view.getContext())
                                .setIcon(mAppIcon.getDrawable())
                                .setTitle(view.getContext().getString(R.string.install_question, apkItems.getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) ->
                                        SplitAPKInstaller.installAPK(apkItems, activity)
                                ).show();
                    }
                }
            } else {
                if (apkItems.isDirectory()) {
                    new BundleOptionsMenu(apkItems.getPath(), view);
                } else {
                    APKData.shareFile(apkItems, "application/java-archive", view.getContext());
                }
            }
        }
    }

}