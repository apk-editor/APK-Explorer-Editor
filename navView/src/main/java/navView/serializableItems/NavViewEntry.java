package navView.serializableItems;

import static android.view.View.VISIBLE;
import static com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START;
import static com.google.android.material.button.MaterialButton.ICON_GRAVITY_TOP;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;

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

    /*
     * NavView with an icon on the left and a title on its bottom
     */
    public NavViewEntry(FragmentSupplier supplier, int drawableRes, String titles) {
        this.supplier = supplier;
        this.drawableRes = drawableRes;
        this.titles = titles;
    }

    /*
     * NavView with only an icon
     */
    public NavViewEntry(FragmentSupplier supplier, int drawableRes) {
        this.supplier = supplier;
        this.drawableRes = drawableRes;
        this.titles = null;
    }

    /*
     * NavView with only a title
     */
    public NavViewEntry(FragmentSupplier supplier, String titles) {
        this.supplier = supplier;
        this.drawableRes = Integer.MIN_VALUE;
        this.titles = titles;
    }

    public FragmentSupplier getSupplier() {
        return supplier;
    }

    public void load(MaterialButton button, boolean isSelected) {
        if (this.drawableRes != Integer.MIN_VALUE) {
            button.setIconResource(this.drawableRes);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            button.setTypeface(Typeface.DEFAULT);
        } else {
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            button.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        }
        if (this.titles != null) {
            button.setIconGravity(ICON_GRAVITY_TOP);
            button.setText(this.titles);
        } else {
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