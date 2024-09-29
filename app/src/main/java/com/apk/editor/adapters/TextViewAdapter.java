package com.apk.editor.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.apk.editor.utils.Common;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 */
public class TextViewAdapter extends RecyclerView.Adapter<TextViewAdapter.ViewHolder> {

    private static List<String> data;
    private static String searchWord;

    public TextViewAdapter(List<String> data, String searchWord) {
        TextViewAdapter.data = data;
        TextViewAdapter.searchWord = searchWord;
    }

    @NonNull
    @Override
    public TextViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_textview, parent, false);
        return new TextViewAdapter.ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TextViewAdapter.ViewHolder holder, int position) {
        if (searchWord != null && Common.isTextMatched(data.get(position), searchWord)) {
            holder.mText.setText(APKEditorUtils.fromHtml(data.get(position).replace(searchWord,
                    "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
        } else {
            holder.mText.setText(data.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mText;

        public ViewHolder(View view) {
            super(view);
            this.mText = view.findViewById(R.id.text);
        }
    }

}