package com.apk.editor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.utils.APKData;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.SignatureCheck;
import com.apk.editor.utils.SplitAPKInstaller;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class RecycleViewApksAdapter extends RecyclerView.Adapter<RecycleViewApksAdapter.ViewHolder> {

    private static List<String> data;

    public RecycleViewApksAdapter(List<String> data) {
        RecycleViewApksAdapter.data = data;
    }

    @NonNull
    @Override
    public RecycleViewApksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_apks, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecycleViewApksAdapter.ViewHolder holder, int position) {
        try {
            if (new File(data.get(position)).isDirectory()) {
                holder.mAppIcon.setImageDrawable(APKData.getAppIcon(data.get(position) + "/base.apk", holder.mAppName.getContext()));
                holder.mAppName.setText(new File(data.get(position)).getName());
                holder.mVersion.setText(holder.mAppName.getContext().getString(R.string.version, APKData.getVersionName(data.get(position) + "/base.apk", holder.mAppName.getContext())));
                holder.mCard.setOnClickListener(v -> {
                    if (!SignatureCheck.isPackageInstalled(APKData.getAppID(data.get(position) + "/base.apk", holder.mAppName.getContext()).toString(),
                            holder.mAppName.getContext()) || SignatureCheck.isPackageInstalled(APKData.getAppID(data.get(position) + "/base.apk",
                            holder.mAppName.getContext()).toString(), holder.mAppName.getContext()) && SignatureCheck.isSignatureMatched(APKData.getAppID(
                                    data.get(position) + "/base.apk", holder.mAppName.getContext()).toString(), holder.mAppName.getContext())) {
                        new MaterialAlertDialogBuilder(holder.mCard.getContext())
                                .setMessage(holder.mCard.getContext().getString(R.string.install_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) -> {
                                    SplitAPKInstaller.installSplitAPKs(data.get(position) + "/base.apk", (Activity) holder.mCard.getContext());
                                }).show();
                    } else {
                        SignatureCheck.showSignatureErrorDialog(APKData.getAppIcon(data.get(position) + "/base.apk", holder.mAppName.getContext()), APKData.getAppName(data.get(position) + "/base.apk", holder.mAppName.getContext()).toString(), holder.mAppName.getContext());
                    }
                });
                holder.mDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(holder.mDelete.getContext())
                        .setMessage(holder.mDelete.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        })
                        .setPositiveButton(R.string.delete, (dialog, id) -> {
                            APKEditorUtils.delete(data.get(position));
                            data.remove(position);
                            notifyDataSetChanged();
                        }).show());
            } else {
                holder.mAppIcon.setImageDrawable(APKData.getAppIcon(data.get(position), holder.mAppName.getContext()));
                if (APKData.mSearchText != null && APKData.getAppName(data.get(position), holder.mAppName.getContext()).toString().toLowerCase().contains(APKData.mSearchText)) {
                    holder.mAppName.setText(APKEditorUtils.fromHtml(APKData.getAppName(data.get(position), holder.mAppName.getContext()).toString().toLowerCase().replace(APKData.mSearchText,
                            "<b><i><font color=\"" + Color.RED + "\">" + APKData.mSearchText + "</font></i></b>")));
                } else {
                    holder.mAppName.setText(APKData.getAppName(data.get(position), holder.mAppName.getContext()));
                }
                if (!APKEditorUtils.isDarkTheme(holder.mCard.getContext())) {
                    holder.mCard.setCardBackgroundColor(Color.LTGRAY);
                }
                holder.mVersion.setText(holder.mAppName.getContext().getString(R.string.version, APKData.getVersionName(data.get(position), holder.mAppName.getContext())));
                holder.mSize.setText(holder.mAppName.getContext().getString(R.string.size, AppData.getAPKSize(data.get(position))));
                holder.mSize.setTextColor(APKEditorUtils.isDarkTheme(holder.mSize.getContext()) ? Color.GREEN : Color.BLACK);
                holder.mSize.setVisibility(View.VISIBLE);
                holder.mCard.setOnClickListener(v -> {
                    if (!SignatureCheck.isPackageInstalled(APKData.getAppID(data.get(position), holder.mAppName.getContext()).toString(), holder.mAppName.getContext())
                            || SignatureCheck.isPackageInstalled(APKData.getAppID(data.get(position), holder.mAppName.getContext()).toString(), holder.mAppName.getContext())
                            && SignatureCheck.isSignatureMatched(APKData.getAppID(data.get(position), holder.mAppName.getContext()).toString(), holder.mAppName.getContext())) {
                        new MaterialAlertDialogBuilder(holder.mCard.getContext())
                                .setMessage(holder.mCard.getContext().getString(R.string.install_question, new File(data.get(position)).getName()))
                                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                })
                                .setPositiveButton(R.string.install, (dialog, id) -> {
                                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri uriFile;
                                    uriFile = FileProvider.getUriForFile(holder.mCard.getContext(), BuildConfig.APPLICATION_ID + ".provider",
                                            new File(data.get(position)));
                                    intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
                                    holder.mCard.getContext().startActivity(Intent.createChooser(intent, ""));
                                }).show();
                    } else {
                        SignatureCheck.showSignatureErrorDialog(APKData.getAppIcon(data.get(position), holder.mAppName.getContext()), APKData.getAppName(data.get(position), holder.mAppName.getContext()).toString(), holder.mAppName.getContext());
                    }
                });
                holder.mDelete.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(holder.mDelete.getContext())
                            .setMessage(holder.mDelete.getContext().getString(R.string.delete_question, new File(data.get(position)).getName()))
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            })
                            .setPositiveButton(R.string.delete, (dialog, id) -> {
                                APKEditorUtils.delete(data.get(position));
                                data.remove(position);
                                notifyDataSetChanged();
                            }).show();
                });
                holder.mCard.setOnLongClickListener(v -> {
                    new MaterialAlertDialogBuilder(holder.mCard.getContext())
                            .setMessage(holder.mCard.getContext().getString(R.string.share_message, APKData.getAppName(data.get(position), holder.mAppName.getContext())))
                            .setNegativeButton(holder.mCard.getContext().getString(R.string.cancel), (dialog, id) -> {
                            })
                            .setPositiveButton(holder.mCard.getContext().getString(R.string.share), (dialog, id) -> {
                                Uri uriFile = FileProvider.getUriForFile(holder.mCard.getContext(),
                                        BuildConfig.APPLICATION_ID + ".provider", new File(data.get(position)));
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("application/java-archive");
                                share.putExtra(Intent.EXTRA_TEXT, holder.mCard.getContext().getString(R.string.share_summary, BuildConfig.VERSION_NAME));
                                share.putExtra(Intent.EXTRA_STREAM, uriFile);
                                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                holder.mCard.getContext().startActivity(Intent.createChooser(share, holder.mCard.getContext().getString(R.string.share_with)));
                            }).show();
                    return false;
                });
            }
            holder.mVersion.setVisibility(View.VISIBLE);
            holder.mDelete.setColorFilter(Color.RED);
            holder.mVersion.setTextColor(Color.RED);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageButton mAppIcon, mDelete;
        private MaterialCardView mCard;
        private MaterialTextView mAppName, mSize, mVersion;

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