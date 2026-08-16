package com.apk.editor.utils.dialogs;

import android.content.Context;

import com.apk.editor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Oct 10, 2024
 */
public abstract class KeyStoreAliasChoiceDialog extends MaterialAlertDialogBuilder {

    public KeyStoreAliasChoiceDialog(String text, String[] singleChoiceItems, int position, Context context) {
        super(context);

        if (text != null) {
            setTitle(text);
            setIcon(R.mipmap.ic_launcher);
        }
        setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        setPositiveButton(R.string.select, (dialog, id) -> onItemSelected(position)
        );
        setSingleChoiceItems(singleChoiceItems, position, (dialog, itemPosition) -> {
            onItemSelected(itemPosition);
            dialog.dismiss();
        });
    }

    public abstract void onItemSelected(int position);
}