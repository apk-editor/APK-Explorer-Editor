package navView.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;

import bottomNavView.R;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class NavViewUtils {

    public static int getButtonSize(int sizeInDp, Context context) {
        return (int) (sizeInDp * context.getResources().getDisplayMetrics().density);
    }

    public static int getMaterialColorActive(Context context) {
        return MaterialColors.getColor(DynamicColors.wrapContextIfAvailable(context,
                        com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight),
                androidx.appcompat.R.attr.colorPrimary,
                ContextCompat.getColor(context, R.color.colorSelectedDefault));
    }

    public static int getMaterialColorInactive(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int baseColor = typedValue.data;

        return ColorUtils.setAlphaComponent(baseColor, Math.round(255 * 0.76f));
    }

}