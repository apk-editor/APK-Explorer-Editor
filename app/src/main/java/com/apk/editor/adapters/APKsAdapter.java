package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SplitAPKInstaller;
import com.apk.editor.utils.dialogs.ShareBundleDialog;
import com.apk.editor.utils.dialogs.SignatureMismatchDialog;
import com.apk.editor.utils.menus.APKOptionsMenu;
import com.apk.editor.utils.menus.BundleOptionsMenu;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.APKUtils.sAPKUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKsAdapter extends RecyclerView.Adapter<APKsAdapter.ViewHolder> {

    private static List<String> data;
    private static String searchWord;

    public APKsAdapter(List<String> data, String searchWord) {
        APKsAdapter.data = data;
        APKsAdapter.searchWord = searchWord;
    }

    @NonNull
    @Override
    public APKsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull APKsAdapter.ViewHolder holder, int position) {
        try {
            if (new File(data.get(position)).isDirectory()) {
                if (sAPKUtils.getAPKIcon(data.get(position) + "/" + APKData.getBaseAPKName(new File(data.get(position)), holder.mAppIcon.getContext()), holder.mAppName.getContext()) != null) {
                    holder.mAppIcon.setImageDrawable(sAPKUtils.getAPKIcon(data.get(position) + "/" + APKData.getBaseAPKName(new File(data.get(position)), holder.mAppIcon.getContext()), holder.mAppName.getContext()));
                } else {
                    holder.mAppIcon.setImageDrawable(ContextCompat.getDrawable(holder.mAppIcon.getContext(), R.drawable.ic_android));
                    holder.mAppIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mAppIcon.getContext()));
                }
                if (searchWord != null && Common.isTextMatched(new File(data.get(position)).getName(), searchWord)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(new File(data.get(position)).getName().replace(searchWord,
                            "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(new File(data.get(position)).getName());
                }
                if (sAPKUtils.getPackageName(data.get(position) + "/" + APKData.getBaseAPKName(new File(data.get(position)), holder.mAppName.getContext()), holder.mAppName.getContext()) == null) {
                    holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mCard.setOnClickListener(v -> sCommonUtils.snackBar(v, v.getContext().getString(R.string.apk_corrupted)).show());
                }
                if (sAPKUtils.getVersionName(data.get(position) + "/" + APKData.getBaseAPKName(new File(data.get(position)), holder.mAppName.getContext()), holder.mAppName.getContext()) != null) {
                    holder.mVersion.setText(holder.mVersion.getContext().getString(R.string.version, sAPKUtils.getVersionName(data.get(position) + "/" + APKData.getBaseAPKName(new File(data.get(position)), holder.mVersion.getContext()), holder.mVersion.getContext())));
                }
                holder.mCard.setOnClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext())) {
                        if (data.get(position).contains("_aee-signed") && !sCommonUtils.getBoolean("signature_warning", false, v.getContext())) {
                            new SignatureMismatchDialog(v.getContext()).show();
                        } else {
                            new MaterialAlertDialogBuilder(v.getContext())
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.app_name)
                                    .setMessage(v.getContext().getString(R.string.install_question, new File(data.get(position)).getName()))
                                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                    })
                                    .setPositiveButton(R.string.install, (dialog, id) -> SplitAPKInstaller.installSplitAPKs(false, null,
                                            data.get(position) + "/" + APKData.getBaseAPKName(new File(data.get(position)), v.getContext()), (Activity) v.getContext())
                                    ).show();
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            new BundleOptionsMenu(data.get(position), v);
                        } else {
                            new ShareBundleDialog(data.get(position), holder.mCard.getContext()).show();
                        }
                    }
                });
                holder.mCard.setOnLongClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext())) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            new BundleOptionsMenu(data.get(position), v);
                        } else {
                            new ShareBundleDialog(data.get(position), holder.mCard.getContext()).show();
                        }
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
                    if (searchWord != null && Common.isTextMatched(Objects.requireNonNull(sAPKUtils.getAPKName(data.get(position), holder.mAppName.getContext())).toString(), searchWord)) {
                        holder.mAppName.setText(APKEditorUtils.fromHtml(Objects.requireNonNull(sAPKUtils.getAPKName(data.get(position), holder.mAppName.getContext())).toString().replace(searchWord,
                                "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
                    } else {
                        holder.mAppName.setText(sAPKUtils.getAPKName(data.get(position), holder.mAppName.getContext()));
                    }
                } else {
                    if (searchWord != null && Common.isTextMatched(new File(data.get(position)).getName(),searchWord)) {
                        holder.mAppName.setText(APKEditorUtils.fromHtml(new File(data.get(position)).getName().replace(searchWord,
                                "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
                    } else {
                        holder.mAppName.setText(new File(data.get(position)).getName());
                    }
                    holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mCard.setOnClickListener(v -> sCommonUtils.snackBar(v, v.getContext().getString(R.string.apk_corrupted)).show());
                }
                if (sAPKUtils.getVersionName(data.get(position), holder.mAppName.getContext()) != null) {
                    holder.mVersion.setText(holder.mVersion.getContext().getString(R.string.version, sAPKUtils.getVersionName(data.get(position), holder.mAppName.getContext())));
                }
                holder.mSize.setText(holder.mSize.getContext().getString(R.string.size, sAPKUtils.getAPKSize(new File(data.get(position)).length())));
                holder.mSize.setVisibility(View.VISIBLE);
                holder.mCard.setOnClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext())) {
                        if (data.get(position).contains("_aee-signed.apk") && !sCommonUtils.getBoolean("signature_warning", false, v.getContext())) {
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
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            new APKOptionsMenu(new File(data.get(position)), v);
                        } else {
                            APKData.shareFile(new File(data.get(position)), "application/java-archive", v.getContext());
                        }
                    }
                });
                holder.mCard.setOnLongClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext())) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            new APKOptionsMenu(new File(data.get(position)), v);
                        } else {
                            APKData.shareFile(new File(data.get(position)), "application/java-archive", v.getContext());
                        }
                    }
                    return false;
                });
            }
            holder.mVersion.setVisibility(View.VISIBLE);
        } catch (NullPointerException ignored) {
        }
        holder.mDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.app_name)
                .setMessage(v.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                })
                .setPositiveButton(R.string.delete, (dialog, id) -> {
                    sFileUtils.delete(new File(data.get(position)));
                    data.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, data.size());
                }).show());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton mAppIcon;
        private final MaterialButton mDelete;
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