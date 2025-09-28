package com.apk.editor.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.XMLEditor;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.apk.editor.utils.dialogs.ResEditorDialog;
import com.apk.editor.utils.dialogs.XMLEditorDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.Dialog.sSingleItemDialog;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 27, 2024
 */
public class XMLEditorAdapter extends RecyclerView.Adapter<XMLEditorAdapter.ViewHolder> {

    private final Activity activity;
    private final List<XMLEntry> data, originalData;
    private final List<ResEntry> resourceMap;
    private final MaterialButton saveButton;
    private final String filePath, rootPath, searchWord;
    private final OnPickImageListener pickImageListener;
    private static boolean isModified = false;

    public XMLEditorAdapter(List<XMLEntry> data, List<XMLEntry> originalData, List<ResEntry> resourceMap, OnPickImageListener listener, String filePath, String rootPath, String searchWord, MaterialButton saveButton, Activity activity) {
        this.data = data;
        this.originalData = originalData;
        this.resourceMap = resourceMap;
        this.pickImageListener = listener;
        this.filePath = filePath;
        this.rootPath = rootPath;
        this.searchWord = searchWord;
        this.saveButton = saveButton;
        this.activity = activity;
    }

    @NonNull
    @Override
    public XMLEditorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_xmleditor, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull XMLEditorAdapter.ViewHolder holder, int position) {
        if (searchWord == null || data.get(position).getText().contains(searchWord)) {
            holder.mText.setAlpha(data.get(position).getValue().isEmpty() ? (float) 0.5 : 1);
            holder.mText.setText(data.get(position).getText());
            holder.mText.setVisibility(VISIBLE);
        } else {
            holder.mText.setVisibility(GONE);
        }

        saveButton.setOnClickListener(v -> XMLEditor.encodeToBinaryXML(XMLEditor.xmlEntriesToXML(originalData, resourceMap), filePath, activity).execute());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final MaterialTextView mText;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mText = view.findViewById(R.id.text);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            if (!APKEditorUtils.isFullVersion(view.getContext()) || data.get(position).getValue().isEmpty()) return;
            if (data.get(position).getTag().trim().equals("android:label") || data.get(position).getValue().startsWith("res/")) {
                if (data.get(position).getTag().trim().equals("android:label")) {
                    new sSingleItemDialog(R.drawable.ic_image, view.getContext().getString(R.string.xml_editor_res_title),
                            new String[]{
                                    view.getContext().getString(R.string.xml_editor_res),
                                    view.getContext().getString(R.string.xml_editor_text)
                            }, view.getContext()) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            switch (itemPosition) {
                                case 0:
                                    chooseResDialog(position);
                                    break;
                                case 1:
                                    launchEditorDialog(position, view.getContext());
                                    break;
                            }
                        }
                    }.show();
                } else if (data.get(position).getTag().trim().equals("android:icon") || data.get(position).getTag().trim().equals("android:roundIcon")) {
                    new sSingleItemDialog(R.drawable.ic_image, view.getContext().getString(R.string.xml_editor_res_title),
                            !data.get(position).getValue().endsWith(".xml") ?
                                    new String[] {
                                            view.getContext().getString(R.string.xml_editor_res),
                                            view.getContext().getString(R.string.xml_editor_storage),
                                    } : new String[] {
                                    view.getContext().getString(R.string.xml_editor_res),
                            }, view.getContext()) {

                        @Override
                        public void onItemSelected(int itemPosition) {
                            switch (itemPosition) {
                                case  0:
                                    chooseResDialog(position);
                                    break;
                                case 1:
                                    if (!data.get(position).getValue().endsWith(".xml")) {
                                        if (pickImageListener != null) {
                                            pickImageListener.onPickImageRequested(data.get(position));
                                        }
                                    }
                                    break;
                            }
                        }
                    }.show();
                } else {
                    chooseResDialog(position);
                }
            } else {
                launchEditorDialog(position, view.getContext());
            }
        }
    }

    private void chooseResDialog(int position) {
        new ResEditorDialog(data.get(position), resourceMap, rootPath, activity) {
            @Override
            public void apply(String newValue) {
                modify(newValue, position, activity).execute();
            }
        };
    }

    private void launchEditorDialog(int position, Context context) {
        new XMLEditorDialog(data.get(position), context) {

            @Override
            public void modifyLine(String newValue) {
                modify(newValue, position, context).execute();
            }

            @Override
            public void removeLine() {
                new sExecutor() {
                    private boolean invalid = false;
                    private ProgressDialog progressDialog;

                    @Override
                    public void onPreExecute() {
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(context.getString(R.string.quick_edits_progress_message));
                        progressDialog.setIcon(R.mipmap.ic_launcher);
                        progressDialog.setIndeterminate(true);
                        progressDialog.show();
                    }

                    @Override
                    public void doInBackground() {
                        int positionOriginal = RecyclerView.NO_POSITION;

                        for (int i = 0; i < originalData.size(); i++) {
                            if (originalData.get(i).getId().equals(data.get(position).getId())) {
                                positionOriginal = i;
                                break;
                            }
                        }
                        if (positionOriginal == RecyclerView.NO_POSITION) return;

                        XMLEntry target = data.get(position);
                        if (target.getEndTag().trim().isEmpty()) {
                            data.set(position, new XMLEntry("", "", "", ""));
                            originalData.remove(positionOriginal);
                        } else {
                            XMLEntry entry = new XMLEntry("", "", "", data.get(position).getEndTag().replace("\"", ""));
                            data.set(position, entry);
                            originalData.set(positionOriginal, entry);
                        }

                        if (XMLEditor.isXMLValid(XMLEditor.xmlEntriesToXML(originalData, resourceMap))) {
                            if (!isModified) {
                                isModified = true;
                            }
                            invalid = false;
                        } else {
                            data.set(position, target);
                            originalData.set(positionOriginal, target);
                            invalid = true;
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        progressDialog.dismiss();
                        if (invalid) {
                            sCommonUtils.toast(context.getString(R.string.xml_corrupted), context).show();
                        } else {
                            saveButton.setVisibility(isModified ? VISIBLE : GONE);
                            notifyItemChanged(position);
                        }
                    }
                }.execute();
            }
        };
    }

    private sExecutor modify(String newValue, int position, Context context) {
        return new sExecutor() {
            private boolean invalid = false;
            private ProgressDialog progressDialog;
            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(context.getString(R.string.quick_edits_progress_message));
                progressDialog.setIcon(R.mipmap.ic_launcher);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
            }

            @Override
            public void doInBackground() {
                int positionOriginal = RecyclerView.NO_POSITION;

                for (int i = 0; i < originalData.size(); i++) {
                    if (originalData.get(i).getId().equals(data.get(position).getId())) {
                        positionOriginal = i;
                        break;
                    }
                }

                if (positionOriginal == RecyclerView.NO_POSITION) return;

                String oldValue = data.get(position).getValue();
                data.get(position).setValue(newValue);
                originalData.get(positionOriginal).setValue(newValue);

                if (XMLEditor.isXMLValid(XMLEditor.xmlEntriesToXML(originalData, resourceMap))) {
                    if (!isModified) {
                        isModified = true;
                    }
                    invalid = false;
                } else {
                    data.get(position).setValue(oldValue);
                    originalData.get(positionOriginal).setValue(oldValue);
                    invalid = true;
                }
            }

            @Override
            public void onPostExecute() {
                progressDialog.dismiss();
                if (invalid) {
                    sCommonUtils.toast(context.getString(R.string.xml_corrupted), context).show();
                } else {
                    saveButton.setVisibility(isModified ? VISIBLE : GONE);
                    notifyItemChanged(position);
                }
            }
        };
    }

    public interface OnPickImageListener {
        void onPickImageRequested(XMLEntry xmlEntry);
    }

}