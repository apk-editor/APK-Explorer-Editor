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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.activities.TextEditorActivity;
import com.apk.editor.activities.TextViewActivity;
import com.apk.editor.activities.XMLEditorActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Serializables.MenuItems;
import com.apk.editor.utils.dialogs.BottomMenuDialog;
import com.apk.editor.utils.dialogs.ResViewerDialog;
import com.apk.editor.utils.dialogs.UnsupportedFileDialog;
import com.apk.editor.utils.tasks.DeleteFiles;
import com.apk.editor.utils.tasks.ExportToStorage;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExplorerAdapter extends RecyclerView.Adapter<APKExplorerAdapter.ViewHolder> {

    private final Activity activity;
    private final List<File> files;
    private final List<String> data;
    private final OnItemClickListener clickListener;
    private final String backupFilePath, packageName;

    public APKExplorerAdapter(List<String> data, List<File> files, String packageName, String backupFilePath, OnItemClickListener clickListener, Activity activity) {
        this.data = data;
        this.files = files;
        this.packageName = packageName;
        this.backupFilePath = backupFilePath;
        this.clickListener = clickListener;
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
            if (APKExplorer.isImageFile(explorerItem) && APKExplorer.getIconFromPath(explorerItem) != null) {
                holder.mIcon.setImageURI(APKExplorer.getIconFromPath(explorerItem));
            } else if (explorerItem.endsWith(".apk") && sAPKUtils.getAPKIcon(explorerItem, holder.mIcon.getContext()) != null) {
                holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(explorerItem, holder.mIcon.getContext()));
            } else {
                holder.mIcon.setImageResource(APKExplorer.getIconResource(explorerItem));
            }
            holder.mDescription.setVisibility(VISIBLE);
            holder.mIcon.setClickable(true);

            if (isSelected) {
                holder.mCheckBox.setVisibility(VISIBLE);
                holder.mIcon.setVisibility(View.GONE);
                holder.mCheckBox.setChecked(true);
            } else {
                holder.mCheckBox.setVisibility(View.GONE);
                holder.mIcon.setVisibility(VISIBLE);
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
        AppSettings.setSlideInAnimation(holder.mIcon, position);
    }

    public void updateData(List<String> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return data.size();
            }

            @Override
            public int getNewListSize() {
                return newData != null ? newData.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(data.get(oldItemPosition), newData.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                String oldItem = data.get(oldItemPosition);
                String newItem = newData.get(newItemPosition);

                return Objects.equals(oldItem, newItem);
            }
        });

        this.data.clear();
        if (newData != null) {
            this.data.addAll(newData);
        }

        diffResult.dispatchUpdatesTo(this);
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
                longClickDialog(getBindingAdapterPosition(), v.getContext());
                return true;
            });
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            String filePath = data.get(currentPos);
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (files.contains(new File(filePath))) {
                view.post(() -> {
                    files.remove(new File(filePath));
                    notifyItemChanged(currentPos);
                });
                return;
            }

            if (new File(filePath).isDirectory() || new File(filePath).isFile() && filePath.endsWith(".dex")) {
                clickListener.onItemClick(data.get(currentPos), false);
            } else if (APKExplorer.isTextFile(filePath)) {
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
                new UnsupportedFileDialog(this.mIcon.getDrawable(), new File(filePath).getName(), view.getContext().getString(R.string.unknown_file_message, new File(filePath).getName()), view.getContext()) {
                    @Override
                    public void onNegativeAction() {
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
                    }

                    @Override
                    public void onPositiveAction() {
                        if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, view.getContext())) {
                            sPermissionUtils.requestPermission(
                                    new String[] {
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, activity);
                        } else {
                            new ExportToStorage(new File(filePath), null, packageName, view.getContext()).execute();
                        }
                    }
                };
            }
        }

        private void longClickDialog(int position, Context context) {
            List<MenuItems> menuItem = new CopyOnWriteArrayList<>();
            menuItem.add(new MenuItems(R.drawable.ic_delete, context.getString(R.string.delete), 0));
            menuItem.add(new MenuItems(R.drawable.ic_export, context.getString(R.string.export), 1));
            menuItem.add(new MenuItems(R.drawable.ic_reset, context.getString(R.string.replace), 2));

            new BottomMenuDialog(menuItem, APKExplorer.drawableToBitmap(this.mIcon.getDrawable()), this.mTitle.getText().toString().trim(), context) {
                @SuppressLint("StringFormatInvalid")
                @Override
                public void onMenuItemClicked(int id) {
                    switch (id) {
                        case 0:
                            new DeleteFiles(new File(data.get(position)), null, backupFilePath, context) {
                                @Override
                                public void onPostExecute() {
                                    data.remove(position);
                                    files.remove(new File(data.get(position)));
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, data.size());
                                }
                            }.execute();
                            break;
                        case 1:
                            if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
                                sPermissionUtils.requestPermission(
                                        new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        }, activity);
                            } else {
                                new ExportToStorage(new File(data.get(position)), null, packageName, context).execute();
                            }
                            break;
                        case 2:
                            clickListener.onItemClick(data.get(position), true);
                            break;
                    }
                }
            };
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String filePath, boolean replace);
    }

}