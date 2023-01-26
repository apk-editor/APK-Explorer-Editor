package com.apk.editor.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.apk.axml.aXMLDecoder;
import com.apk.axml.aXMLEncoder;
import com.apk.editor.R;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.Common;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class TextEditorActivity extends AppCompatActivity {

    private AppCompatEditText mText;
    private LinearLayoutCompat mProgressLayout;
    public static final String PATH_INTENT = "path";
    private String mTextContents = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texteditor);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mSave = findViewById(R.id.save);
        mProgressLayout = findViewById(R.id.progress_layout);
        MaterialTextView mTitle = findViewById(R.id.title);
        mText = findViewById(R.id.text);

        AppData.toggleKeyboard(1, mText, this);

        String mPath = getIntent().getStringExtra(PATH_INTENT);

        mText.setTextColor(sUtils.isDarkTheme(this) ? Color.WHITE : Color.BLACK);

        mTitle.setText(new File(mPath).getName());

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
                    try {
                        text = new aXMLDecoder(new File(mPath)).decode().trim();
                    } catch (Exception e) {
                        invalid = true;
                    }
                } else {
                    text = sUtils.read(new File(mPath));
                }
            }

            @Override
            public void onPostExecute() {
                if (text != null) {
                    mText.setText(text);
                    mTextContents = text;
                }
                if (invalid) {
                    sUtils.toast(getString(R.string.xml_decode_failed, new File(mPath).getName()), TextEditorActivity.this).show();
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
                                        if (isXMLValid(text)) {
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
                                        sUtils.create(text, new File(mPath));
                                        if (mPath.contains("classes") && mPath.contains(".dex")) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(sUtils.read(new File(getCacheDir(), Common.getAppID() + "/.aeeBackup/appData")));
                                                jsonObject.put("smali_edited", true);
                                                sUtils.create(jsonObject.toString(), new File(getCacheDir(), Common.getAppID() + "/.aeeBackup/appData"));
                                            } catch (JSONException ignored) {
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onPostExecute() {
                                    if (invalid) {
                                        sUtils.toast(getString(R.string.xml_corrupted), TextEditorActivity.this).show();
                                    }
                                    mProgressLayout.setVisibility(View.GONE);
                                    finish();
                                }
                            }.execute()
                    ).show();
        });

        mBack.setOnClickListener(v -> onBackPressed());
    }

    private static boolean isXMLValid(String xmlString) {
        try {
            SAXParserFactory.newInstance().newSAXParser().getXMLReader().parse(new InputSource(new StringReader(xmlString)));
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (mTextContents != null && mText.getText() != null && !mTextContents.equals(mText.getText().toString())) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.text_editor)
                    .setMessage(getString(R.string.discard_message))
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .setPositiveButton(R.string.discard, (dialogInterface, i) -> finish()).show();
            return;
        }
        super.onBackPressed();
    }

}