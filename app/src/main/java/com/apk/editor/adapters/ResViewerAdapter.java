package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.serializableItems.ResEntry;
import com.apk.editor.R;
import com.apk.editor.utils.APKExplorer;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 22, 2025
 */
public class ResViewerAdapter extends RecyclerView.Adapter<ResViewerAdapter.ViewHolder> {

    private final boolean clickable;
    private final List<ResEntry> data;
    private final String rootPath;
    private static ClickListener clickListener;

    public ResViewerAdapter(List<ResEntry> data, String rootPath, boolean clickable) {
        this.data = data;
        this.rootPath = rootPath;
        this.clickable = clickable;
    }

    @NonNull
    @Override
    public ResViewerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_resviewer, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ResViewerAdapter.ViewHolder holder, int position) {
        String name = data.get(position).getName();
        String resAttr = data.get(position).getResAttr();
        String value = data.get(position).getValue();

        holder.mName.setText(resAttr);
        if (value != null) {
            holder.mValue.setText(value);
        } else {
            holder.mValue.setText(name);
        }

        if (value != null && (value.startsWith("res/"))) {
            if (value.endsWith(".xml")) {
                holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_xml, holder.mIcon.getContext()));
            } else if (APKExplorer.getIconFromPath(rootPath + "/" + value) != null) {
                holder.mIcon.setImageURI(APKExplorer.getIconFromPath(rootPath + "/" + value));
            } else {
                holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_image, holder.mIcon.getContext()));
            }
            holder.mIcon.setVisibility(VISIBLE);
        } else {
            holder.mIcon.setVisibility(GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AppCompatImageButton mIcon;
        private final MaterialTextView mName, mValue;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mName = view.findViewById(R.id.name);
            this.mValue = view.findViewById(R.id.value);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View view) {
            if (clickable) {
                clickListener.onItemClick(data.get(getBindingAdapterPosition()).getValue(), view);
            }
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        ResViewerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(String newValue, View v);
    }

}