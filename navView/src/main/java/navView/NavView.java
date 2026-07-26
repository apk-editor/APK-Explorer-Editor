package navView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import bottomNavView.R;
import navView.adapters.NavViewAdapter;
import navView.serializableItems.NavViewEntry;
import navView.utils.NavViewUtils;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class NavView extends FrameLayout {

    private final Context context;
    private final MaterialButton extraButton;
    private final RecyclerView recyclerView;
    private NavViewAdapter adapter;
    private OnItemSelectedListener itemSelectedListener;

    public NavView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        View view = inflate(context, R.layout.layout_bottomnav, this);
        recyclerView = view.findViewById(R.id.recycler_view);
        extraButton = view.findViewById(R.id.button);

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    }

    private Fragment getOrCreateFragment(NavViewEntry.FragmentSupplier supplier, int position) {
        if (!(getContext() instanceof FragmentActivity) || supplier == null) {
            return null;
        }

        FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();
        String tag = "nav_tab_" + position;

        Fragment fragment = fm.findFragmentByTag(tag);

        if (fragment == null || fragment.isDetached()) {
            fragment = supplier.create();
        }

        return fragment;
    }

    public void setNavigationItems(List<NavViewEntry> entries) {
        adapter = new NavViewAdapter(entries, (fragmentSupplier, position) -> {
            if (itemSelectedListener != null) {
                Fragment fragment = getOrCreateFragment(fragmentSupplier, position);

                if (fragment != null) {
                    itemSelectedListener.onItemSelected(fragment, position);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public interface OnMenuButtonClickedListener {
        void onMenuClicked();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Fragment fragment, int position);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }

    public void setOnMenuButtonClicked(int iconRes, OnMenuButtonClickedListener listener) {
        extraButton.setIconResource(iconRes);
        extraButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClicked();
            }
        });
        extraButton.setIconTint(ColorStateList.valueOf(NavViewUtils.getMaterialColorInactive(context)));
        extraButton.setVisibility(VISIBLE);
        extraButton.setCheckable(false);
    }

    public void setSelectedPosition(int position) {
        if (adapter != null) {
            adapter.setSelectedPosition(position);

            NavViewEntry entry = adapter.getNavViewEntry(position);
            if (entry != null && itemSelectedListener != null) {
                Fragment fragment = getOrCreateFragment(entry.getSupplier(), position);
                if (fragment != null) {
                    itemSelectedListener.onItemSelected(fragment, position);
                }
            }
        }
    }

}