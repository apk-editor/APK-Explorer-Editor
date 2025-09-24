package com.apk.editor.utils.dialogs;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 29, 2025
 */
public abstract class XMLEditorDialog extends MaterialAlertDialogBuilder {

    private static AlertDialog alertDialog = null;

    private static boolean modified;

    public XMLEditorDialog(XMLEntry xmlEntry, Context context) {
        super(context);
        View rootView = View.inflate(context, R.layout.layout_xml_editor, null);
        TextInputLayout layout = rootView.findViewById(R.id.text);
        MaterialAutoCompleteTextView value = rootView.findViewById(R.id.value);
        SwitchMaterial enable = rootView.findViewById(R.id.enable);
        MaterialButton delete = rootView.findViewById(R.id.delete);

        if (xmlEntry.isBoolean()) {
            enable.setText(xmlEntry.getTag());
            enable.setChecked(xmlEntry.isChecked());
            value.setVisibility(GONE);
            enable.setVisibility(VISIBLE);
        } else {
            layout.setHint(xmlEntry.getTag());
            value.setText(xmlEntry.getValue());
            value.setVisibility(VISIBLE);
            enable.setVisibility(GONE);
        }

        enable.setOnClickListener(v -> modified = enable.isChecked() != xmlEntry.isChecked());

        delete.setOnClickListener(v -> {
            removeLine();
            alertDialog.dismiss();
        });

        setView(rootView);
        setIcon(R.drawable.ic_edit);
        setTitle(R.string.xml_modify_title);
        setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        setPositiveButton(R.string.apply, (dialog, id) -> {
            if (modified || !value.getText().toString().trim().isEmpty() && !value.getText().toString().trim().equals(xmlEntry.getValue())) {
                modifyLine(enable.getVisibility() == VISIBLE ? enable.isChecked() ? "true" : "false" : value.getText().toString().trim());
            }
        });

        alertDialog = create();
        alertDialog.show();
    }

    public abstract void modifyLine(String newValue);

    public abstract void removeLine();

}