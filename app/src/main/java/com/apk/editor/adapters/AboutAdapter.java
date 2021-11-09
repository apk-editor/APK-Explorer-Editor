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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.activities.CreditsActivity;
import com.apk.editor.activities.DocumentationActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.recyclerViewItems.AboutItems;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {

    private static ArrayList<AboutItems> data;

    public AboutAdapter(ArrayList<AboutItems> data) {
        AboutAdapter.data = data;
    }

    @NonNull
    @Override
    public AboutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_about, parent, false);
        return new AboutAdapter.ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull AboutAdapter.ViewHolder holder, int position) {
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
                APKEditorUtils.launchUrl(data.get(position).getURL(), (Activity) v.getContext());
            } else if (position == 0) {
                Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                settings.setData(uri);
                v.getContext().startActivity(settings);
            } else if (position == 5) {
                Intent documentation = new Intent(v.getContext(), DocumentationActivity.class);
                v.getContext().startActivity(documentation);
            } else if (position == 6) {
                Intent credits = new Intent(v.getContext(), CreditsActivity.class);
                v.getContext().startActivity(credits);
            } else {
                Intent share_app = new Intent();
                share_app.setAction(Intent.ACTION_SEND);
                share_app.putExtra(Intent.EXTRA_TEXT, v.getContext().getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                share_app.setType("text/plain");
                Intent shareIntent = Intent.createChooser(share_app, v.getContext().getString(R.string.share_with));
                v.getContext().startActivity(shareIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mIcon;
        private final MaterialTextView Description, Title;
        private final LinearLayoutCompat mRVLayout;

        public ViewHolder(View view) {
            super(view);
            this.mIcon = view.findViewById(R.id.icon);
            this.Title = view.findViewById(R.id.title);
            this.Description = view.findViewById(R.id.description);
            this.mRVLayout = view.findViewById(R.id.rv_about);
        }
    }

}