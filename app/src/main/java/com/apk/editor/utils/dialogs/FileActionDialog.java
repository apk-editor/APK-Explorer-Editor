package com.apk.editor.utils.dialogs;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public abstract class FileActionDialog extends MaterialAlertDialogBuilder {

    private AlertDialog alertDialog;

    public FileActionDialog(Drawable drawable, String itemTitle, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_file_action, null);

        AppCompatImageButton icon = rootView.findViewById(R.id.icon);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialButton action = rootView.findViewById(R.id.action);
        MaterialTextView title = rootView.findViewById(R.id.title);

        if (drawable != null) {
            icon.setImageDrawable(drawable);
            icon.setVisibility(VISIBLE);
        } else {
            icon.setVisibility(GONE);
        }
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize(itemTitle.length()));
        title.setText(itemTitle);

        setView(rootView);

        cancel.setOnClickListener(v -> alertDialog.dismiss());

        action.setOnClickListener(v -> {
            onPositiveAction();
            alertDialog.dismiss();
        });

        alertDialog = create();
        alertDialog.show();
    }

    private int getTextSize(int length) {
        if (length >= 100) {
            return 15;
        } else if (length >= 50) {
            return 17;
        } else {
            return 20;
        }
    }

    public abstract void onPositiveAction();

}