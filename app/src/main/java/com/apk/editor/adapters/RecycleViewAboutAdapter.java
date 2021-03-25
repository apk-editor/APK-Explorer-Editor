package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.activities.CreditsActivity;
import com.apk.editor.activities.DocumentationActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.RecycleViewItem;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class RecycleViewAboutAdapter extends RecyclerView.Adapter<RecycleViewAboutAdapter.ViewHolder> {

    private static ArrayList<RecycleViewItem> data;

    public RecycleViewAboutAdapter(ArrayList<RecycleViewItem> data) {
        RecycleViewAboutAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewAboutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_about, parent, false);
        return new RecycleViewAboutAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewAboutAdapter.ViewHolder holder, int position) {
        holder.Title.setText(data.get(position).getTitle());
        holder.Description.setText(data.get(position).getDescription());
        if (APKEditorUtils.isDarkTheme(holder.Title.getContext())) {
            holder.Title.setTextColor(APKEditorUtils.getThemeAccentColor(holder.Title.getContext()));
        }
        if (position != 0) {
            holder.mIcon.setColorFilter(APKEditorUtils.isDarkTheme(holder.Title.getContext()) ? Color.WHITE : Color.BLACK);
        }
        holder.mIcon.setImageDrawable(data.get(position).getIcon());
        holder.mRVLayout.setOnClickListener(v -> {
            if (data.get(position).getURL() != null) {
                APKEditorUtils.launchUrl(data.get(position).getURL(), (Activity) holder.mRVLayout.getContext());
            } else if (position == 0) {
                Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                settings.setData(uri);
                holder.mRVLayout.getContext().startActivity(settings);
            } else if (position == 5) {
                Intent documentation = new Intent(holder.mRVLayout.getContext(), DocumentationActivity.class);
                holder.mRVLayout.getContext().startActivity(documentation);
            } else if (position == 6) {
                Intent credits = new Intent(holder.mRVLayout.getContext(), CreditsActivity.class);
                holder.mRVLayout.getContext().startActivity(credits);
            } else {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_TEXT, holder.mRVLayout.getContext().getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, holder.mRVLayout.getContext().getString(R.string.share_with));
                holder.mRVLayout.getContext().startActivity(shareIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageButton mIcon;
        private MaterialTextView Title;
        private MaterialTextView Description;
        private LinearLayout mRVLayout;

        public ViewHolder(View view) {
            super(view);
            this.mIcon = view.findViewById(R.id.icon);
            this.Title = view.findViewById(R.id.title);
            this.Description = view.findViewById(R.id.description);
            this.mRVLayout = view.findViewById(R.id.rv_about);
        }
    }

}