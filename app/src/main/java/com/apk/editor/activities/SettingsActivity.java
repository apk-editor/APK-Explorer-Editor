package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppSettings;
import com.google.android.material.textview.MaterialTextView;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class SettingsActivity extends AppCompatActivity {

    private ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mData.add(new RecycleViewItem(getString(R.string.user_interface), null, null));
        mData.add(new RecycleViewItem(getString(R.string.app_theme), AppSettings.getAppTheme(this), getResources().getDrawable(R.drawable.ic_theme)));
        mData.add(new RecycleViewItem(getString(R.string.settings_general), null, null));
        mData.add(new RecycleViewItem(getString(R.string.project_exist_action), AppSettings.getProjectExistAction(this), getResources().getDrawable(R.drawable.ic_projects)));
        mData.add(new RecycleViewItem(getString(R.string.export_path_resources), AppSettings.getExportPath(this), getResources().getDrawable(R.drawable.ic_export)));
        if (APKEditorUtils.isFullVersion(this)) {
            mData.add(new RecycleViewItem(getString(R.string.text_editing), AppSettings.getEditingOptions(this), getResources().getDrawable(R.drawable.ic_edit)));
            mData.add(new RecycleViewItem(getString(R.string.signing_title), null, null));
            mData.add(new RecycleViewItem(getString(R.string.export_options), AppSettings.getAPKs(this), getResources().getDrawable(R.drawable.ic_android)));
            mData.add(new RecycleViewItem(getString(R.string.installer_action), AppSettings.getInstallerAction(this), getResources().getDrawable(R.drawable.ic_installer)));
            mData.add(new RecycleViewItem(getString(R.string.sign_apk_with), AppSettings.getAPKSign(this), getResources().getDrawable(R.drawable.ic_key)));
        }
        mData.add(new RecycleViewItem(getString(R.string.settings_misc), null, null));
        mData.add(new RecycleViewItem(getString(R.string.clear_cache), getString(R.string.clear_cache_summary), getResources().getDrawable(R.drawable.ic_delete)));

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (mData.get(position).getDescription() != null) {
                if (position == 1) {
                    AppSettings.setAppTheme(v.getContext());
                } else if (position == 3) {
                    AppSettings.setProjectExistAction(v.getContext());
                    mData.set(position, new RecycleViewItem(getString(R.string.project_exist_action), AppSettings.getProjectExistAction(this), getResources().getDrawable(R.drawable.ic_projects)));
                    mRecycleViewAdapter.notifyItemChanged(position);
                } else if (position == 4) {
                    AppSettings.setExportPath(v.getContext());
                    mData.set(position, new RecycleViewItem(getString(R.string.export_path_resources), AppSettings.getExportPath(this), getResources().getDrawable(R.drawable.ic_export)));
                    mRecycleViewAdapter.notifyItemChanged(position);
                } else if (APKEditorUtils.isFullVersion(this) && position == 5) {
                    AppSettings.setEditingOptions(v.getContext());
                    mData.set(position, new RecycleViewItem(getString(R.string.text_editing), AppSettings.getEditingOptions(this), getResources().getDrawable(R.drawable.ic_edit)));
                    mRecycleViewAdapter.notifyItemChanged(position);
                } else if (APKEditorUtils.isFullVersion(this) && position == 7) {
                    AppSettings.setAPKs(v.getContext());
                    mData.set(position, new RecycleViewItem(getString(R.string.export_options), AppSettings.getAPKs(this), getResources().getDrawable(R.drawable.ic_android)));
                    mRecycleViewAdapter.notifyItemChanged(position);
                } else if (APKEditorUtils.isFullVersion(this) && position == 8) {
                    AppSettings.setInstallerAction(v.getContext());
                    mData.set(position, new RecycleViewItem(getString(R.string.installer_action), AppSettings.getInstallerAction(this), getResources().getDrawable(R.drawable.ic_installer)));
                    mRecycleViewAdapter.notifyItemChanged(position);
                } else if (APKEditorUtils.isFullVersion(this) && position == 9) {
                    AppSettings.setAPKSign(v.getContext());
                    mData.set(position, new RecycleViewItem(getString(R.string.sign_apk_with), AppSettings.getAPKSign(this), getResources().getDrawable(R.drawable.ic_key)));
                    mRecycleViewAdapter.notifyItemChanged(position);
                } else {
                    AppSettings.deleteAppSettings(this);
                }
            }
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private static ClickListener clickListener;

        private static ArrayList<RecycleViewItem> data;

        public RecycleViewAdapter(ArrayList<RecycleViewItem> data) {
            RecycleViewAdapter.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_about, parent, false);
            return new ViewHolder(rowItem);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            holder.Title.setText(data.get(position).getTitle());
            if (data.get(position).getDescription() != null) {
                holder.Description.setText(data.get(position).getDescription());
            } else {
                holder.Description.setVisibility(View.GONE);
            }
            if (APKEditorUtils.isDarkTheme(holder.Title.getContext())) {
                holder.Title.setTextColor(APKEditorUtils.getThemeAccentColor(holder.Title.getContext()));
            }
            holder.mIcon.setColorFilter(APKEditorUtils.isDarkTheme(holder.Title.getContext()) ? Color.WHITE : Color.BLACK);
            if (data.get(position).getIcon() != null) {
                holder.mIcon.setImageDrawable(data.get(position).getIcon());
            } else {
                holder.mIcon.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private AppCompatImageButton mIcon;
            private MaterialTextView Title;
            private MaterialTextView Description;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.mIcon = view.findViewById(R.id.icon);
                this.Title = view.findViewById(R.id.title);
                this.Description = view.findViewById(R.id.description);
            }

            @Override
            public void onClick(View view) {
                clickListener.onItemClick(getAdapterPosition(), view);
            }
        }

        public void setOnItemClickListener(ClickListener clickListener) {
            RecycleViewAdapter.clickListener = clickListener;
        }

        public interface ClickListener {
            void onItemClick(int position, View v);
        }

    }

    private static class RecycleViewItem implements Serializable {
        private String mTitle;
        private String mDescription;
        private Drawable mIcon;

        public RecycleViewItem(String title, String description, Drawable icon) {
            this.mTitle = title;
            this.mDescription = description;
            this.mIcon = icon;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public Drawable getIcon() {
            return mIcon;
        }

    }

}