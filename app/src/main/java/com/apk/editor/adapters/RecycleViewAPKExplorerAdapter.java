package com.apk.editor.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.FilePickerActivity;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Projects;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class RecycleViewAPKExplorerAdapter extends RecyclerView.Adapter<RecycleViewAPKExplorerAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private static List<String> data;

    public RecycleViewAPKExplorerAdapter(List<String> data) {
        RecycleViewAPKExplorerAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewAPKExplorerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apkexplorer, parent, false);
        return new RecycleViewAPKExplorerAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewAPKExplorerAdapter.ViewHolder holder, int position) {
        if (new File(data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(holder.mTitle.getContext().getResources().getDrawable(R.drawable.ic_folder));
            holder.mIcon.setBackground(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_circle));
            holder.mIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mTitle.getContext()));
            holder.mSettings.setVisibility(View.GONE);
            holder.mDescription.setVisibility(View.GONE);
        } else if (APKExplorer.isImageFile(data.get(position))) {
            if (APKExplorer.getIconFromPath(data.get(position)) != null) {
                holder.mIcon.setImageURI(APKExplorer.getIconFromPath(data.get(position)));
            } else {
                APKExplorer.setIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file), holder.mIcon.getContext());
            }
        } else if (data.get(position).endsWith(".apk")) {
            if (APKData.getAppIcon(data.get(position), holder.mIcon.getContext()) != null) {
                holder.mIcon.setImageDrawable(APKData.getAppIcon(data.get(position), holder.mIcon.getContext()));
            } else {
                APKExplorer.setIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_android), holder.mIcon.getContext());
            }
        } else {
            if (data.get(position).endsWith(".xml")) {
                APKExplorer.setIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_xml), holder.mIcon.getContext());
            } else {
                APKExplorer.setIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file), holder.mIcon.getContext());
            }
        }
        holder.mTitle.setText(new File(data.get(position)).getName());
        holder.mDescription.setText(AppData.getAPKSize(data.get(position)));
        holder.mSettings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            if (APKEditorUtils.isFullVersion(v.getContext())) {
                menu.add(Menu.NONE, 0, Menu.NONE, R.string.delete);
            }
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.export);
            if (APKEditorUtils.isFullVersion(v.getContext())) {
                menu.add(Menu.NONE, 2, Menu.NONE, R.string.replace);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setMessage(v.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.delete, (dialog, id) -> {
                                    APKEditorUtils.delete(data.get(position));
                                    data.remove(position);
                                    notifyDataSetChanged();
                                }).show();
                        break;
                    case 1:
                        if (!APKEditorUtils.isWritePermissionGranted(v.getContext())) {
                            ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            APKEditorUtils.snackbar(v, v.getContext().getString(R.string.permission_denied_message));
                        } else {
                            new MaterialAlertDialogBuilder(v.getContext())
                                    .setMessage(R.string.export_question)
                                    .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                                    })
                                    .setPositiveButton(v.getContext().getString(R.string.export), (dialog, id) -> {
                                        if (Build.VERSION.SDK_INT >= 30 && APKExplorer.isPermissionDenied() && Projects.getExportPath(v.getContext())
                                                .startsWith(Environment.getExternalStorageDirectory().toString())) {
                                            new MaterialAlertDialogBuilder(v.getContext())
                                                    .setIcon(R.mipmap.ic_launcher)
                                                    .setTitle(v.getContext().getString(R.string.important))
                                                    .setMessage(v.getContext().getString(R.string.file_permission_request_message, v.getContext().getString(R.string.app_name)))
                                                    .setCancelable(false)
                                                    .setNegativeButton(v.getContext().getString(R.string.cancel), (dialogInterface, i) -> {
                                                    })
                                                    .setPositiveButton(v.getContext().getString(R.string.grant), (dialog1, id1) -> APKExplorer.requestPermission((Activity) v.getContext())).show();
                                        } else {
                                            APKEditorUtils.mkdir(Projects.getExportPath(v.getContext()) + "/" + APKExplorer.mAppID);
                                            APKEditorUtils.copy(data.get(position), Projects.getExportPath(v.getContext()) + "/" + APKExplorer.mAppID + "/" + new File(data.get(position)).getName());
                                            new MaterialAlertDialogBuilder(v.getContext())
                                                    .setMessage(v.getContext().getString(R.string.export_complete_message, Projects.getExportPath(v.getContext()) + "/" + APKExplorer.mAppID))
                                                    .setPositiveButton(v.getContext().getString(R.string.cancel), (dialog1, id1) -> {
                                                    }).show();
                                        }
                                    }).show();
                        }
                        break;
                    case 2:
                        if (!APKEditorUtils.isWritePermissionGranted(v.getContext())) {
                            ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            APKEditorUtils.snackbar(v, v.getContext().getString(R.string.permission_denied_message));
                        } else {
                            APKExplorer.mFileToReplace = data.get(position);
                            Intent filePicker = new Intent(v.getContext(), FilePickerActivity.class);
                            v.getContext().startActivity(filePicker);
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon, mSettings;
        private final MaterialTextView mDescription, mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mSettings = view.findViewById(R.id.settings);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecycleViewAPKExplorerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}