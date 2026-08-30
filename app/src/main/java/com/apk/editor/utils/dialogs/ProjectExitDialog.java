package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import com.apk.editor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public abstract class ProjectExitDialog extends MaterialAlertDialogBuilder {

    public ProjectExitDialog(Bitmap bitmap, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_project_exit, null);

        AppCompatImageButton icon = rootView.findViewById(R.id.icon);
        MaterialButton discard = rootView.findViewById(R.id.discard);
        MaterialButton save = rootView.findViewById(R.id.save);
        MaterialTextView title = rootView.findViewById(R.id.title);

        icon.setImageBitmap(bitmap);
        title.setText(R.string.save_projects_question);

        setView(rootView);

        discard.setOnClickListener(v -> onDiscard());

        save.setOnClickListener(v -> onSave());

        show();
    }

    public abstract void onDiscard();

    public abstract void onSave();

}