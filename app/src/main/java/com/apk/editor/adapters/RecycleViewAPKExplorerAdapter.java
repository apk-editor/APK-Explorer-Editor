package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
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
            holder.mDelete.setVisibility(View.GONE);
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
        holder.mDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(holder.mDelete.getContext())
                    .setMessage(holder.mDelete.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.delete, (dialog, id) -> {
                        APKEditorUtils.delete(data.get(position));
                        data.remove(position);
                        notifyDataSetChanged();
                    }).show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private AppCompatImageButton mIcon, mDelete;
        private MaterialTextView mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mDelete = view.findViewById(R.id.delete);
            this.mTitle = view.findViewById(R.id.title);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public static void setOnItemClickListener(ClickListener clickListener) {
        RecycleViewAPKExplorerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}