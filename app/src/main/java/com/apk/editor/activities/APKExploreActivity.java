package com.apk.editor.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.apk.editor.R;
import com.apk.editor.fragments.APKExplorerFragment;
import com.apk.editor.utils.APKExplorer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class APKExploreActivity extends AppCompatActivity {

    public static final String BACKUP_PATH_INTENT = "backup_path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apkexplorer);

        AppCompatImageView mApplicationIcon = findViewById(R.id.app_image);
        MaterialButton mInfoButton = findViewById(R.id.info_button);
        MaterialTextView mApplicationName = findViewById(R.id.app_title);
        MaterialTextView mPackageName = findViewById(R.id.package_id);

        String mBackupFilePath = getIntent().getStringExtra(BACKUP_PATH_INTENT);
        if (APKExplorer.getAppIcon(mBackupFilePath) != null) {
            mApplicationIcon.setImageBitmap(APKExplorer.getAppIcon(mBackupFilePath));
        }
        mApplicationName.setText(APKExplorer.getAppName(mBackupFilePath));
        mPackageName.setText(APKExplorer.getPackageName(mBackupFilePath));
        mPackageName.setVisibility(View.VISIBLE);

        if (sFileUtils.exist(new File(Objects.requireNonNull(mBackupFilePath))) && APKExplorer.getAppData(mBackupFilePath) != null) {
            mInfoButton.setVisibility(View.VISIBLE);
        }

        mInfoButton.setOnClickListener(v -> new sExecutor() {
                    private boolean mFailed = false;
                    private String mDescription = null;
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        try {
                            JSONObject jsonObject = APKExplorer.getAppData(mBackupFilePath);
                            mDescription = getString(R.string.sort_by_id) + ": " + Objects.requireNonNull(
                                    jsonObject).getString("package_name") + "\n\n" +
                                    jsonObject.getString("version_info") + "\n\n" +
                                    jsonObject.getString("sdk_minimum") + "\n\n" +
                                    jsonObject.getString("sdk_compiled") + "\n\n" +
                                    getString(R.string.certificate) + "\n" +
                                    jsonObject.getString("certificate_info");
                        } catch (JSONException ignored) {}
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
                                    .setPositiveButton(getString(R.string.cancel), (dialog, id) -> {}
                                    ).show();
                        }
                    }
                }.execute()
        );

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                getMainFragment(mBackupFilePath, mPackageName.getText().toString().trim())).commit();
    }

    private Fragment getMainFragment(String backupFilePath, String packageName) {
        Bundle bundle = new Bundle();
        bundle.putString("backupFilePath", backupFilePath);
        bundle.putString("packageName", packageName);

        Fragment fragment = new APKExplorerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

}