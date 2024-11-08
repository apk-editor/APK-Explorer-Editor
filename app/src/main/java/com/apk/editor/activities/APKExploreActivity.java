package com.apk.editor.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.apk.editor.R;
import com.apk.editor.fragments.APKExplorerFragment;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer);

        AppCompatImageView mApplicationIcon = findViewById(R.id.app_image);
        MaterialButton mInfoButton = findViewById(R.id.info_button);
        MaterialTextView mApplicationName = findViewById(R.id.app_title);
        MaterialTextView mPackageName = findViewById(R.id.package_id);

        mApplicationIcon.setImageBitmap(APKExplorer.getAppIcon(Common.getPath() + "/.aeeBackup/appData"));
        mApplicationName.setText(APKExplorer.getAppName(Common.getPath() + "/.aeeBackup/appData"));
        mPackageName.setText(APKExplorer.getPackageName(Common.getPath() + "/.aeeBackup/appData"));
        mPackageName.setVisibility(View.VISIBLE);

        mInfoButton.setOnClickListener(v ->
                new sExecutor() {
                    private boolean mFailed = false;
                    private String mDescription = null;
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        try {
                            JSONObject jsonObject = APKExplorer.getAppData(Common.getPath() + "/.aeeBackup/appData");
                            mDescription = getString(R.string.sort_by_id) + ": " + Objects.requireNonNull(
                                    jsonObject).getString("package_name") + "\n\n" +
                                    jsonObject.getString("version_info") + "\n\n" +
                                    jsonObject.getString("sdk_minimum") + "\n\n" +
                                    jsonObject.getString("sdk_compiled") + "\n\n" +
                                    getString(R.string.certificate) + "\n" +
                                    jsonObject.getString("certificate_info");
                        } catch (JSONException ignored) {
                        }
                        if (mDescription == null || mDescription.isEmpty()) {
                            mFailed = true;
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        if (!mFailed) {
                            new MaterialAlertDialogBuilder(APKExploreActivity.this)
                                    .setIcon(mApplicationIcon.getDrawable())
                                    .setTitle(mApplicationName.getText())
                                    .setMessage(mDescription)
                                    .setPositiveButton(getString(R.string.cancel), (dialog, id) -> {
                                            }
                                    ).show();
                        }
                    }
                }.execute()
        );

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new APKExplorerFragment()).commit();
    }

}