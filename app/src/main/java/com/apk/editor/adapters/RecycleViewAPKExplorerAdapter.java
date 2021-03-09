package com.apk.editor.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
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
            holder.mIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mTitle.getContext()));
            holder.mSettings.setVisibility(View.GONE);
        } else if (APKExplorer.isImageFile(data.get(position))) {
            if (APKExplorer.getIconFromPath(data.get(position)) != null) {
                holder.mIcon.setImageURI(APKExplorer.getIconFromPath(data.get(position)));
            } else {
                holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file);
            }
        } else {
            holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file));
            holder.mIcon.setColorFilter(APKEditorUtils.isDarkTheme(holder.mIcon.getContext()) ? holder.mIcon.getContext()
                    .getResources().getColor(R.color.colorWhite) : holder.mIcon.getContext().getResources().getColor(R.color.colorBlack));
        }
        holder.mTitle.setText(new File(data.get(position)).getName());
        holder.mSettings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(holder.mSettings.getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.delete);
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.export);
            menu.add(Menu.NONE, 2, Menu.NONE, R.string.replace);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        new MaterialAlertDialogBuilder(holder.mSettings.getContext())
                                .setMessage(holder.mSettings.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.delete, (dialog, id) -> {
                                    APKEditorUtils.delete(data.get(position));
                                    data.remove(position);
                                    notifyDataSetChanged();
                                }).show();
                        break;
                    case 1:
                        new MaterialAlertDialogBuilder(holder.mSettings.getContext())
                                .setMessage(R.string.export_question)
                                .setNegativeButton(holder.mSettings.getContext().getString(R.string.cancel), (dialog, id) -> {
                                })
                                .setPositiveButton(holder.mSettings.getContext().getString(R.string.export), (dialog, id) -> {
                                    APKEditorUtils.mkdir(holder.mSettings.getContext().getExternalFilesDir("") + "/" + APKExplorer.mAppID);
                                    APKEditorUtils.copy(data.get(position), holder.mSettings.getContext().getExternalFilesDir("") + "/" + APKExplorer.mAppID + "/" + new File(data.get(position)).getName());
                                    new MaterialAlertDialogBuilder(holder.mSettings.getContext())
                                            .setMessage(holder.mSettings.getContext().getString(R.string.export_complete_message, holder.mSettings.getContext().getExternalFilesDir("") + "/" + APKExplorer.mAppID))
                                            .setPositiveButton(holder.mSettings.getContext().getString(R.string.cancel), (dialog1, id1) -> {
                                            }).show();
                                }).show();
                        break;
                    case 2:
                        if (!APKEditorUtils.isWritePermissionGranted(holder.mSettings.getContext())) {
                            ActivityCompat.requestPermissions((Activity) holder.mSettings.getContext(), new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else if (Build.VERSION.SDK_INT >= 29 && !APKEditorUtils.getBoolean("storage_message", false, holder.mSettings.getContext())) {
                            new MaterialAlertDialogBuilder(holder.mSettings.getContext())
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.app_name)
                                    .setCancelable(false)
                                    .setMessage(holder.mSettings.getContext().getString(R.string.external_storage_permission_message) +
                                            "\n\nadb shell appops set --uid com.apk.editor MANAGE_EXTERNAL_STORAGE allow")
                                    .setPositiveButton(holder.mSettings.getContext().getString(R.string.got_it), (dialog1, id1) -> {
                                        APKEditorUtils.saveBoolean("storage_message", true, holder.mSettings.getContext());
                                        APKExplorer.mFileToReplace = data.get(position);
                                        Intent filePicker = new Intent(holder.mSettings.getContext(), FilePickerActivity.class);
                                        holder.mSettings.getContext().startActivity(filePicker);
                                    }).show();
                        } else {
                            APKExplorer.mFileToReplace = data.get(position);
                            Intent filePicker = new Intent(holder.mSettings.getContext(), FilePickerActivity.class);
                            holder.mSettings.getContext().startActivity(filePicker);
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
        private AppCompatImageButton mIcon, mSettings;
        private MaterialTextView mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mSettings = view.findViewById(R.id.settings);
            this.mTitle = view.findViewById(R.id.title);
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