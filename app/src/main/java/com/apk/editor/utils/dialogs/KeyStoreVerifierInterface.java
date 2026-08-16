package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.text.Editable;
import android.view.View;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Oct 10, 2024
 */
public abstract class KeyStoreVerifierInterface extends MaterialAlertDialogBuilder {

    public KeyStoreVerifierInterface(String text, Context context) {
        super(context);
        View layout = View.inflate(context, R.layout.layout_keystoreverifier, null);
        MaterialAutoCompleteTextView editText = layout.findViewById(R.id.text);

        editText.setSingleLine(true);
        editText.requestFocus();

        setView(layout);

        if (text != null) {
            setTitle(text);
            setIcon(R.mipmap.ic_launcher);
        }
        setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        setPositiveButton(R.string.ok, (dialog, id) ->
                positiveButtonLister(editText.getText())
        );
    }

    public abstract void positiveButtonLister(Editable s);

}