package com.apk.editor.utils.Serializables;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apk.editor.R;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on July 13, 2026
 */
public class BatchItems implements Serializable {

    private boolean selected;
    private Drawable appIcon = null;
    private final String packageName;
    private String appName = null;

    public BatchItems(String packageName, boolean selected) {
        this.packageName = packageName;
        this.selected = selected;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void load(ImageButton button, TextView title, TextView description, CheckBox checkBox){
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                PackageManager pm = button.getContext().getPackageManager();
                ApplicationInfo ai;
                try {
                    ai = pm.getApplicationInfo(this.packageName, 0);
                    appName = pm.getApplicationLabel(ai).toString();
                    appIcon = pm.getApplicationIcon(ai);
                } catch (PackageManager.NameNotFoundException ignored) {
                }

                handler.post(() -> {
                    checkBox.setChecked(this.selected);

                    description.setText(this.packageName);

                    if (appIcon != null) {
                        button.setImageDrawable(appIcon);
                    } else {
                        button.setImageResource(R.drawable.ic_android_app);
                    }

                    if (appName != null) {
                        title.setText(appName);
                        title.setVisibility(VISIBLE);
                    } else {
                        title.setVisibility(GONE);
                    }
                });
            });
        }
    }

}