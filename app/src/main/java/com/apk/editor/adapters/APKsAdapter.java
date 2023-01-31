package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.ShareBundleDialog;
import com.apk.editor.utils.dialogs.SignatureMismatchDialog;
import com.apk.editor.utils.tasks.SaveAPKtoDownloads;
import com.apk.editor.utils.tasks.SaveBundletoDownloads;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.Utils.sAPKUtils;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsAdapter extends RecyclerView.Adapter<APKsAdapter.ViewHolder> {

    private static List<String> data;

    public APKsAdapter(List<String> data) {
        APKsAdapter.data = data;
    }

    @NonNull
    @Override
    public APKsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull APKsAdapter.ViewHolder holder, int position) {
        try {
            if (new File(data.get(position)).isDirectory()) {
                if (sAPKUtils.getAPKIcon(data.get(position) + "/base.apk", holder.mAppName.getContext()) != null) {
                    holder.mAppIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position) + "/base.apk", holder.mAppName.getContext()));
                } else {
                    holder.mAppIcon.setImageDrawable(ContextCompat.getDrawable(holder.mAppIcon.getContext(), R.drawable.ic_android));
                    holder.mAppIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mAppIcon.getContext()));
                }
                if (Common.getSearchWord() != null && Common.isTextMatched(new File(data.get(position)).getName(), Common.getSearchWord())) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(new File(data.get(position)).getName().replace(Common.getSearchWord(),
                            "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(new File(data.get(position)).getName());
                }
                if (sAPKUtils.getPackageName(data.get(position) + "/base.apk", holder.mAppName.getContext()) == null) {
                    holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mCard.setOnClickListener(v -> sUtils.snackBar(v, v.getContext().getString(R.string.apk_corrupted)).show());
                }
                if (sAPKUtils.getVersionName(data.get(position) + "/base.apk", holder.mAppName.getContext()) != null) {
                    holder.mVersion.setText(holder.mVersion.getContext().getString(R.string.version, sAPKUtils.getVersionName(data.get(position) + "/base.apk", holder.mAppName.getContext())));
                }
                holder.mCard.setOnClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext()) && data.get(position).contains("_aee-signed") && !sUtils.getBoolean("signature_warning", false, v.getContext())) {
                        new SignatureMismatchDialog(v.getContext()).show();
                    } else {
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(v.getContext().getString(R.string.install_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) -> SplitAPKInstaller.installSplitAPKs(false, null, data.get(position) + "/base.apk", (Activity) v.getContext())).show();
                    }
                });
                holder.mCard.setOnLongClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        Menu menu = popupMenu.getMenu();
                        menu.add(Menu.NONE, 0, Menu.NONE, R.string.share);
                        menu.add(Menu.NONE, 1, Menu.NONE, R.string.save_to_downloads);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()) {
                                case 0:
                                    new ShareBundleDialog(data.get(position), holder.mCard.getContext()).show();
                                    break;
                                case 1:
                                    new SaveBundletoDownloads(data.get(position), true, v.getContext()).execute();
                                    break;
                            }
                            return false;
                        });
                        popupMenu.show();
                    } else {
                        new ShareBundleDialog(data.get(position), holder.mCard.getContext()).show();
                    }
                    return false;
                });
            } else {
                if (sAPKUtils.getAPKIcon(data.get(position), holder.mAppName.getContext()) != null) {
                    holder.mAppIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position), holder.mAppName.getContext()));
                } else {
                    holder.mAppIcon.setImageDrawable(ContextCompat.getDrawable(holder.mAppIcon.getContext(), R.drawable.ic_android));
                    holder.mAppIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mAppIcon.getContext()));
                }
                if (sAPKUtils.getAPKName(data.get(position), holder.mAppName.getContext()) != null) {
                    if (Common.getSearchWord() != null && Common.isTextMatched(Objects.requireNonNull(sAPKUtils.getAPKName(data.get(position), holder.mAppName.getContext())).toString(), Common.getSearchWord())) {
                        holder.mAppName.setText(APKEditorUtils.fromHtml(Objects.requireNonNull(sAPKUtils.getAPKName(data.get(position), holder.mAppName.getContext())).toString().replace(Common.getSearchWord(),
                                "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
                    } else {
                        holder.mAppName.setText(sAPKUtils.getAPKName(data.get(position), holder.mAppName.getContext()));
                    }
                } else {
                    if (Common.getSearchWord() != null && Common.isTextMatched(new File(data.get(position)).getName(), Common.getSearchWord())) {
                        holder.mAppName.setText(APKEditorUtils.fromHtml(new File(data.get(position)).getName().replace(Common.getSearchWord(),
                                "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
                    } else {
                        holder.mAppName.setText(new File(data.get(position)).getName());
                    }
                    holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mCard.setOnClickListener(v -> sUtils.snackBar(v, v.getContext().getString(R.string.apk_corrupted)).show());
                }
                if (!sUtils.isDarkTheme(holder.mCard.getContext())) {
                    holder.mCard.setCardBackgroundColor(Color.LTGRAY);
                }
                if (sAPKUtils.getVersionName(data.get(position), holder.mAppName.getContext()) != null) {
                    holder.mVersion.setText(holder.mVersion.getContext().getString(R.string.version, sAPKUtils.getVersionName(data.get(position), holder.mAppName.getContext())));
                }
                holder.mSize.setText(holder.mSize.getContext().getString(R.string.size, sAPKUtils.getAPKSize(data.get(position))));
                holder.mSize.setTextColor(sUtils.isDarkTheme(holder.mSize.getContext()) ? Color.GREEN : Color.BLACK);
                holder.mSize.setVisibility(View.VISIBLE);
                holder.mCard.setOnClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext()) && data.get(position).contains("_aee-signed.apk") && !sUtils.getBoolean("signature_warning", false, v.getContext())) {
                        new SignatureMismatchDialog(v.getContext()).show();
                    } else {
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(v.getContext().getString(R.string.install_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) -> SplitAPKInstaller.installAPK(false, new File(data.get(position)), (Activity) v.getContext())).show();
                    }
                });
                holder.mCard.setOnLongClickListener(v -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        Menu menu = popupMenu.getMenu();
                        menu.add(Menu.NONE, 0, Menu.NONE, R.string.share);
                        menu.add(Menu.NONE, 1, Menu.NONE, R.string.save_to_downloads);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()) {
                                case 0:
                                    APKData.shareFile(new File(data.get(position)), "application/java-archive", v.getContext());
                                    break;
                                case 1:
                                    new SaveAPKtoDownloads(new File(data.get(position)), v.getContext()).execute();
                                    break;
                            }
                            return false;
                        });
                        popupMenu.show();
                    } else {
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(v.getContext().getString(R.string.share_message, sAPKUtils.getAPKName(data.get(position), v.getContext())))
                                .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                                })
                                .setPositiveButton(v.getContext().getString(R.string.share), (dialog, id) ->
                                        APKData.shareFile(new File(data.get(position)), "application/java-archive", v.getContext())
                                ).show();
                    }
                    return false;
                });
            }
            holder.mVersion.setVisibility(View.VISIBLE);
            holder.mVersion.setTextColor(Color.RED);
        } catch (NullPointerException ignored) {
        }
        holder.mDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(v.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.delete, (dialog, id) -> {
                    sUtils.delete(new File(data.get(position)));
                    data.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                }).show());
        holder.mDelete.setColorFilter(Color.RED);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mAppIcon, mDelete;
        private final MaterialCardView mCard;
        private final MaterialTextView mAppName, mSize, mVersion;

        public ViewHolder(View view) {
            super(view);
            this.mCard = view.findViewById(R.id.card);
            this.mAppIcon = view.findViewById(R.id.icon);
            this.mDelete = view.findViewById(R.id.delete);
            this.mAppName = view.findViewById(R.id.title);
            this.mSize = view.findViewById(R.id.size);
            this.mVersion = view.findViewById(R.id.version);
        }
    }

}