package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.activities.TextEditorActivity;
import com.apk.editor.activities.TextViewActivity;
import com.apk.editor.activities.XMLEditorActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.dialogs.ResViewerDialog;
import com.apk.editor.utils.tasks.DeleteFiles;
import com.apk.editor.utils.tasks.ExportToStorage;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExplorerAdapter extends RecyclerView.Adapter<APKExplorerAdapter.ViewHolder> {

    private final Activity activity;
    private final ActivityResultLauncher<Intent> activityResultLauncher;
    private static ClickListener clickListener;
    private final List<File> files;
    private final List<String> data;
    private final String backupFilePath, packageName;

    public APKExplorerAdapter(List<String> data, ActivityResultLauncher<Intent> activityResultLauncher, List<File> files, String packageName, String backupFilePath, Activity activity) {
        this.data = data;
        this.activityResultLauncher = activityResultLauncher;
        this.files = files;
        this.packageName = packageName;
        this.backupFilePath = backupFilePath;
        this.activity = activity;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apkexplorer, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String explorerItem = this.data.get(position);
        boolean isSelected = files.contains(new File(explorerItem));

        if (new File(explorerItem).isDirectory()) {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(holder.mTitle.getContext(), R.drawable.ic_folder));
            holder.mDescription.setVisibility(GONE);
            holder.mIcon.setClickable(false);
        } else {
            if (APKExplorer.isImageFile(explorerItem)) {
                if (APKExplorer.getIconFromPath(explorerItem) != null) {
                    holder.mIcon.setImageURI(APKExplorer.getIconFromPath(explorerItem));
                } else {
                    APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_file), holder.mIcon.getContext());
                }
            } else if (explorerItem.endsWith(".apk")) {
                holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(explorerItem, holder.mIcon.getContext()));
            } else if (explorerItem.contains("classes") && explorerItem.endsWith(".dex")) {
                APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_classes), holder.mIcon.getContext());
            } else if (explorerItem.endsWith(".arsc")) {
                APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_res), holder.mIcon.getContext());
            } else {
                if (explorerItem.endsWith(".xml")) {
                    APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), explorerItem.endsWith("AndroidManifest.xml") ? R.drawable.ic_manifest : R.drawable.ic_xml), holder.mIcon.getContext());
                } else {
                    APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_file), holder.mIcon.getContext());
                }
            }
            holder.mIcon.setClickable(true);

            if (isSelected) {
                holder.mCheckBox.setVisibility(View.VISIBLE);
                holder.mIcon.setVisibility(View.GONE);
                holder.mCheckBox.setChecked(true);
            } else {
                holder.mCheckBox.setVisibility(View.GONE);
                holder.mIcon.setVisibility(View.VISIBLE);
                holder.mCheckBox.setChecked(false);
            }

            holder.mIcon.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    files.add(new File(data.get(currentPos)));
                    notifyItemChanged(currentPos);
                }
            });

            holder.mCheckBox.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    files.remove(new File(data.get(currentPos)));
                    notifyItemChanged(currentPos);
                }
            });
        }

        holder.mTitle.setText(new File(explorerItem).getName());
        holder.mDescription.setText(APKExplorer.getFormattedFileSize(new File(explorerItem), holder.mDescription.getContext()));
        holder.mDescription.setVisibility(VISIBLE);
        AppSettings.setSlideInAnimation(holder.itemView, position);
    }

    private sSingleItemDialog longClickDialog(int position, Context context) {
        return new sSingleItemDialog(0, null, new String[]{
                context.getString(R.string.delete),
                context.getString(R.string.export),
                context.getString(R.string.replace)
        }, context) {

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onItemSelected(int itemPosition) {
                if (itemPosition == 0) {
                    new MaterialAlertDialogBuilder(context)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(context.getString(R.string.delete_question, new File(data.get(position)).getName()))
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setPositiveButton(R.string.delete, (dialog, id) -> new DeleteFiles(new File(data.get(position)), null, backupFilePath, context) {
                                @Override
                                public void onPostExecute() {
                                    data.remove(position);
                                    files.remove(new File(data.get(position)));
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, data.size());
                                }
                            }.execute()
                            ).show();
                } else if (itemPosition == 1) {
                    new MaterialAlertDialogBuilder(context)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.export_question)
                            .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(context.getString(R.string.export), (dialog, id) -> {
                                if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
                                    sPermissionUtils.requestPermission(
                                            new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            }, activity);
                                } else {
                                    new ExportToStorage(new File(data.get(position)), null, packageName, context).execute();
                                }
                            }).show();
                } else {
                    Common.setFileToReplace(data.get(position));
                    Intent replace = new Intent(Intent.ACTION_GET_CONTENT);
                    replace.setType("*/*");
                    activityResultLauncher.launch(replace);
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mDescription, mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);

            view.setOnLongClickListener(v -> {
                if (new File(data.get(getBindingAdapterPosition())).isDirectory() || !APKEditorUtils.isFullVersion(view.getContext())) {
                    return false;
                }
                longClickDialog(getBindingAdapterPosition(), v.getContext()).show();
                return true;
            });
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            String filePath = data.get(currentPos);
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (new File(filePath).isDirectory() || new File(filePath).isFile() && filePath.endsWith(".dex")) {
                clickListener.onItemClick(data.get(currentPos));
            } else {
                if (files.contains(new File(filePath))) {
                    view.post(() -> {
                        files.remove(new File(filePath));
                        notifyItemChanged(currentPos);
                    });
                    return;
                }
                if (APKExplorer.isTextFile(filePath)) {
                    Intent intent;
                    if (APKEditorUtils.isFullVersion(view.getContext())) {
                        intent = new Intent(view.getContext(), TextEditorActivity.class);
                        intent.putExtra(TextEditorActivity.PATH_INTENT, filePath);
                        intent.putExtra(TextEditorActivity.BACKUP_PATH_INTENT, backupFilePath);
                    } else {
                        intent = new Intent(view.getContext(), TextViewActivity.class);
                        intent.putExtra(TextViewActivity.PATH_INTENT, filePath);
                    }
                    view.getContext().startActivity(intent);
                } else if (APKExplorer.isImageFile(filePath)) {
                    Intent imageView = new Intent(view.getContext(), ImageViewActivity.class);
                    imageView.putExtra(ImageViewActivity.PATH_INTENT, filePath);
                    imageView.putExtra(ImageViewActivity.PACKAGE_NAME_INTENT, packageName);
                    view.getContext().startActivity(imageView);
                } else if (filePath.endsWith(".xml")) {
                    Intent xmlEditor = new Intent(view.getContext(), XMLEditorActivity.class);
                    xmlEditor.putExtra(XMLEditorActivity.PATH_INTENT, filePath);
                    xmlEditor.putExtra(XMLEditorActivity.RESOURCE_PATH_INTENT, backupFilePath.replace("/.aeeBackup/appData", "/resources.arsc"));
                    view.getContext().startActivity(xmlEditor);
                } else if (filePath.endsWith(".RSA")) {
                    Intent rsaCertificate = new Intent(view.getContext(), TextViewActivity.class);
                    rsaCertificate.putExtra(TextViewActivity.PATH_INTENT, filePath);
                    view.getContext().startActivity(rsaCertificate);
                } else if (filePath.endsWith("resources.arsc")) {
                    new ResViewerDialog(filePath, activity);
                } else {
                    new MaterialAlertDialogBuilder(view.getContext())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(view.getContext().getString(R.string.unknown_file_message, new File(filePath).getName()))
                            .setNeutralButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setNegativeButton(view.getContext().getString(R.string.export), (dialog, id) -> {
                                if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, view.getContext())) {
                                    sPermissionUtils.requestPermission(
                                            new String[] {
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            }, activity);
                                } else {
                                    new ExportToStorage(new File(filePath), null, packageName, view.getContext()).execute();
                                }
                            })
                            .setPositiveButton(view.getContext().getString(R.string.open_as_text), (dialog1, id1) -> {
                                Intent intent;
                                if (APKEditorUtils.isFullVersion(view.getContext())) {
                                    intent = new Intent(view.getContext(), TextEditorActivity.class);
                                    intent.putExtra(TextEditorActivity.PATH_INTENT, filePath);
                                    intent.putExtra(TextEditorActivity.BACKUP_PATH_INTENT, backupFilePath);
                                } else {
                                    intent = new Intent(view.getContext(), TextViewActivity.class);
                                    intent.putExtra(TextViewActivity.PATH_INTENT, filePath);
                                }
                                view.getContext().startActivity(intent);
                            }).show();
                }
            }
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        APKExplorerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(String filePath);
    }

}