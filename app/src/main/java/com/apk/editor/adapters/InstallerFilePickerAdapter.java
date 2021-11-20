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
import com.apk.editor.utils.Common;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 21, 2021
 */
public class InstallerFilePickerAdapter extends RecyclerView.Adapter<InstallerFilePickerAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private final List<String> data;

    public InstallerFilePickerAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public InstallerFilePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_installerfilepicker, parent, false);
        return new InstallerFilePickerAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull InstallerFilePickerAdapter.ViewHolder holder, int position) {
        try {
            if (new File(this.data.get(position)).isDirectory()) {
                holder.mIcon.setImageDrawable(ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_folder));
                if (sUtils.isDarkTheme(holder.mIcon.getContext())) {
                    holder.mIcon.setBackground(ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_circle));
                }
                holder.mIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mIcon.getContext()));
                holder.mDescription.setVisibility(View.GONE);
                holder.mSize.setVisibility(View.GONE);
                holder.mCheckBox.setVisibility(View.GONE);
            } else if (this.data.get(position).endsWith(".apk")) {
                holder.mIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position), holder.mIcon.getContext()));
                if (sAPKUtils.getPackageName(data.get(position), holder.mIcon.getContext()) != null) {
                    holder.mDescription.setText(sAPKUtils.getPackageName(data.get(position), holder.mIcon.getContext()));
                    holder.mDescription.setVisibility(View.VISIBLE);
                }
                holder.mCheckBox.setChecked(Common.getAPKList().contains(this.data.get(position)));
                holder.mCheckBox.setOnClickListener(v -> {
                    if (Common.getAPKList().contains(this.data.get(position))) {
                        Common.getAPKList().remove(this.data.get(position));
                    } else {
                        Common.getAPKList().add(this.data.get(position));
                    }
                    Common.getSelectCard().setVisibility(Common.getAPKList().isEmpty() ? View.GONE : View.VISIBLE);
                });
                holder.mSize.setText(sAPKUtils.getAPKSize(data.get(position)));
                holder.mSize.setVisibility(View.VISIBLE);
                holder.mCheckBox.setVisibility(View.VISIBLE);
            } else {
                holder.mIcon.setImageDrawable(ContextCompat.getDrawable(holder.mIcon.getContext(), R.drawable.ic_bundle));
                holder.mIcon.setColorFilter(sUtils.isDarkTheme(holder.mIcon.getContext()) ? ContextCompat.getColor(
                        holder.mIcon.getContext(), R.color.colorWhite) : ContextCompat.getColor(holder.mIcon.getContext(), R.color.colorBlack));
                holder.mSize.setText(sAPKUtils.getAPKSize(data.get(position)));
                holder.mSize.setVisibility(View.VISIBLE);
            }
            holder.mTitle.setText(new File(this.data.get(position)).getName());
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        InstallerFilePickerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}