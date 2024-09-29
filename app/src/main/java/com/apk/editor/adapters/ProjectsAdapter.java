package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKExploreActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Projects;
import com.apk.editor.utils.tasks.DeleteProject;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.text.DateFormat;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 06, 2021
 */
public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    private static List<String> data;

    public ProjectsAdapter(List<String> data) {
        ProjectsAdapter.data = data;
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
            holder.mAppIcon.setImageBitmap(APKExplorer.getAppIcon(data.get(position) + "/.aeeBackup/appData"));
            if (Common.getSearchWord() != null && Common.isTextMatched((Objects.requireNonNull(APKExplorer.getAppName(data.get(position) + "/.aeeBackup/appData"))), Common.getSearchWord())) {
                holder.mAppName.setText(APKEditorUtils.fromHtml(Objects.requireNonNull(APKExplorer.getAppName(data.get(position) + "/.aeeBackup/appData")).replace(Common.getSearchWord(),
                        "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
            } else {
                holder.mAppName.setText(APKExplorer.getAppName(data.get(position) + "/.aeeBackup/appData"));
            }
            holder.mTotalSize.setText(holder.mAppName.getContext().getString(R.string.last_modified, DateFormat.getDateTimeInstance()
                    .format(new File(data.get(position)).lastModified())));
            holder.mCard.setOnClickListener(v -> {
                Common.setPath(data.get(position));
                Intent explorer = new Intent(v.getContext(), APKExploreActivity.class);
                v.getContext().startActivity(explorer);
            });
            holder.mCard.setOnLongClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return false;
                new MaterialAlertDialogBuilder(v.getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(v.getContext().getString(R.string.export_project_question))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.export, (dialog, id) -> {
                            if (sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext())) {
                                sPermissionUtils.requestPermission(
                                        new String[] {
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        },(Activity) v.getContext());
                            } else {
                                Projects.exportProject(new File(data.get(position)), v.getContext());
                            }
                        }).show();
                return false;
            });
            holder.mDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(v.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.delete, (dialog, id) -> {
                        new DeleteProject(new File(data.get(position)), v.getContext()).execute();
                        data.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, data.size());
                    }).show());
            holder.mTotalSize.setVisibility(View.VISIBLE);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mAppIcon, mDelete;
        private final MaterialCardView mCard;
        private final MaterialTextView mAppName, mTotalSize;

        public ViewHolder(View view) {
            super(view);
            this.mCard = view.findViewById(R.id.card);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mDelete = view.findViewById(R.id.delete);
            this.mAppName = view.findViewById(R.id.title);
            this.mTotalSize = view.findViewById(R.id.version);
        }
    }

}