package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on January 23, 2025
 */
public class BatchOptionsAdapter extends RecyclerView.Adapter<BatchOptionsAdapter.ViewHolder> {

    private final List<String> data;

    public BatchOptionsAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public BatchOptionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_batch_options, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
    @Override
    public void onBindViewHolder(@NonNull BatchOptionsAdapter.ViewHolder holder, int position) {
        loadAppIcon(data.get(position), holder.mAppIcon);
        holder.mAppID.setText(data.get(position));
        holder.mAppName.setText(sPackageUtils.getAppName(data.get(position), holder.mAppName.getContext()));
    }

    private void loadAppIcon(String packageName, AppCompatImageButton view) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Drawable drawable = sPackageUtils.getAppIcon(packageName, view.getContext());

            handler.post(() -> view.setImageDrawable(drawable));
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageButton mAppIcon;
        private final MaterialTextView mAppID, mAppName;

        public ViewHolder(View view) {
            super(view);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mAppName = view.findViewById(R.id.title);
            this.mAppID = view.findViewById(R.id.description);
        }
    }

}