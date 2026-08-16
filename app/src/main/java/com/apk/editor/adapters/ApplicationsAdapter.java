package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.Serializables.PackageItems;
import com.apk.editor.utils.menu.ExploreOptionsMenu;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {

    private final Activity activity;
    private final List<PackageItems> data;
    private final List<String> packageNames;
    private final MaterialButton batchButton;
    private final String searchWord;

    public ApplicationsAdapter(List<PackageItems> data, List<String> packageNames, MaterialButton batchButton, String searchWord, Activity activity) {
        this.data = data;
        this.packageNames = packageNames;
        this.batchButton = batchButton;
        this.searchWord = searchWord;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ApplicationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull ApplicationsAdapter.ViewHolder holder, int position) {
        try {
            PackageItems packageItems = this.data.get(position);
            boolean isSelected = packageNames.contains(packageItems.getPackageName());

            packageItems.loadAppIcon(holder.mAppIcon);

            if (searchWord != null && Common.isTextMatched(packageItems.getPackageName(), searchWord)) {
                holder.mAppID.setText(APKEditorUtils.fromHtml(packageItems.getPackageName().replace(searchWord, "<b><i><font color=\"" +
                        Color.RED + "\">" + searchWord + "</font></i></b>")));
            } else {
                holder.mAppID.setText(packageItems.getPackageName());
            }
            if (searchWord != null && Common.isTextMatched(packageItems.getAppName(), searchWord)) {
                holder.mAppName.setText(APKEditorUtils.fromHtml(packageItems.getAppName().replace(searchWord,
                        "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
            } else {
                holder.mAppName.setText(packageItems.getAppName());
            }

            if (isSelected) {
                holder.mCheckBox.setVisibility(View.VISIBLE);
                holder.mAppIcon.setVisibility(View.GONE);
                holder.mCheckBox.setChecked(true);
            } else {
                holder.mCheckBox.setVisibility(View.GONE);
                holder.mAppIcon.setVisibility(View.VISIBLE);
                holder.mCheckBox.setChecked(false);
            }

            toggleBatchMenu();

            holder.mAppIcon.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    packageNames.add(data.get(currentPos).getPackageName());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.mAppIcon.setOnLongClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    Intent imageView = new Intent(v.getContext(), ImageViewActivity.class);
                    imageView.putExtra(ImageViewActivity.PACKAGE_NAME_INTENT, data.get(currentPos).getPackageName());
                    v.getContext().startActivity(imageView);
                }
                return true;
            });

            holder.mCheckBox.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    packageNames.remove(data.get(currentPos).getPackageName());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                }
            });

            holder.mOpenIcon.setVisibility(packageItems.launchIntent(holder.mOpenIcon.getContext()) != null ? VISIBLE : GONE);

            holder.mOpenIcon.setOnClickListener(v -> {
                if (packageItems.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    return;
                }
                v.getContext().startActivity(packageItems.launchIntent(holder.mOpenIcon.getContext()));
            });

            holder.mVersion.setText(holder.mAppName.getContext().getString(R.string.version, packageItems.getAppVersion()));
            holder.mSize.setText(holder.mAppName.getContext().getString(R.string.size, packageItems.getSize(holder.mSize.getContext())));
            holder.mSize.setVisibility(VISIBLE);
            holder.mVersion.setVisibility(VISIBLE);

            AppSettings.setSlideInAnimation(holder.itemView, position);
        } catch (NullPointerException | IndexOutOfBoundsException ignored) {}
    }

    private void toggleBatchMenu() {
        if (packageNames.isEmpty()) {
            batchButton.setVisibility(View.GONE);
        } else {
            batchButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mAppIcon;
        private final MaterialButton mOpenIcon;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mAppID, mAppName, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mCheckBox = view.findViewById(R.id.checkbox);
            this.mOpenIcon = view.findViewById(R.id.open);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mAppName = view.findViewById(R.id.title);
            this.mAppID = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
            this.mVersion = view.findViewById(R.id.version);
        }

        @Override
        public void onClick(View view) {
            int currentPos = getBindingAdapterPosition();
            PackageItems packageItems = data.get(currentPos);
            if (currentPos == RecyclerView.NO_POSITION) return;

            if (packageNames.contains(packageItems.getPackageName())) {
                view.post(() -> {
                    packageNames.remove(packageItems.getPackageName());
                    notifyItemChanged(currentPos);
                    toggleBatchMenu();
                });
                return;
            }

            ExploreOptionsMenu.getMenu(packageItems.getPackageName(), null, null, false, activity);
        }
    }

}