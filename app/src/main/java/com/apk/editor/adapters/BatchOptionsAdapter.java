package com.apk.editor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Serializables.BatchItems;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 23, 2025
 */
public class BatchOptionsAdapter extends RecyclerView.Adapter<BatchOptionsAdapter.ViewHolder> {

    private final List<BatchItems> data;

    public BatchOptionsAdapter(List<BatchItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public BatchOptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_batch_options, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull BatchOptionsAdapter.ViewHolder holder, int position) {
        data.get(position).load(holder.mAppIcon, holder.mAppName, holder.mAppID, holder.mCheckBox);
        AppSettings.setSlideInAnimation(holder.mAppIcon, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageButton mAppIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mAppID, mAppName;

        public ViewHolder(View view) {
            super(view);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mAppName = view.findViewById(R.id.title);
            this.mAppID = view.findViewById(R.id.description);

            view.setOnClickListener(v -> {
                BatchItems batchItems = data.get(getBindingAdapterPosition());
                batchItems.setSelected(!batchItems.isSelected());
                notifyItemChanged(getBindingAdapterPosition());
            });
        }
    }

}