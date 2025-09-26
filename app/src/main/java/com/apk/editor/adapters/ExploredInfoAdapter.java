package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.HashMap;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 25, 2025
 */
public class ExploredInfoAdapter extends RecyclerView.Adapter<ExploredInfoAdapter.ViewHolder> {

    private final List<HashMap<String, String>> data;

    public ExploredInfoAdapter(List<HashMap<String, String>> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ExploredInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_exploreinfo, parent, false);
        return new ExploredInfoAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploredInfoAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position).get("title"));
        if (data.get(position).get("description") != null) {
            holder.mDescription.setText(data.get(position).get("description"));
            holder.mDescription.setVisibility(VISIBLE);
        } else {
            holder.mDescription.setVisibility(GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mTitle, mDescription;

        public ViewHolder(View view) {
            super(view);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
        }
    }

}