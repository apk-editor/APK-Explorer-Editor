package navView.serializables;

import static android.view.View.VISIBLE;
import static com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START;
import static com.google.android.material.button.MaterialButton.ICON_GRAVITY_TOP;

import android.content.res.ColorStateList;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.Serializable;

import navView.utils.NavViewUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class NavViewEntry implements Serializable {

    public interface FragmentSupplier {
        Fragment create();
    }

    private final transient FragmentSupplier supplier;
    private final int drawableRes;
    private final String titles;

    public NavViewEntry(FragmentSupplier supplier, int drawableRes, String titles) {
        this.supplier = supplier;
        this.drawableRes = drawableRes;
        this.titles = titles;
    }

    public NavViewEntry(FragmentSupplier supplier, int drawableRes) {
        this.supplier = supplier;
        this.drawableRes = drawableRes;
        this.titles = null;
    }

    public FragmentSupplier getSupplier() {
        return supplier;
    }

    public void load(MaterialButton button, boolean isSelected) {
        button.setIconResource(this.drawableRes);
        if (this.titles != null) {
            button.setIconSize(NavViewUtils.getButtonSize(24, button.getContext()));
            button.setIconGravity(ICON_GRAVITY_TOP);
            button.setText(this.titles);
        } else {
            button.setIconSize(NavViewUtils.getButtonSize(30, button.getContext()));
            button.setIconGravity(ICON_GRAVITY_TEXT_START);
        }
        int activeColor = NavViewUtils.getMaterialColorActive(button.getContext());
        int inactiveColor = NavViewUtils.getMaterialColorInactive(button.getContext());

        button.setIconTint(ColorStateList.valueOf(isSelected ? activeColor : inactiveColor));
        button.setTextColor(isSelected ? activeColor : inactiveColor);

        button.setChecked(isSelected);

        if (button.getVisibility() != VISIBLE) {
            button.setVisibility(VISIBLE);
        }
    }

}