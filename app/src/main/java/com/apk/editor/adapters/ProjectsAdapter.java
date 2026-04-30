package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Projects;
import com.apk.editor.utils.tasks.DeleteFile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.text.DateFormat;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 06, 2021
 */
public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    private final Activity activity;
    private final ActivityResultLauncher<Intent> activityResultLauncher;
    private final List<String> data;
    private final List<String> selectedProjects;
    private final MaterialButton batchButton;
    private final String searchWord;

    public ProjectsAdapter(List<String> data, List<String> selectedProjects, MaterialButton batchButton, String searchWord, ActivityResultLauncher<Intent> activityResultLauncher, Activity activity) {
        this.data = data;
        this.selectedProjects = selectedProjects;
        this.batchButton = batchButton;
        this.searchWord = searchWord;
        this.activityResultLauncher = activityResultLauncher;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ProjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ProjectsAdapter.ViewHolder holder, int position) {
        try {
            String projectPath = this.data.get(position);
            boolean isSelected = selectedProjects.contains(projectPath);

            if (APKExplorer.getAppIcon(projectPath + "/.aeeBackup/appData") != null) {
                holder.mAppIcon.setImageBitmap(APKExplorer.getAppIcon(projectPath + "/.aeeBackup/appData"));
            }
            if (searchWord != null && Common.isTextMatched((Objects.requireNonNull(APKExplorer.getAppName(projectPath + "/.aeeBackup/appData"))), searchWord)) {
                holder.mAppName.setText(APKEditorUtils.fromHtml(Objects.requireNonNull(APKExplorer.getAppName(projectPath + "/.aeeBackup/appData")).replace(searchWord,
                        "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
            } else {
                holder.mAppName.setText(APKExplorer.getAppName(projectPath + "/.aeeBackup/appData"));
            }

            holder.mTotalSize.setText(holder.mAppName.getContext().getString(R.string.last_modified, DateFormat.getDateTimeInstance()
                    .format(new File(projectPath).lastModified())));
            holder.mTotalSize.setVisibility(View.VISIBLE);

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
                    selectedProjects.add(data.get(currentPos));
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.mCheckBox.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    selectedProjects.remove(data.get(currentPos));
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.mDelete.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                String pathToDelete = data.get(currentPos);

                new MaterialAlertDialogBuilder(v.getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(v.getContext().getString(R.string.delete_question, holder.mAppName.getText()))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.delete, (dialog, id) -> v.post(() -> {
                            new DeleteFile(new File(pathToDelete), activity, false).execute();
                            selectedProjects.remove(pathToDelete);
                            data.remove(currentPos);
                            notifyItemRemoved(currentPos);
                            notifyItemRangeChanged(currentPos, data.size());
                            toggleBatchMenu();
                        })).show();
            });

            AppSettings.setSlideInAnimation(holder.itemView, position);
        } catch (NullPointerException ignored) {}
    }

    private void toggleBatchMenu() {
        if (selectedProjects.isEmpty()) {
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
        private final MaterialTextView mAppName, mTotalSize;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mDelete = view.findViewById(R.id.delete);
            this.mAppName = view.findViewById(R.id.title);
            this.mTotalSize = view.findViewById(R.id.version);

            view.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(v.getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(v.getContext().getString(R.string.export_project_question))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.export, (dialog, id) -> {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext())) {
                                sPermissionUtils.requestPermission(
                                        new String[] {
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        }, activity);
                            } else {
                                Projects.exportProject(new File(data.get(getBindingAdapterPosition())), v.getContext());
                            }
                        }).show();
                return false;
            });
        }

        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            String folderPath = data.get(currentPos);

            if (selectedProjects.contains(folderPath)) {
                selectedProjects.remove(folderPath);
                notifyItemChanged(currentPos);
                toggleBatchMenu();
                return;
            }

            Intent explorer = new Intent(view.getContext(), APKExploreActivity.class);
            if (sFileUtils.exist(new File(folderPath, ".aeeBackup/appData"))) {
                explorer.putExtra(APKExploreActivity.BACKUP_PATH_INTENT, new File(data.get(getBindingAdapterPosition()), ".aeeBackup/appData").getAbsolutePath());
            }
            activityResultLauncher.launch(explorer);
        }
    }

}