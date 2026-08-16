package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ResViewerOptionsAdapter;
import com.apk.editor.utils.Serializables.ResViewerOptionsItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public abstract class ResViewerOptionsDialog extends BottomSheetDialog {

    public ResViewerOptionsDialog(List<ResViewerOptionsItems> data, String selected, Context context) {
        super(context);
        View rootView = View.inflate(context, R.layout.layout_resvieweroptions, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new ResViewerOptionsAdapter(data, selected, this::OnSelected));

        setContentView(rootView);
        show();
    }

    public abstract void OnSelected(String text);

}