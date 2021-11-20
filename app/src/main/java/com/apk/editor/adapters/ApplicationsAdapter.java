package com.apk.editor.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.ImageViewActivity;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.recyclerViewItems.PackageItems;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sPackageUtils;
import in.sunilpaulmathew.sCommon.Utils.sPermissionUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {

    private static List<PackageItems> data;

    public ApplicationsAdapter(List<PackageItems> data) {
        ApplicationsAdapter.data = data;
    }

    @NonNull
    @Override
    public ApplicationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationsAdapter.ViewHolder holder, int position) {
        try {
            holder.mAppIcon.setImageDrawable(data.get(position).getAppIcon());
            if (Common.getSearchWord() != null && Common.isTextMatched(data.get(position).getPackageName(), Common.getSearchWord())) {
                holder.mAppID.setText(APKEditorUtils.fromHtml(data.get(position).getPackageName().replace(Common.getSearchWord(), "<b><i><font color=\"" +
                        Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
            } else {
                holder.mAppID.setText(data.get(position).getPackageName());
            }
            if (Common.getSearchWord() != null && Common.isTextMatched(data.get(position).getAppName(), Common.getSearchWord())) {
                holder.mAppName.setText(APKEditorUtils.fromHtml(data.get(position).getAppName().replace(Common.getSearchWord(),
                        "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
            } else {
                holder.mAppName.setText(data.get(position).getAppName());
            }
            holder.mVersion.setText(holder.mAppName.getContext().getString(R.string.version, data.get(position).getAppVersion()));
            holder.mSize.setText(holder.mAppName.getContext().getString(R.string.size, sAPKUtils.getAPKSize(sPackageUtils
                    .getSourceDir(data.get(position).getPackageName(), holder.mAppName.getContext()))));
            holder.mVersion.setTextColor(Color.RED);
            holder.mSize.setTextColor(sUtils.isDarkTheme(holder.mSize.getContext()) ? Color.GREEN : Color.BLACK);
            holder.mAppIcon.setOnClickListener(v -> {
                Common.setAppID(data.get(position).getPackageName());
                Intent imageView = new Intent(v.getContext(), ImageViewActivity.class);
                v.getContext().startActivity(imageView);
            });
            if (!sUtils.isDarkTheme(holder.mCard.getContext())) {
                holder.mCard.setCardBackgroundColor(Color.LTGRAY);
            }
            holder.mSize.setVisibility(View.VISIBLE);
            holder.mVersion.setVisibility(View.VISIBLE);
            holder.mCard.setOnLongClickListener(v -> {
                if (sPermissionUtils.isPermissionDenied(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, v.getContext()) && sUtils.getString("exportAPKsPath", "externalFiles",
                        v.getContext()).equals("internalStorage")) {
                    sPermissionUtils.requestPermission(
                            new String[] {
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },(Activity) v.getContext());
                    return true;
                }
                if (APKEditorUtils.isFullVersion(v.getContext())) {
                    if (sUtils.getString("exportAPKs", null, v.getContext()) == null) {
                        AppData.getExportOptionsMenu(data.get(position).getPackageName(), v.getContext()).show();
                    } else if (sUtils.getString("exportAPKs", null, v.getContext()).equals(v.getContext().getString(R.string.export_storage))) {
                        APKData.exportApp(data.get(position).getPackageName(), v.getContext());
                    } else {
                        if (!sUtils.getBoolean("firstSigning", false, v.getContext())) {
                            AppData.getSigningOptionsMenu(data.get(position).getPackageName(), v.getContext()).show();
                        } else {
                            APKData.reSignAPKs(data.get(position).getPackageName(), false, (Activity) v.getContext());
                        }
                    }
                } else {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setIcon(data.get(position).getAppIcon())
                            .setTitle(data.get(position).getAppName())
                            .setMessage(v.getContext().getString(R.string.export_app_question, data.get(position).getAppName()))
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setPositiveButton(R.string.export, (dialog, id) -> APKData.exportApp(data.get(position).getPackageName(), v.getContext())).show();
                }
                return false;
            });
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mAppIcon;
        private final MaterialCardView mCard;
        private final MaterialTextView mAppID, mAppName, mSize, mVersion;

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
            APKExplorer.exploreAPK(data.get(getAdapterPosition()).getPackageName(), view.getContext());
        }
    }

}