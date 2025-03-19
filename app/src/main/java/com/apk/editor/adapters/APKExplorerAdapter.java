package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
import com.apk.editor.utils.Common;
import com.apk.editor.utils.tasks.DeleteFile;
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
    public APKExplorerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apkexplorer, parent, false);
        return new APKExplorerAdapter.ViewHolder(rowItem);
    }

    @SuppressLint({"NotifyDataSetChanged", "StringFormatInvalid"})
    @Override
    public void onBindViewHolder(@NonNull APKExplorerAdapter.ViewHolder holder, int position) {
        if (new File(data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(holder.mTitle.getContext(), R.drawable.ic_folder));
            holder.mCheckBox.setVisibility(View.GONE);
            holder.mDescription.setVisibility(View.GONE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);
            if (APKExplorer.isImageFile(data.get(position))) {
                if (APKExplorer.getIconFromPath(data.get(position)) != null) {
                    holder.mIcon.setImageURI(APKExplorer.getIconFromPath(data.get(position)));
                } else {
                    APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_file), holder.mIcon.getContext());
                }
            } else if (data.get(position).endsWith(".apk")) {
                holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position), holder.mIcon.getContext()));
            } else {
                if (data.get(position).endsWith(".xml")) {
                    APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_xml), holder.mIcon.getContext());
                } else {
                    APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_file), holder.mIcon.getContext());
                }
            }
            if (APKEditorUtils.isFullVersion(holder.mLayout.getContext())) {
                holder.mLayout.setOnLongClickListener(v -> {
                    longClickDilog(position, v.getContext()).show();
                    return true;
                });
            }

            holder.mCheckBox.setOnClickListener(v -> {
                if (holder.mCheckBox.isChecked()) {
                    files.add(new File(data.get(position)));
                } else {
                    files.remove(new File(data.get(position)));
                }
            });
        }
        holder.mTitle.setText(new File(data.get(position)).getName());
        holder.mDescription.setText(APKExplorer.getFormattedFileSize(new File(data.get(position))));
        holder.mDescription.setVisibility(View.VISIBLE);
    }

    private sSingleItemDialog longClickDilog(int position, Context context) {
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
                            .setPositiveButton(R.string.delete, (dialog, id) -> new DeleteFile(new File(data.get(position)), null, backupFilePath, context) {

                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onPostExecute() {
                                    data.remove(position);
                                    notifyDataSetChanged();
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
                                if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
                                    sPermissionUtils.requestPermission(
                                            new String[]{
                                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final FrameLayout mLayout;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mDescription, mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mLayout = view.findViewById(R.id.layout_main);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            String filePath = data.get(getAdapterPosition());
            if (new File(filePath).isDirectory() || new File(filePath).isFile() && filePath.endsWith(".dex")) {
                clickListener.onItemClick(data.get(getAdapterPosition()));
            } else {
                if (APKExplorer.isTextFile(filePath)) {
                    Intent intent;
                    if (APKEditorUtils.isFullVersion(view.getContext())) {
                        intent = new Intent(view.getContext(), TextEditorActivity.class);
                        intent.putExtra(TextEditorActivity.PATH_INTENT, filePath);
                        intent.putExtra(TextEditorActivity.PACKAGE_NAME_INTENT, packageName);
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
                    view.getContext().startActivity(xmlEditor);
                } else if (filePath.endsWith(".RSA")) {
                    Intent rsaCertificate = new Intent(view.getContext(), TextViewActivity.class);
                    rsaCertificate.putExtra(TextViewActivity.PATH_INTENT, filePath);
                    view.getContext().startActivity(rsaCertificate);
                } else if (filePath.endsWith("resources.arsc")) {
                    new MaterialAlertDialogBuilder(view.getContext())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.unsupported_file)
                            .setMessage(R.string.unsupported_file_arsc)
                            .setPositiveButton(R.string.cancel, (dialog, id) -> {
                            }).show();
                } else {
                    new MaterialAlertDialogBuilder(view.getContext())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(view.getContext().getString(R.string.unknown_file_message, new File(filePath).getName()))
                            .setNeutralButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setNegativeButton(view.getContext().getString(R.string.export), (dialog, id) -> {
                                if (sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, view.getContext())) {
                                    sPermissionUtils.requestPermission(
                                            new String[] {
                                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                                    intent.putExtra(TextEditorActivity.PACKAGE_NAME_INTENT, packageName);
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