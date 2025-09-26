package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 21, 2021
 */
public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {

    private final Activity activity;

    private final List<String> data, apkList;
    private static ClickListener clickListener;

    public FilePickerAdapter(List<String> data, List<String> apklist, Activity activity) {
        this.data = data;
        this.apkList = apklist;
        this.activity = activity;
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
        try {
            if (new File(this.data.get(position)).isDirectory()) {
                holder.mIcon.setImageDrawable(ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_folder));
                holder.mIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mIcon.getContext()));
                holder.mDescription.setVisibility(View.GONE);
                holder.mSize.setVisibility(View.GONE);
                holder.mCheckBox.setVisibility(View.GONE);
            } else if (this.data.get(position).endsWith(".apk")) {
                holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position), holder.mIcon.getContext()));
                if (sAPKUtils.getPackageName(data.get(position), holder.mIcon.getContext()) != null) {
                    holder.mDescription.setText(sAPKUtils.getPackageName(data.get(position), holder.mIcon.getContext()));
                    holder.mDescription.setVisibility(View.VISIBLE);
                } else {
                    holder.mDescription.setVisibility(View.GONE);
                }
                holder.mCheckBox.setChecked(apkList.contains(this.data.get(position)));
                holder.mCheckBox.setOnClickListener(v -> {
                    if (apkList.contains(this.data.get(position))) {
                        apkList.remove(this.data.get(position));
                    } else {
                        apkList.add(this.data.get(position));
                    }
                    activity.findViewById(R.id.select).setVisibility(apkList.isEmpty() ? View.GONE : View.VISIBLE);
                });
                holder.mSize.setText(sAPKUtils.getAPKSize(new File(this.data.get(position)).length()));
                holder.mSize.setVisibility(View.VISIBLE);
                holder.mCheckBox.setVisibility(View.VISIBLE);
            } else {
                holder.mIcon.setImageDrawable(ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_bundle));
                holder.mIcon.setColorFilter(sThemeUtils.isDarkTheme(holder.mIcon.getContext()) ? ContextCompat.getColor(
                        holder.mIcon.getContext(), R.color.colorWhite) : ContextCompat.getColor(holder.mIcon.getContext(), R.color.colorBlack));
                holder.mSize.setText(sAPKUtils.getAPKSize(new File(this.data.get(position)).length()));
                holder.mSize.setVisibility(View.VISIBLE);
                holder.mCheckBox.setVisibility(View.GONE);
                holder.mDescription.setVisibility(View.GONE);
            }
            holder.mTitle.setText(new File(this.data.get(position)).getName());
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mDescription, mSize, mTitle;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(data.get(getBindingAdapterPosition()), getBindingAdapterPosition());
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        FilePickerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(String filePath, int position);
    }

}