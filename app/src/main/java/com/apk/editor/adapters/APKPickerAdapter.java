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
import com.apk.editor.utils.SerializableItems.APKPickerItems;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 22, 2025
 */
public class APKPickerAdapter extends RecyclerView.Adapter<APKPickerAdapter.ViewHolder> {

    private final List<APKPickerItems> data;

    public APKPickerAdapter(List<APKPickerItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public APKPickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apkpicker, parent, false);
        return new APKPickerAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull APKPickerAdapter.ViewHolder holder, int position) {
        holder.mIcon.setImageDrawable(data.get(position).getImageDrawable(holder.mIcon.getContext()));
        holder.mTitle.setText(data.get(position).getAPKName());
        if (data.get(position).getPackageName(holder.mDescription.getContext()) != null) {
            holder.mDescription.setText(data.get(position).getPackageName(holder.mDescription.getContext()));
            holder.mDescription.setVisibility(VISIBLE);
        } else {
            holder.mDescription.setVisibility(GONE);
        }
        holder.mSize.setText(data.get(position).getAPKSize());
        holder.mSize.setVisibility(VISIBLE);
        holder.mCheckBox.setChecked(data.get(position).isSelected());
        holder.mCheckBox.setVisibility(VISIBLE);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mTitle, mDescription, mSize;

        public ViewHolder(View view) {
            super(view);
            this.mIcon = view.findViewById(R.id.icon);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);

            view.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                data.get(position).isSelected(!data.get(position).isSelected());
                mCheckBox.setChecked(!mCheckBox.isChecked());
            });
        }
    }

}