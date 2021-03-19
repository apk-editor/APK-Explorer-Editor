package com.apk.editor.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.APKSignActivity;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class RecycleViewAppsAdapter extends RecyclerView.Adapter<RecycleViewAppsAdapter.ViewHolder> {

    private static List<String> data;

    public RecycleViewAppsAdapter(List<String> data) {
        RecycleViewAppsAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewAppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAppsAdapter.ViewHolder holder, int position) {
        try {
            holder.mAppIcon.setImageDrawable(AppData.getAppIcon(data.get(position), holder.mAppIcon.getContext()));
            if (AppData.mSearchText != null && data.get(position).toLowerCase().contains(AppData.mSearchText)) {
                holder.mAppID.setText(APKEditorUtils.fromHtml(data.get(position).toLowerCase().replace(AppData.mSearchText, "<b><i><font color=\"" +
                        Color.RED + "\">" + AppData.mSearchText + "</font></i></b>")));
            } else {
                holder.mAppID.setText(data.get(position));
            }
            if (AppData.mSearchText != null && AppData.getAppName(data.get(position), holder.mAppName.getContext()).toString().toLowerCase().contains(AppData.mSearchText)) {
                holder.mAppName.setText(APKEditorUtils.fromHtml(AppData.getAppName(data.get(position), holder.mAppName.getContext()).toString().toLowerCase().replace(AppData.mSearchText,
                        "<b><i><font color=\"" + Color.RED + "\">" + AppData.mSearchText + "</font></i></b>")));
            } else {
                holder.mAppName.setText(AppData.getAppName(data.get(position), holder.mAppName.getContext()));
            }
            holder.mVersion.setText(holder.mAppName.getContext().getString(R.string.version, AppData.getVersionName(AppData.getSourceDir(data.get(position), holder.mAppName.getContext()), holder.mAppName.getContext())));
            holder.mSize.setText(holder.mAppName.getContext().getString(R.string.size, AppData.getAPKSize(AppData.getSourceDir(data.get(position), holder.mAppName.getContext()))));
            holder.mVersion.setTextColor(Color.RED);
            holder.mSize.setTextColor(APKEditorUtils.isDarkTheme(holder.mSize.getContext()) ? Color.GREEN : Color.BLACK);
            holder.mAppIcon.setOnClickListener(v -> {
                APKExplorer.mAppID = data.get(position);
                Intent imageView = new Intent(holder.mCard.getContext(), ImageViewActivity.class);
                holder.mCard.getContext().startActivity(imageView);
            });
            if (!APKEditorUtils.isDarkTheme(holder.mCard.getContext())) {
                holder.mCard.setCardBackgroundColor(Color.LTGRAY);
            }
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mVersion.setVisibility(View.VISIBLE);
            if (APKEditorUtils.isFullVersion(holder.mCard.getContext())) {
                holder.mCard.setOnLongClickListener(v -> {
                    new MaterialAlertDialogBuilder(holder.mCard.getContext())
                            .setMessage(holder.mCard.getContext().getString(R.string.sign_question, AppData.getAppName(data.get(position), holder.mAppName.getContext())))
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setPositiveButton(R.string.sign, (dialog, id) -> {
                                if (!APKEditorUtils.getBoolean("firstSigning", false, v.getContext())) {
                                    new MaterialAlertDialogBuilder(v.getContext()).setItems(v.getContext().getResources().getStringArray(
                                            R.array.signing), (dialogInterface, i) -> {
                                        APKEditorUtils.saveBoolean("firstSigning", true, v.getContext());
                                        switch (i) {
                                            case 0:
                                                APKData.signAPK(data.get(position), holder.mCard.getContext());
                                                break;
                                            case 1:
                                                Intent signing = new Intent(v.getContext(), APKSignActivity.class);
                                                v.getContext().startActivity(signing);
                                                break;
                                        }}).setCancelable(false)
                                            .setOnDismissListener(dialogInterface -> {
                                            }).show();
                                } else {
                                    APKData.signAPK(data.get(position), holder.mCard.getContext());
                                }
                            }).show();
                    return false;
                });
            }
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private AppCompatImageButton mAppIcon;
        private MaterialCardView mCard;
        private MaterialTextView mAppName, mAppID, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mCard = view.findViewById(R.id.card);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mAppName = view.findViewById(R.id.title);
            this.mAppID = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
            this.mVersion = view.findViewById(R.id.version);
        }

        @Override
        public void onClick(View view) {
            APKExplorer.exploreAPK(data.get(getAdapterPosition()), view.getContext());
        }
    }

}