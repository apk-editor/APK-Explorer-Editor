package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.ContentLoadingProgressBar;

import com.apk.axml.aXMLDecoder;
import com.apk.axml.aXMLEncoder;
import com.apk.editor.R;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.ThemeUtils.sThemeUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditorActivity extends AppCompatActivity {

    private ContentLoadingProgressBar mProgressLayout;
    private MaterialAutoCompleteTextView mText;
    public static final String PACKAGE_NAME_INTENT = "package_name", PATH_INTENT = "path";
    private String mTextContents = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        AppCompatImageButton mBack = findViewById(R.id.back);
        MaterialButton mSave = findViewById(R.id.save);
        mProgressLayout = findViewById(R.id.progress);
        MaterialTextView mTitle = findViewById(R.id.title);
        mText = findViewById(R.id.text);

        AppData.toggleKeyboard(1, mText, this);

        String mPath = getIntent().getStringExtra(PATH_INTENT);
        String packageName = getIntent().getStringExtra(PACKAGE_NAME_INTENT);

        mText.setTextColor(sThemeUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        mTitle.setText(new File(Objects.requireNonNull(mPath)).getName());

        new sExecutor() {
            private boolean invalid = false;
            private String text = null;
            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                if (APKExplorer.isBinaryXML(mPath)) {
                    try (FileInputStream fis = new FileInputStream(mPath)) {
                        text = new aXMLDecoder(fis).decodeAsString();
                    } catch (Exception e) {
                        invalid = true;
                    }
                } else {
                    text = sFileUtils.read(new File(mPath));
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                if (text != null) {
                    mText.setText(text);
                    mTextContents = text;
                }
                if (invalid) {
                    sCommonUtils.toast(getString(R.string.xml_decode_failed, new File(mPath).getName()), TextEditorActivity.this).show();
                }
                mProgressLayout.setVisibility(View.GONE);
            }
        }.execute();

        mSave.setVisibility(View.VISIBLE);

        mSave.setOnClickListener(v -> {
            if (mText == null || mText.getText() != null && mText.getText().toString().isEmpty()) return;
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.save_question)
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    })
                    .setPositiveButton(getString(R.string.save), (dialog, id) ->
                            new sExecutor() {
                                private boolean invalid = false;
                                private final String text = mText.getText().toString().trim();
                                @Override
                                public void onPreExecute() {
                                    mProgressLayout.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void doInBackground() {
                                    if (APKExplorer.isBinaryXML(mPath)) {
                                        if (APKExplorer.isXMLValid(text)) {
                                            try (FileOutputStream fos = new FileOutputStream(mPath)) {
                                                aXMLEncoder aXMLEncoder = new aXMLEncoder();
                                                byte[] bs = aXMLEncoder.encodeString(TextEditorActivity.this, text);
                                                fos.write(bs);
                                            } catch (IOException | XmlPullParserException ignored) {
                                            }
                                        } else {
                                            invalid = true;
                                        }
                                    } else {
                                        sFileUtils.create(text, new File(mPath));
                                        if (mPath.contains("classes") && mPath.endsWith(".smali")) {
                                            try {
                                                File backupFile = new File(getCacheDir(), packageName + "/.aeeBackup/appData");
                                                JSONObject jsonObject = new JSONObject(sFileUtils.read(backupFile));
                                                jsonObject.put("smali_edited", true);
                                                sFileUtils.create(jsonObject.toString(), backupFile);
                                            } catch (JSONException ignored) {
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onPostExecute() {
                                    if (invalid) {
                                        sCommonUtils.toast(getString(R.string.xml_corrupted), TextEditorActivity.this).show();
                                    }
                                    mProgressLayout.setVisibility(View.GONE);
                                    finish();
                                }
                            }.execute()
                    ).show();
        });

        mBack.setOnClickListener(v -> exit());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                exit();
            }
        });
    }

    private void exit() {
        if (mTextContents != null && mText.getText() != null && !mTextContents.equals(mText.getText().toString())) {
            new MaterialAlertDialogBuilder(TextEditorActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.text_editor)
                    .setMessage(getString(R.string.discard_message))
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .setPositiveButton(R.string.discard, (dialogInterface, i) -> finish()).show();
            return;
        }
        finish();
    }

}