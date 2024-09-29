package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 05, 2021
 */
public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private final List<String> data;

    public FilePickerAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public FilePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
        return new FilePickerAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull FilePickerAdapter.ViewHolder holder, int position) {
        if (new File(this.data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(holder.mTitle.getContext(), R.drawable.ic_folder));
            holder.mIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mTitle.getContext()));
        } else {
            if (APKExplorer.isImageFile(data.get(position))) {
                if (APKExplorer.getIconFromPath(data.get(position)) != null) {
                    holder.mIcon.setImageURI(APKExplorer.getIconFromPath(data.get(position)));
                } else {
                    APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_file), holder.mIcon.getContext());
                }
            } else if (this.data.get(position).endsWith(".apk")) {
                holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position), holder.mIcon.getContext()));
            } else if (data.get(position).endsWith(".xml")) {
                APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_xml), holder.mIcon.getContext());
            } else {
                APKExplorer.setIcon(holder.mIcon, ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_file), holder.mIcon.getContext());
            }
        }
        holder.mTitle.setText(new File(this.data.get(position)).getName());
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialTextView mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.title);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        FilePickerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}