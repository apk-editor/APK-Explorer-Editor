package com.apk.editor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import com.apk.editor.utils.Serializables.SettingsItems;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 31, 2021
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private final ArrayList<SettingsItems> data;
    private final OnItemClickListener clickListener;

    public SettingsAdapter(ArrayList<SettingsItems> data, OnItemClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_about, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        holder.Title.setText(data.get(position).getTitle());
        if (data.get(position).getDescription() != null) {
            holder.Description.setText(data.get(position).getDescription());
        } else {
            holder.Description.setVisibility(View.GONE);
        }
        if (data.get(position).getIconRes() != Integer.MIN_VALUE) {
            holder.mIcon.setImageResource(data.get(position).getIconRes());
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }
        if (data.get(position).getId() == 0) {
            holder.Title.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.Title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            holder.Title.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            clickListener.onItemClick(getBindingAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}