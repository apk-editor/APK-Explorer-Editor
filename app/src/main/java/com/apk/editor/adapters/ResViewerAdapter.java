package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.aXMLUtils.Utils;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.editor.R;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.ResPatcher;
import com.apk.editor.utils.tasks.ExportToStorage;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 22, 2025
 */
public class ResViewerAdapter extends RecyclerView.Adapter<ResViewerAdapter.ViewHolder> {

    private final Activity activity;
    private final boolean clickable;
    private final List<ResEntry> data;
    private final String rootPath;
    private static ClickListener clickListener;

    public ResViewerAdapter(List<ResEntry> data, String rootPath, boolean clickable, Activity activity) {
        this.data = data;
        this.rootPath = rootPath;
        this.clickable = clickable;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ResViewerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_resviewer, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ResViewerAdapter.ViewHolder holder, int position) {
        String name = data.get(position).getName();
        String resAttr = data.get(position).getResAttr();
        String value = data.get(position).getValue();

        holder.mName.setText(resAttr);
        if (value != null) {
            holder.mValue.setText(value);
        } else {
            holder.mValue.setText(name);
        }

        if (value != null && (value.startsWith("res/"))) {
            if (value.endsWith(".xml")) {
                holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_xml, holder.mIcon.getContext()));
            } else if (APKExplorer.getIconFromPath(rootPath + "/" + value) != null) {
                holder.mIcon.setImageURI(APKExplorer.getIconFromPath(rootPath + "/" + value));
            } else {
                holder.mIcon.setImageDrawable(sCommonUtils.getDrawable(R.drawable.ic_image, holder.mIcon.getContext()));
            }
            holder.mIcon.setVisibility(VISIBLE);
        } else {
            holder.mIcon.setVisibility(GONE);
        }
    }

    private sExecutor resPatcher(String newText, ResEntry entry, int adapterPosition) {
        return new sExecutor() {
            private boolean success;
            @Override
            public void onPreExecute() {

            }

            @Override
            public void doInBackground() {
                try (FileInputStream fis = new FileInputStream(rootPath + "/resources.arsc")) {
                    byte[] arsc = Utils.toByteArray(fis);
                    success = ResPatcher.patch(arsc, entry, newText);
                    if (success) {
                        sFileUtils.copy(arsc, new File(rootPath + "/resources.arsc"));
                    }
                } catch (IOException ignored) {}
            }

            @Override
            public void onPostExecute() {
                if (success) {
                    data.set(adapterPosition, new ResEntry(entry.getResourceId(), entry.getName(), newText));
                    notifyItemChanged(adapterPosition);
                }
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AppCompatImageButton mIcon;
        private final MaterialTextView mName, mValue;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mName = view.findViewById(R.id.name);
            this.mValue = view.findViewById(R.id.value);
        }

        @SuppressLint("StringFormatInvalid")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            if (clickable) {
                clickListener.onItemClick(data.get(getBindingAdapterPosition()).getValue(), view);
            } else if (data.get(position).getValue() != null && (!data.get(position).getValue().startsWith("@") || !data.get(position).getValue().startsWith("?"))) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                Menu menu = popupMenu.getMenu();
                if (data.get(position).getValue().startsWith("res/")) {
                    menu.add(Menu.NONE, 0, Menu.NONE, view.getContext().getString(R.string.export_storage)).setIcon(R.drawable.ic_export);
                } else {
                    menu.add(Menu.NONE, 1, Menu.NONE, view.getContext().getString(R.string.update)).setIcon(R.drawable.ic_edit);
                }
                popupMenu.setForceShowIcon(true);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        if (Build.VERSION.SDK_INT < 29 && sPermissionUtils.isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE, view.getContext())) {
                            sPermissionUtils.requestPermission(
                                    new String[] {
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, activity);
                        } else {
                            new ExportToStorage(new File(rootPath, Objects.requireNonNull(data.get(position).getValue())), null, new File(rootPath).getName(), view.getContext()).execute();
                        }
                    } else if (item.getItemId() == 1) {
                        View rootView = View.inflate(activity, R.layout.layout_res_value_patcher, null);
                        MaterialAutoCompleteTextView newText = rootView.findViewById(R.id.new_text);
                        TextInputLayout newTextHint = rootView.findViewById(R.id.new_text_hint);

                        newTextHint.setHint(view.getContext().getString(R.string.res_add_new));
                        newText.setText(data.get(position).getValue());

                        newText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (s.toString().trim().length() > data.get(position).getValue().length()) {
                                    newText.setText(s.toString().trim().substring(0, data.get(position).getValue().length()));
                                    newText.setSelection(newText.getText().length());
                                    sCommonUtils.toast(R.string.res_patcher_warning, view.getContext()).show();
                                }
                            }
                        });

                        new MaterialAlertDialogBuilder(activity)
                                .setIcon(R.drawable.ic_edit)
                                .setTitle(view.getContext().getString(R.string.replace_question, data.get(position).getValue()))
                                .setCancelable(false)
                                .setView(rootView)
                                .setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
                                })
                                .setPositiveButton(R.string.apply, (dialogInterface, i) -> resPatcher(newText.getText().toString().trim(), data.get(position), position).execute()).show();
                    }
                    return false;
                });
                popupMenu.show();
            }
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        ResViewerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(String newValue, View v);
    }

}