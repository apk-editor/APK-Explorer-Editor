package com.apk.editor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.serializableItems.ResEntry;
import com.apk.editor.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 25, 2025
 */
public class StringViewAdapter extends RecyclerView.Adapter<StringViewAdapter.ViewHolder> {

    private final List<ResEntry> data;

    public StringViewAdapter(List<ResEntry> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public StringViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_stringviewer, parent, false);
        return new StringViewAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull StringViewAdapter.ViewHolder holder, int position) {
        holder.mTitle.setHint(data.get(position).getName());
        holder.mValue.setText(data.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText mValue;
        private final TextInputLayout mTitle;

        public ViewHolder(View view) {
            super(view);
            this.mTitle = view.findViewById(R.id.title);
            this.mValue = view.findViewById(R.id.value);
        }
    }

}