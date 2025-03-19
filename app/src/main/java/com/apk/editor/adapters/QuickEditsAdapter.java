package com.apk.editor.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.SerializableItems.QuickEditsItems;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 11, 2025
 */
public class QuickEditsAdapter extends RecyclerView.Adapter<QuickEditsAdapter.ViewHolder> {

    private boolean mQuickEdited = false;
    private final List<QuickEditsItems> data;

    public QuickEditsAdapter(List<QuickEditsItems> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public QuickEditsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_quickedits, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickEditsAdapter.ViewHolder holder, int position) {
        holder.mText.setText(position == 0 ? getAppName(holder.mText.getContext()) : data.get(position).getValue());
        holder.mLayout.setHint(data.get(position).getName());
        holder.mCard.setOnClickListener(v -> AppData.toggleKeyboard(1, holder.mText, v.getContext()));

        if (position == 3 || position == 4) {
            holder.mText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        holder.mText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                holder.mText.setText(position == 0 ? getAppName(holder.mText.getContext()) : data.get(position).getValue());
                holder.mAction.setVisibility(View.GONE);
            }
        });

        holder.mText.addTextChangedListener(getTextWatcher(position, holder.mAction));

        holder.mAction.setOnClickListener(v -> apply(holder.mText, position));

        holder.mText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                apply(holder.mText, position);
                return true;
            }
            return false;
        });
    }

    private String getAppName(Context context) {
        return data.get(0).isEdited() ? data.get(0).getValue() : sPackageUtils.isPackageInstalled(data.get(1).getValue(), context) ? sPackageUtils.getAppName(data.get(1).getValue(), context).toString() : data.get(0).getValue();
    }

    private TextWatcher getTextWatcher(int position, MaterialButton materialButton) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean modified = s != null && !s.toString().trim().isEmpty() && !Objects.equals(data.get(position).getValue(), s.toString().trim());
                materialButton.setVisibility(modified ? View.VISIBLE : View.GONE);
            }
        };
    }

    private void apply(MaterialAutoCompleteTextView textView, int position) {
        if (textView.getText() != null && !textView.getText().toString().trim().equals(data.get(position).getValue())) {
            data.get(position).setValue(position == 1 && textView.getText().toString().trim().contains(" ") ? textView.getText().toString().trim().replace(" ", "") : textView.getText().toString().trim());
            mQuickEdited = true;
            notifyItemChanged(position);
        }
        textView.clearFocus();
    }

    public boolean isQuickEdited() {
        return mQuickEdited;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialAutoCompleteTextView mText;
        private final MaterialButton mAction;
        private final MaterialCardView mCard;
        private final TextInputLayout mLayout;

        public ViewHolder(View view) {
            super(view);
            this.mText = view.findViewById(R.id.text);
            this.mAction = view.findViewById(R.id.action);
            this.mCard = view.findViewById(R.id.card);
            this.mLayout = view.findViewById(R.id.layout);
        }
    }

}