package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public abstract class UnsupportedFileDialog extends BottomSheetDialog {

    public UnsupportedFileDialog(Drawable drawable, String fileName, String itemText, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_file_unsupported, null);

        AppCompatImageButton icon = rootView.findViewById(R.id.icon);
        MaterialButton open = rootView.findViewById(R.id.open);
        MaterialButton export = rootView.findViewById(R.id.export);
        MaterialTextView title = rootView.findViewById(R.id.title);
        MaterialTextView text = rootView.findViewById(R.id.text);

        icon.setImageDrawable(drawable);
        title.setText(fileName);
        text.setText(itemText);

        setContentView(rootView);

        open.setOnClickListener(v -> {
            onNegativeAction();
            dismiss();
        });

        export.setOnClickListener(v -> {
            onPositiveAction();
            dismiss();
        });

        show();
    }

    public abstract void onNegativeAction();

    public abstract void onPositiveAction();

}