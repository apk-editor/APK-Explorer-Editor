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

import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

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
            holder.mNumber.setText(String.valueOf(position + 1));
            holder.mText.setText(APKEditorUtils.fromHtml(data.get(position).replace(searchWord,
                    "<b><i><font color=\"" + Color.RED + "\">" + searchWord + "</font></i></b>")));
        } else {
            holder.mNumber.setText(String.valueOf(position + 1));
            holder.mText.setText(data.get(position));
        }
        holder.mNumber.setTextColor(Color.MAGENTA);
        if (data.get(position).contains("<manifest") || data.get(position).contains("</manifest>")) {
            holder.mText.setTextColor(APKEditorUtils.getThemeAccentColor(holder.mText.getContext()));
        } else if (data.get(position).trim().matches("<uses-permission|</uses-permission>")) {
            holder.mText.setTextColor(Color.RED);
        } else if (data.get(position).trim().matches("<activity|</activity>") || data.get(position).startsWith(".method") || data.get(position).startsWith(".annotation")) {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.GREEN : Color.MAGENTA);
        } else if (data.get(position).trim().matches("<service|</service>") || data.get(position).startsWith(".end method") || data.get(position).startsWith(".end annotation")) {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.MAGENTA : Color.BLUE);
        } else if (data.get(position).trim().matches("<provider|</provider>")) {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.LTGRAY : Color.DKGRAY);
        } else {
            holder.mText.setTextColor(sThemeUtils.isDarkTheme(holder.mText.getContext()) ? Color.WHITE : Color.BLACK);
        }
        if (data.get(position).startsWith("#")) {
            holder.mText.setAlpha((float) 0.5);
        } else {
            holder.mText.setAlpha(1);
        }
        holder.mNumber.setAlpha((float) 0.5);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView mNumber, mText;

        public ViewHolder(View view) {
            super(view);
            this.mNumber = view.findViewById(R.id.number);
            this.mText = view.findViewById(R.id.text);
        }
    }

}