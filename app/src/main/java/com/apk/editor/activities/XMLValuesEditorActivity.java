package com.apk.editor.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.aXMLEncoder;
import com.apk.editor.R;
import com.apk.editor.adapters.XMLValueEditorAdapter;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.SerializableItems.XMLItems;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 27, 2024
 */
public class XMLValuesEditorActivity extends AppCompatActivity {

    private boolean mTitleTextParsed = false, mWhitespaceParsed = false;
    private int mPosition;
    private List<String> mXMLData;
    private List<XMLItems> mData;
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private String mEndText = ">", mPath, mTitleText = null, mWhiteSpace = null;
    private XMLValueEditorAdapter mAdapter;
    public static final String POSITION_INTENT = "position", PATH_INTENT = "text", XML_INTENT = "xml";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmlvalueseditor);

        MaterialButton mApply = findViewById(R.id.apply_icon);
        MaterialButton mReset = findViewById(R.id.reset_icon);
        MaterialButton mDelete = findViewById(R.id.delete_icon);
        mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPosition = getIntent().getIntExtra(POSITION_INTENT, Integer.MIN_VALUE);
        mXMLData = getIntent().getStringArrayListExtra(XML_INTENT);
        mPath = getIntent().getStringExtra(PATH_INTENT);

        mReset.setOnClickListener(v -> loadUI().execute());

        mApply.setOnClickListener(v -> updateString().execute());

        mDelete.setOnClickListener(v -> new sExecutor() {
                    private boolean invalid = false;
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mXMLData.size(); i++) {
                            if (i != mPosition) {
                                sb.append(mXMLData.get(i)).append("\n");
                            }
                        }
                        if (APKExplorer.isXMLValid(sb.toString().trim())) {
                            try (FileOutputStream fos = new FileOutputStream(mPath)) {
                                aXMLEncoder aXMLEncoder = new aXMLEncoder();
                                byte[] bs = aXMLEncoder.encodeString(XMLValuesEditorActivity.this, sb.toString().trim());
                                fos.write(bs);
                            } catch (IOException | XmlPullParserException ignored) {
                            }
                            mXMLData.remove(mPosition);
                        } else {
                            invalid = true;
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        if (invalid) {
                            sCommonUtils.toast(getString(R.string.xml_corrupted), XMLValuesEditorActivity.this).show();
                        } else {
                            Common.isReloading(true);
                            finish();
                        }
                    }
                }.execute()
        );

        loadUI().execute();
    }

    private static boolean isSupportedString(String text) {
        return text.contains("app:") || text.contains("android:") || text.contains("class=")
                || text.contains(":duration") || text.contains(":fromAlpha") || text.contains(":interpolator")
                || text.contains("layout") || text.contains("name") || text.contains("package") || text.contains("path")
                || text.contains("platformBuild") || text.contains("style") || text.contains(":toAlpha");
    }

    private sExecutor loadUI() {
        return new sExecutor() {

            @Override
            public void onPreExecute() {
                mData = new ArrayList<>();
            }

            @Override
            public void doInBackground() {
                for (String line : Objects.requireNonNull(mXMLData.get(mPosition)).split("\n")) {
                    if (line.trim().startsWith("<")) {
                        mData.add(new XMLItems(line, null, false, false));
                        if (!mTitleTextParsed) {
                            mTitleText = line.replace("<", "");
                            mTitleTextParsed = true;
                        }
                    } else if (isSupportedString(line)) {
                        String[] splits = line.trim().split("=");
                        mData.add(new XMLItems(splits[0], splits[1].replace("\"", "")
                                .replace(">","").replace("/", ""), (splits[1].startsWith("\"true") || splits[1].startsWith("\"false")), false));
                        if (!mWhitespaceParsed) {
                            mWhiteSpace = line.replace(line.trim(), "");
                            mWhitespaceParsed = true;
                        }
                        if (splits[splits.length - 1].endsWith("/>")) {
                            mEndText = "/>";
                        }
                    }
                }
                mAdapter = new XMLValueEditorAdapter(mData);
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mAdapter);
                if (mTitleText == null) {
                    mTitle.setText(R.string.app_name);
                } else {
                    mTitle.setText(mTitleText);
                }
            }
        };
    }

    private sExecutor updateString() {
        return new sExecutor() {
            private boolean invalid = false;
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                StringBuilder sb = new StringBuilder();
                for (XMLItems line : mData) {

                    if (!line.isRemoved()) {
                        sb.append(line.getValue() == null ? line.getID() : mWhiteSpace + line.getID() + "=\"" + line.getValue() + "\"").append("\n");
                    }
                }

                StringBuilder xmlString = new StringBuilder();
                for (int i = 0; i < mXMLData.size(); i++) {
                    if (i == mPosition) {
                        xmlString.append(sb.append(mEndText)).append("\n");
                    } else {
                        xmlString.append(mXMLData.get(i)).append("\n");
                    }
                }

                if (APKExplorer.isXMLValid(xmlString.toString().trim())) {
                    try (FileOutputStream fos = new FileOutputStream(mPath)) {
                        aXMLEncoder aXMLEncoder = new aXMLEncoder();
                        byte[] bs = aXMLEncoder.encodeString(XMLValuesEditorActivity.this, xmlString.toString().trim());
                        fos.write(bs);
                    } catch (IOException | XmlPullParserException ignored) {
                    }
                    mXMLData.set(mPosition, sb.toString().trim());
                } else {
                    invalid = true;
                }
            }

            @Override
            public void onPostExecute() {
                if (invalid) {
                    sCommonUtils.toast(getString(R.string.xml_corrupted), XMLValuesEditorActivity.this).show();
                    loadUI().execute();
                } else {
                    Common.isReloading(true);
                    finish();
                }
            }
        };
    }

}