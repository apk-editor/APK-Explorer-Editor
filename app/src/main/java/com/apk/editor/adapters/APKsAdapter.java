package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;
import java.util.Objects;

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
                if (APKData.getAppIcon(data.get(position) + "/base.apk", holder.mAppName.getContext()) != null) {
                    holder.mAppIcon.setImageDrawable(APKData.getAppIcon(data.get(position) + "/base.apk", holder.mAppName.getContext()));
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
                if (APKData.getAppID(data.get(position) + "/base.apk", holder.mAppName.getContext()) == null) {
                    holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mCard.setOnClickListener(v -> APKEditorUtils.snackbar(v, v.getContext().getString(R.string.apk_corrupted)));
                }
                if (APKData.getVersionName(data.get(position) + "/base.apk", holder.mAppName.getContext()) != null) {
                    holder.mVersion.setText(holder.mVersion.getContext().getString(R.string.version, APKData.getVersionName(data.get(position) + "/base.apk", holder.mAppName.getContext())));
                }
                holder.mCard.setOnClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext()) && data.get(position).contains("_aee-signed") && !APKEditorUtils.getBoolean("signature_warning", false, v.getContext())) {
                        APKData.showSignatureErrorDialog(v.getContext());
                    } else {
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setMessage(v.getContext().getString(R.string.install_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) -> SplitAPKInstaller.installSplitAPKs(null, data.get(position) + "/base.apk", (Activity) v.getContext())).show();
                    }
                });
                holder.mCard.setOnLongClickListener(v -> {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(v.getContext().getString(R.string.share_message, new File(data.get(position)).getName()))
                            .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(v.getContext().getString(R.string.share), (dialog, id) -> APKData.shareAppBundle(new File(data.get(position)).getName(), data.get(position), holder.mCard.getContext())).show();
                    return false;
                });
            } else {
                if (APKData.getAppIcon(data.get(position), holder.mAppName.getContext()) != null) {
                    holder.mAppIcon.setImageDrawable(APKData.getAppIcon(data.get(position), holder.mAppName.getContext()));
                } else {
                    holder.mAppIcon.setImageDrawable(ContextCompat.getDrawable(holder.mAppIcon.getContext(), R.drawable.ic_android));
                    holder.mAppIcon.setColorFilter(APKEditorUtils.getThemeAccentColor(holder.mAppIcon.getContext()));
                }
                if (APKData.getAppName(data.get(position), holder.mAppName.getContext()) != null) {
                    if (Common.getSearchWord() != null && Common.isTextMatched(Objects.requireNonNull(APKData.getAppName(data.get(position), holder.mAppName.getContext())).toString(), Common.getSearchWord())) {
                        holder.mAppName.setText(APKEditorUtils.fromHtml(Objects.requireNonNull(APKData.getAppName(data.get(position), holder.mAppName.getContext())).toString().replace(Common.getSearchWord(),
                                "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
                    } else {
                        holder.mAppName.setText(APKData.getAppName(data.get(position), holder.mAppName.getContext()));
                    }
                } else {
                    if (Common.getSearchWord() != null && Common.isTextMatched(new File(data.get(position)).getName(), Common.getSearchWord())) {
                        holder.mAppName.setText(APKEditorUtils.fromHtml(new File(data.get(position)).getName().replace(Common.getSearchWord(),
                                "<b><i><font color=\"" + Color.RED + "\">" + Common.getSearchWord() + "</font></i></b>")));
                    } else {
                        holder.mAppName.setText(new File(data.get(position)).getName());
                    }
                    holder.mAppName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.mCard.setOnClickListener(v -> APKEditorUtils.snackbar(v, v.getContext().getString(R.string.apk_corrupted)));
                }
                if (!APKEditorUtils.isDarkTheme(holder.mCard.getContext())) {
                    holder.mCard.setCardBackgroundColor(Color.LTGRAY);
                }
                if (APKData.getVersionName(data.get(position), holder.mAppName.getContext()) != null) {
                    holder.mVersion.setText(holder.mVersion.getContext().getString(R.string.version, APKData.getVersionName(data.get(position), holder.mAppName.getContext())));
                }
                holder.mSize.setText(holder.mSize.getContext().getString(R.string.size, AppData.getAPKSize(new File(data.get(position)).length())));
                holder.mSize.setTextColor(APKEditorUtils.isDarkTheme(holder.mSize.getContext()) ? Color.GREEN : Color.BLACK);
                holder.mSize.setVisibility(View.VISIBLE);
                holder.mCard.setOnClickListener(v -> {
                    if (APKEditorUtils.isFullVersion(v.getContext()) && data.get(position).contains("_aee-signed.apk") && !APKEditorUtils.getBoolean("signature_warning", false, v.getContext())) {
                        APKData.showSignatureErrorDialog(v.getContext());
                    } else {
                        new MaterialAlertDialogBuilder(v.getContext())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(v.getContext().getString(R.string.install_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) -> SplitAPKInstaller.installAPK(new File(data.get(position)), (Activity) v.getContext())).show();
                    }
                });
                holder.mCard.setOnLongClickListener(v -> {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(v.getContext().getString(R.string.share_message, APKData.getAppName(data.get(position), v.getContext())))
                            .setNegativeButton(v.getContext().getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(v.getContext().getString(R.string.share), (dialog, id) -> {
                                Uri uriFile = FileProvider.getUriForFile(v.getContext(),
                                        BuildConfig.APPLICATION_ID + ".provider", new File(data.get(position)));
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("application/java-archive");
                                share.putExtra(Intent.EXTRA_TEXT, v.getContext().getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                                share.putExtra(Intent.EXTRA_STREAM, uriFile);
                                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                v.getContext().startActivity(Intent.createChooser(share, v.getContext().getString(R.string.share_with)));
                            }).show();
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
                    APKEditorUtils.delete(data.get(position));
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