package com.apk.editor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.SerializableItems.ResItems;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 22, 2025
 */
public class ResViewerAdapter extends RecyclerView.Adapter<ResViewerAdapter.ViewHolder> {

    private final List<ResItems> data;

    public ResViewerAdapter(List<ResItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ResViewerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_resviewer, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ResViewerAdapter.ViewHolder holder, int position) {
        holder.mTitle.setHint(data.get(position).getName());
        holder.mValue.setText(data.get(position).getValue());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialAutoCompleteTextView mValue;
        private final TextInputLayout mTitle;

        public ViewHolder(View view) {
            super(view);
            this.mValue = view.findViewById(R.id.value);
            this.mTitle = view.findViewById(R.id.title);
        }
    }

}