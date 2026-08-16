package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.Serializables.ExploreOptionsItems;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 26, 2026
 */
public class ExploreOptionsAdapter extends RecyclerView.Adapter<ExploreOptionsAdapter.ViewHolder> {

    private final List<ExploreOptionsItems> data;
    private final OnItemClickListener listener;

    public ExploreOptionsAdapter(List<ExploreOptionsItems> items, OnItemClickListener listener) {
        this.data = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_exploreoptions, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExploreOptionsItems item = this.data.get(position);
        holder.title.setText(item.getTitle());

        if (this.data.get(position).getIconRes() != Integer.MIN_VALUE) {
            holder.icon.setImageResource(this.data.get(position).getIconRes());
            holder.icon.setVisibility(VISIBLE);
        } else {
            holder.icon.setVisibility(GONE);
        }

        if (this.data.get(position).isCheckable()) {
            holder.checkBox.setChecked(item.isChecked());
            holder.checkBox.setVisibility(VISIBLE);
        } else {
            holder.checkBox.setVisibility(GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton icon;
        private final MaterialCheckBox checkBox;
        private final MaterialTextView title;
        ViewHolder(@NonNull View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            title = view.findViewById(R.id.title);
            checkBox = view.findViewById(R.id.checkbox);

            view.setOnClickListener(v -> listener.onItemClick(data.get(getBindingAdapterPosition()).getId()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

}