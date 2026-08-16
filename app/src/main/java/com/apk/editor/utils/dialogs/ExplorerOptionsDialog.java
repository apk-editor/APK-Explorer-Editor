package com.apk.editor.utils.dialogs;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ExploreOptionsAdapter;
import com.apk.editor.utils.Serializables.ExploreOptionsItems;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 26, 2026
 */
public abstract class ExplorerOptionsDialog extends BottomSheetDialog {

    public ExplorerOptionsDialog(List<ExploreOptionsItems> menuItems, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_explorer_options, null);

        MaterialTextView title = rootView.findViewById(R.id.title);
        MaterialTextView description = rootView.findViewById(R.id.description);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new ExploreOptionsAdapter(menuItems, id -> {
            onMenuItemClicked(id);
            dismiss();
        }));
        setContentView(rootView);
        show();
    }

    public abstract void onMenuItemClicked(int id);

}