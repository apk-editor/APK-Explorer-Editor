package com.apk.editor.utils.dialogs;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.BottomMenuAdapter;
import com.apk.editor.utils.Serializables.MenuItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 26, 2026
 */
public abstract class BottomMenuDialog extends BottomSheetDialog {

    public BottomMenuDialog(List<MenuItems> menuItems, Bitmap bitmap, String titleString, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_bottom_menu, null);
        AppCompatImageButton icon = rootView.findViewById(R.id.icon);
        MaterialTextView title = rootView.findViewById(R.id.title);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        if (bitmap != null) {
            icon.setImageBitmap(bitmap);
            icon.setVisibility(VISIBLE);
        } else {
            icon.setVisibility(GONE);
        }

        if (titleString != null) {
            title.setText(titleString);
            title.setVisibility(VISIBLE);
        } else {
            title.setVisibility(GONE);
        }

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new BottomMenuAdapter(menuItems, id -> {
            onMenuItemClicked(id);
            dismiss();
        }));

        setContentView(rootView);

        show();
    }

    public abstract void onMenuItemClicked(int id);

}