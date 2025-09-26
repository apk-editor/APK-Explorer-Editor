package com.apk.editor.adapters;

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
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SerializableItems.PackageItems;
import com.apk.editor.utils.menu.ExploreOptionsMenu;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {

    private final Activity activity;
    private final List<PackageItems> data;
    private final List<String> packageNames;
    private final String searchWord;
    private static boolean mlongClicked;

    public ApplicationsAdapter(List<PackageItems> data, List<String> packageNames, String searchWord, boolean longClicked, Activity activity) {
        mlongClicked = longClicked;
        this.data = data;
        this.packageNames = packageNames;
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
            data.get(position).loadAppIcon(holder.mAppIcon);
            if (searchWord != null && Common.isTextMatched(data.get(position).getPackageName(), searchWord)) {
                holder.mAppID.setText(APKEditorUtils.fromHtml(data.get(position).getPackageName().replace(searchWord, "<b><i><font color=\"" +
                        Color.RED + "\">" + searchWord + "</font></i></b>")));
            } else {
                holder.mAppID.setText(data.get(position).getPackageName());
            }
            if (searchWord != null && Common.isTextMatched(data.get(position).getAppName(), searchWord)) {
                holder.mAppName.setText(APKEditorUtils.fromHtml(data.get(position).getAppName().replace(searchWord,
                        "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
            } else {
                holder.mAppName.setText(data.get(position).getAppName());
            }
            holder.mCheckBox.setVisibility(mlongClicked ? View.VISIBLE : View.GONE);
            holder.mCheckBox.setChecked(packageNames.contains(data.get(position).getPackageName()));
            holder.mCheckBox.setOnClickListener(v -> {
                if (packageNames.contains(data.get(position).getPackageName())) {
                    packageNames.remove(data.get(position).getPackageName());
                } else {
                    packageNames.add(data.get(position).getPackageName());
                }
                notifyItemChanged(position);
                activity.findViewById(R.id.batch_options).setVisibility(packageNames.isEmpty() ? View.GONE : View.VISIBLE);
            });
            holder.mOpenIcon.setVisibility(!mlongClicked && data.get(position).launchIntent(holder.mOpenIcon.getContext()) != null ? View.VISIBLE : View.GONE);
            holder.mOpenIcon.setOnClickListener(v -> {
                if (data.get(position).getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    return;
                }
                v.getContext().startActivity(data.get(position).launchIntent(holder.mOpenIcon.getContext()));
            });
            holder.mVersion.setText(holder.mAppName.getContext().getString(R.string.version, data.get(position).getAppVersion()));
            holder.mSize.setText(holder.mAppName.getContext().getString(R.string.size, sAPKUtils.getAPKSize(data.get(position).getAPKSize())));
            holder.mAppIcon.setOnClickListener(v -> {
                Intent imageView = new Intent(v.getContext(), ImageViewActivity.class);
                imageView.putExtra(ImageViewActivity.PACKAGE_NAME_INTENT, data.get(position).getPackageName());
                v.getContext().startActivity(imageView);
            });
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mVersion.setVisibility(View.VISIBLE);
            holder.mCard.setOnLongClickListener(v -> {
                String packageName = data.get(position).getPackageName();
                mlongClicked = !mlongClicked;
                if (packageNames.contains(packageName)) {
                    packageNames.remove(packageName);
                } else {
                    packageNames.add(packageName);
                }
                activity.findViewById(R.id.batch_options).setVisibility(packageNames.isEmpty() ? View.GONE : View.VISIBLE);
                notifyItemRangeChanged(0, getItemCount());
                return true;
            });
        } catch (NullPointerException | IndexOutOfBoundsException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mAppIcon;
        private final MaterialButton mOpenIcon;
        private final MaterialCardView mCard;
        private final MaterialCheckBox mCheckBox;
        private final MaterialTextView mAppID, mAppName, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mCard = view.findViewById(R.id.card);
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
            if (mlongClicked) {
                if (packageNames.contains(data.get(getBindingAdapterPosition()).getPackageName())) {
                    packageNames.remove(data.get(getBindingAdapterPosition()).getPackageName());
                } else {
                    packageNames.add(data.get(getBindingAdapterPosition()).getPackageName());
                }
                notifyItemChanged(getBindingAdapterPosition());
                activity.findViewById(R.id.batch_options).setVisibility(packageNames.isEmpty() ? View.GONE : View.VISIBLE);
                return;
            }
            ExploreOptionsMenu.getMenu(data.get(getBindingAdapterPosition()).getPackageName(), null, null, false, activity);
        }
    }

}