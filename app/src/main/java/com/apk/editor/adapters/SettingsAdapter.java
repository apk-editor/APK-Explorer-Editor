package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.recyclerViewItems.SettingsItems;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 31, 2021
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private static ArrayList<SettingsItems> data;

    public SettingsAdapter(ArrayList<SettingsItems> data) {
        SettingsAdapter.data = data;
    }

    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_about, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        holder.Title.setText(data.get(position).getTitle());
        if (data.get(position).getDescription() != null) {
            holder.Description.setText(data.get(position).getDescription());
        } else {
            holder.Description.setVisibility(View.GONE);
        }
        if (APKEditorUtils.isDarkTheme(holder.Title.getContext())) {
            holder.Title.setTextColor(APKEditorUtils.getThemeAccentColor(holder.Title.getContext()));
        }
        holder.mIcon.setColorFilter(APKEditorUtils.isDarkTheme(holder.Title.getContext()) ? Color.WHITE : Color.BLACK);
        if (data.get(position).getIcon() != null) {
            holder.mIcon.setImageDrawable(data.get(position).getIcon());
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialTextView Description, Title;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.Title = view.findViewById(R.id.title);
            this.Description = view.findViewById(R.id.description);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        SettingsAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}