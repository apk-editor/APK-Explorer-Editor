package com.apk.editor.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.recyclerViewItems.XMLItems;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 31, 2024
 */
public class XMLValueEditorAdapter extends RecyclerView.Adapter<XMLValueEditorAdapter.ViewHolder> {

    private static List<XMLItems> data;

    public XMLValueEditorAdapter(List<XMLItems> data) {
        XMLValueEditorAdapter.data = data;
    }

    @NonNull
    @Override
    public XMLValueEditorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_xmleditorvalues, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull XMLValueEditorAdapter.ViewHolder holder, int position) {
        if (data.get(position).getValue() != null) {
            holder.mID.setText(data.get(position).getID());
            if (data.get(position).isBoolean()) {
                holder.mSwitch.setChecked(data.get(position).getValue().equals("true"));
                holder.mSwitch.setVisibility(View.VISIBLE);
                holder.mValue.setVisibility(View.GONE);
            } else {
                holder.mValue.setText(data.get(position).getValue());
                holder.mValue.setVisibility(View.VISIBLE);
                holder.mSwitch.setVisibility(View.GONE);
            }
            holder.mID.setVisibility(View.VISIBLE);
            holder.mDelete.setVisibility(View.VISIBLE);
            if (data.get(position).isRemoved()) {
                holder.mValue.setEnabled(false);
            }
        } else {
            holder.mID.setVisibility(View.GONE);
            holder.mValue.setVisibility(View.GONE);
            holder.mDelete.setVisibility(View.GONE);
            holder.mSwitch.setVisibility(View.GONE);
        }

        holder.mValue.addTextChangedListener(gettTextWatcher(position));

        holder.mDelete.setOnClickListener(v -> {
            data.get(position).setToRemove(!data.get(position).isRemoved());
            holder.mID.setEnabled(!data.get(position).isRemoved());
            holder.mValue.setEnabled(!data.get(position).isRemoved());
            holder.mSwitch.setEnabled(!data.get(position).isRemoved());
        });

        holder.mSwitch.setOnClickListener(v -> data.get(position).setValue(data.get(position).getValue().equals("true") ? "false" : "true"));
    }

    private TextWatcher gettTextWatcher(int position) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    data.get(position).setValue(s.toString().trim());
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialAutoCompleteTextView mValue;
        private final MaterialButton mDelete;
        private final MaterialTextView mID;
        private final SwitchMaterial mSwitch;

        public ViewHolder(View view) {
            super(view);
            this.mID = view.findViewById(R.id.text);
            this.mValue = view.findViewById(R.id.value);
            this.mSwitch = view.findViewById(R.id.enable);
            this.mDelete = view.findViewById(R.id.delete);
        }
    }

}