package com.apk.editor.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.Serializables.ResViewerOptionsItems;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Objects;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class ResViewerOptionsAdapter extends RecyclerView.Adapter<ResViewerOptionsAdapter.ChipViewHolder> {

    private final List<ResViewerOptionsItems> data;
    private final OnItemSelectedListener listener;
    private String selectedItem;
    public ResViewerOptionsAdapter(List<ResViewerOptionsItems> data, String defaultSelected, OnItemSelectedListener listener) {
        this.data = data;
        this.selectedItem = defaultSelected;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Chip chip = (Chip) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_view_resvieweroptions, parent, false);
        return new ChipViewHolder(chip);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        holder.bind(data.get(position), Objects.equals(data.get(position).getTitle(), selectedItem));
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ChipViewHolder extends RecyclerView.ViewHolder {
        private final Chip chip;

        public ChipViewHolder(@NonNull Chip itemView) {
            super(itemView);
            this.chip = itemView;
        }

        public void bind(ResViewerOptionsItems item, boolean isSelected) {
            chip.setText(item.getTitle());
            chip.setChecked(isSelected);

            chip.setOnClickListener(v -> {
                int currentPos = getBindingAdapterPosition();
                String selectedText = data.get(getBindingAdapterPosition()).getTitle();
                if (currentPos == RecyclerView.NO_POSITION) return;

                if (listener != null) {
                    selectedItem = selectedText;
                    listener.onItemSelected(selectedText);
                    notifyItemRangeChanged(0, getItemCount());
                }
            });
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(String selectedItem);
    }

}