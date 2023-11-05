package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.FilePickerActivity;
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
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExplorerAdapter extends RecyclerView.Adapter<APKExplorerAdapter.ViewHolder> {

    private static ActivityResultLauncher<Intent> mActivityResultLauncher;
    private static ClickListener clickListener;
    private static List<String> data;

    public APKExplorerAdapter(List<String> data, ActivityResultLauncher<Intent> activityResultLauncher) {
        APKExplorerAdapter.data = data;
        APKExplorerAdapter.mActivityResultLauncher = activityResultLauncher;
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
            if (sThemeUtils.isDarkTheme(holder.mIcon.getContext())) {
                holder.mIcon.setBackground(ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_circle));
            }
            holder.mIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mTitle.getContext()));
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
            holder.mLayout.setOnLongClickListener(v -> {
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
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.app_name)
                                    .setMessage(v.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                    })
                                    .setPositiveButton(R.string.delete, (dialog, id) -> deleteFile(position, v.getContext())
                                    ).show();
                            break;
                        case 1:
                            new MaterialAlertDialogBuilder(v.getContext())
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.app_name)
                                    .setMessage(R.string.export_question)
                                    .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                                    })
                                    .setPositiveButton(v.getContext().getString(R.string.export), (dialog, id) -> {
                                        if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext())) {
                                            sPermissionUtils.requestPermission(
                                                    new String[] {
                                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                    }, (Activity) v.getContext());
                                        } else {
                                            new ExportToStorage(new File(data.get(position)), null, Common.getAppID(), v.getContext()).execute();
                                        }
                                    }).show();
                            break;
                        case 2:
                            Common.setFileToReplace(data.get(position));
                            if (Build.VERSION.SDK_INT >= 29) {
                                Intent replace = new Intent(Intent.ACTION_GET_CONTENT);
                                replace.setType("*/*");
                                mActivityResultLauncher.launch(replace);
                            } else {
                                Intent filePicker = new Intent(v.getContext(), FilePickerActivity.class);
                                v.getContext().startActivity(filePicker);
                            }
                            break;
                    }
                    return false;
                });
                popupMenu.show();
                return true;
            });

            holder.mCheckBox.setOnClickListener(v -> {
                if (holder.mCheckBox.isChecked()) {
                    Common.addToFilesList(new File(data.get(position)));
                } else {
                    Common.removeFromFilesList(new File(data.get(position)));
                }
            });
        }
        holder.mTitle.setText(new File(data.get(position)).getName());
        holder.mDescription.setText(sAPKUtils.getAPKSize(new File(data.get(position)).getName().length()));
    }

    private void deleteFile(int position, Context context) {
        new DeleteFile(new File(data.get(position)), context) {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onPostExecute() {
                data.remove(position);
                notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        APKExplorerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}