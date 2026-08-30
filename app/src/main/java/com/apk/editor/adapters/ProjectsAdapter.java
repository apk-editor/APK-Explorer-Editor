package com.apk.editor.adapters;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Projects;
import com.apk.editor.utils.dialogs.FileActionDialog;
import com.apk.editor.utils.tasks.DeleteFile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.text.DateFormat;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 06, 2021
 */
public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    private final Activity activity;
    private final OnItemClickListener clickListener;
    private final List<String> data;
    private final List<String> selectedProjects;
    private final MaterialButton batchButton;

    public ProjectsAdapter(List<String> data, List<String> selectedProjects, MaterialButton batchButton, OnItemClickListener clickListener, Activity activity) {
        this.data = data;
        this.selectedProjects = selectedProjects;
        this.batchButton = batchButton;
        this.clickListener = clickListener;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ProjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
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
            holder.mAppName.setText(APKExplorer.getAppName(projectPath + "/.aeeBackup/appData"));
            holder.mPackageName.setText(APKExplorer.getPackageName(projectPath + "/.aeeBackup/appData"));

            holder.mDelete.setIcon(sCommonUtils.getDrawable(R.drawable.ic_delete, holder.mDelete.getContext()));
            holder.mVersion.setText(holder.mVersion.getContext().getString(R.string.last_modified, DateFormat.getDateTimeInstance()
                    .format(new File(projectPath).lastModified())));
            holder.mSize.setText(APKExplorer.getVersionInfo(projectPath + "/.aeeBackup/appData"));
            holder.mDelete.setVisibility(VISIBLE);

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

                deleteProject(holder.mAppIcon.getDrawable(), currentPos, v.getContext().getString(R.string.delete_question, holder.mAppName
                        .getText().toString().trim() + " (" + new File(data.get(currentPos)).getName() + ")"), data.get(currentPos), v.getContext());
            });

            AppSettings.setSlideInAnimation(holder.mAppIcon, position);
        } catch (NullPointerException ignored) {}
    }

    private void deleteProject(Drawable drawable, int currentPos, String title, String filePathToDelete, Context context) {
        new FileActionDialog(drawable, title, context) {
            @Override
            public void onPositiveAction() {
                new DeleteFile(new File(filePathToDelete), activity, false).execute();
                selectedProjects.remove(filePathToDelete);
                data.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, data.size());
                toggleBatchMenu();
            }
        };
    }

    private void toggleBatchMenu() {
        if (selectedProjects.isEmpty()) {
            batchButton.setVisibility(View.GONE);
        } else {
            batchButton.setVisibility(View.VISIBLE);
        }
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
        private final AppCompatImageButton mAppIcon;
        private final MaterialButton mDelete;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mAppName, mPackageName, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mDelete = view.findViewById(R.id.open);
            this.mAppName = view.findViewById(R.id.title);
            this.mPackageName = view.findViewById(R.id.description);
            this.mVersion = view.findViewById(R.id.version);
            this.mSize = view.findViewById(R.id.size);

            view.setOnLongClickListener(v -> {
                new FileActionDialog(this.mAppIcon.getDrawable(), v.getContext().getString(R.string.export_project_question), v.getContext()) {
                    @Override
                    public void onPositiveAction() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext())) {
                            sPermissionUtils.requestPermission(
                                    new String[] {
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, activity);
                        } else {
                            Projects.exportProject(new File(data.get(getBindingAdapterPosition())), v.getContext());
                        }
                    }
                };
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

            clickListener.onItemClick(sFileUtils.exist(new File(folderPath, ".aeeBackup/appData")) ? new File(folderPath, ".aeeBackup/appData").getAbsolutePath() : null);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String backupPath);
    }

}