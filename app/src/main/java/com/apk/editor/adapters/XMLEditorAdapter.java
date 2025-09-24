package com.apk.editor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.aXMLEncoder;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.dialogs.ResEditorDialog;
import com.apk.editor.utils.dialogs.XMLEditorDialog;
import com.google.android.material.textview.MaterialTextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 27, 2024
 */
public class XMLEditorAdapter extends RecyclerView.Adapter<XMLEditorAdapter.ViewHolder> {

    private final List<XMLEntry> data;
    private final List<ResEntry> resourceMap;
    private final String filePath, rootPath, searchWord;

    public XMLEditorAdapter(List<XMLEntry> data, List<ResEntry> resourceMap, String filePath, String rootPath, String searchWord) {
        this.data = data;
        this.resourceMap = resourceMap;
        this.filePath = filePath;
        this.rootPath = rootPath;
        this.searchWord = searchWord;
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
            holder.mText.setVisibility(View.VISIBLE);
        } else {
            holder.mText.setVisibility(View.GONE);
        }
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

        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            if (!APKEditorUtils.isFullVersion(view.getContext()) || data.get(position).getValue().isEmpty()) return;
            if (data.get(position).getTag().trim().equals("android:label") || data.get(position).getValue().startsWith("res/")) {
                new ResEditorDialog(data.get(position), resourceMap, rootPath, view.getContext()) {
                    @Override
                    public void apply(boolean editor, String newValue) {
                        if (editor) {
                            launchEditorDialog(position, view.getContext());
                        } else {
                            modify(newValue, position, view.getContext()).execute();
                        }
                    }
                };
            } else {
                launchEditorDialog(position, view.getContext());
            }
        }
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
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        data.remove(position);

                        StringBuilder sb = new StringBuilder();
                        for (XMLEntry items : data) {
                            if (!items.getTag().trim().equals("android:debuggable") && !items.getTag().trim().equals("android:testOnly")) {
                                sb.append(items.getText(resourceMap)).append("\n");
                            }
                        }

                        if (APKExplorer.isXMLValid(sb.toString().trim())) {
                            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                                aXMLEncoder aXMLEncoder = new aXMLEncoder();
                                byte[] bs = aXMLEncoder.encodeString(context, sb.toString().trim());
                                fos.write(bs);
                            } catch (IOException | XmlPullParserException ignored) {
                            }
                        } else  {
                            invalid = true;
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        if (invalid) {
                            sCommonUtils.toast(context.getString(R.string.xml_corrupted), context).show();
                        } else {
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, getItemCount());
                        }
                    }
                }.execute();
            }
        };
    }

    private sExecutor modify(String newValue, int position, Context context) {
        return new sExecutor() {
            private boolean invalid = false;
            @Override
            public void onPreExecute() {
            }

            private StringBuilder getStringBuilder() {
                StringBuilder sb = new StringBuilder();

                for (XMLEntry xmlEntry : data) {
                    if (!xmlEntry.getTag().trim().equals("android:debuggable") && !xmlEntry.getTag().trim().equals("android:testOnly")) {
                        sb.append(xmlEntry.getText(resourceMap)).append("\n");
                    }
                }

                return sb;
            }

            @Override
            public void doInBackground() {
                data.get(position).setValue(newValue);

                StringBuilder sb = getStringBuilder();

                if (APKExplorer.isXMLValid(sb.toString().trim())) {
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        aXMLEncoder aXMLEncoder = new aXMLEncoder();
                        byte[] bs = aXMLEncoder.encodeString(context, sb.toString().trim());
                        fos.write(bs);
                    } catch (IOException | XmlPullParserException ignored) {
                    }
                } else  {
                    invalid = true;
                }
            }

            @Override
            public void onPostExecute() {
                if (invalid) {
                    sCommonUtils.toast(context.getString(R.string.xml_corrupted), context).show();
                } else {
                    notifyItemChanged(position);
                }
            }
        };
    }

}