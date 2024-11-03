package com.apk.editor.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.activities.XMLValuesEditorActivity;
import com.apk.editor.utils.APKEditorUtils;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 27, 2024
 */
public class XMLEditorAdapter extends RecyclerView.Adapter<XMLEditorAdapter.ViewHolder> {

    private static ArrayList<String> data;
    private static String searchWord, path;

    public XMLEditorAdapter(ArrayList<String> data, String path, String searchWord) {
        XMLEditorAdapter.data = data;
        XMLEditorAdapter.path = path;
        XMLEditorAdapter.searchWord = searchWord;
    }

    @NonNull
    @Override
    public XMLEditorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_xmleditor, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull XMLEditorAdapter.ViewHolder holder, int position) {
        if (searchWord == null || data.get(position).contains(searchWord)) {
            holder.mText.setText(data.get(position));
            holder.mText.setVisibility(View.VISIBLE);
        } else {
            holder.mText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final MaterialTextView mText;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mText = view.findViewById(R.id.text);
        }

        @Override
        public void onClick(View view) {
            if (APKEditorUtils.isFullVersion(view.getContext())) {
                Intent xmlValuesEditor = new Intent(view.getContext(), XMLValuesEditorActivity.class);
                xmlValuesEditor.putExtra(XMLValuesEditorActivity.POSITION_INTENT, getAdapterPosition());
                xmlValuesEditor.putStringArrayListExtra(XMLValuesEditorActivity.XML_INTENT, data);
                xmlValuesEditor.putExtra(XMLValuesEditorActivity.PATH_INTENT, path);
                view.getContext().startActivity(xmlValuesEditor);
            }
        }
    }

}