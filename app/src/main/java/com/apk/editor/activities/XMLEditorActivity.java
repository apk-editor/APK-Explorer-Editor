package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.ResourceTableParser;
import com.apk.axml.aXMLDecoder;
import com.apk.axml.aXMLEncoder;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.XMLEditorAdapter;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.XMLEditor;
import com.apk.editor.utils.dialogs.ProgressDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on October 27, 2024
 */
public class XMLEditorActivity extends AppCompatActivity {

    private List<XMLEntry> mData;
    private List<ResEntry> mResourceMap;
    private ContentLoadingProgressBar mProgress;
    private RecyclerView mRecyclerView;
    private String mPath = null, mResPath = null, mSearchText = null;
    public static final String PATH_INTENT = "path", RESOURCE_PATH_INTENT = "resource_path";
    private XMLEditorAdapter mAdapter;
    private XMLEntry mXMLEntry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmleditor);

        mProgress = findViewById(R.id.progress);
        MaterialAutoCompleteTextView mSearch = findViewById(R.id.search);
        MaterialButton mSave = findViewById(R.id.save);
        MaterialTextView mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPath = getIntent().getStringExtra(PATH_INTENT);
        mResPath = getIntent().getStringExtra(RESOURCE_PATH_INTENT);

        if (mPath != null && sFileUtils.exist(new File(mPath))) {
            mTitle.setText(new File(mPath).getName());
            mTitle.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.GONE);
        }

        loadUI(mSearchText).execute();

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadUI(s.toString().toLowerCase()).execute();
            }
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (mSave.getVisibility() == View.VISIBLE) {
                    new MaterialAlertDialogBuilder(XMLEditorActivity.this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.discard_message)
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .setPositiveButton(R.string.discard, (dialogInterface, i) -> finish())
                            .show();
                    return;
                }
                if (mSearchText != null) {
                    mSearch.setText(null);
                }
                finish();
            }
        });
    }

    private sExecutor loadUI(String searchText) {
        return new sExecutor() {
            private boolean failed;

            @Override
            public void onPreExecute() {
                mRecyclerView.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
            }

            private List<ResEntry> getResourceMap() {
                try (FileInputStream fis = new FileInputStream(mResPath)) {
                    ResourceTableParser parser = new ResourceTableParser(fis);
                    return parser.parse();
                } catch (IOException ignored) {
                    return null;
                }
            }

            @Override
            public void doInBackground() {
                mResourceMap = getResourceMap();
                try (FileInputStream fis = new FileInputStream(mPath)) {
                    if (mResourceMap != null) {
                        mData = new aXMLDecoder(fis, mResourceMap).decode();
                    } else {
                        mData = new aXMLDecoder(fis).decode();
                    }
                } catch (IOException | XmlPullParserException ignored) {
                }

                if (mData != null && !mData.isEmpty()) {
                    mAdapter = new XMLEditorAdapter(mData, mResourceMap,
                            (xmlEntry) -> {
                                mXMLEntry = xmlEntry;

                                Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                try {
                                    chooseImage.launch(pickImage);
                                } catch (ActivityNotFoundException ignored) {
                                }
                            }, mPath, mResPath.replace("/resources.arsc", ""), searchText, XMLEditorActivity.this);
                    failed = false;
                } else {
                    failed = true;
                }
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onPostExecute() {
                mProgress.setVisibility(View.GONE);
                if (failed) {
                    sCommonUtils.toast(getString(R.string.xml_decode_failed, new File(mPath).getName()), mRecyclerView.getContext()).show();
                    finish();
                } else {
                    mSearchText = searchText;
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    @SuppressLint("StringFormatInvalid")
    private final ActivityResultLauncher<Intent> chooseImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    new sExecutor() {
                        private Activity activity;
                        private Bitmap bitmapNew = null;
                        private ProgressDialog mProgressDialog;
                        private String tag;
                        private Uri imageUri = null;

                        @Override
                        public void onPreExecute() {
                            activity = XMLEditorActivity.this;
                            mProgressDialog = new ProgressDialog(activity);
                            mProgressDialog.setTitle(activity.getString(R.string.loading));
                            mProgressDialog.setIcon(R.mipmap.ic_launcher);
                            mProgressDialog.setIndeterminate(true);
                            mProgressDialog.show();
                        }

                        private Bitmap loadBitmap(Uri uri) {
                            try {
                                return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                            } catch (IOException ignored) {
                            }
                            return null;
                        }

                        private String getRef() {
                            for (ResEntry resEntry : mResourceMap) {
                                if (mXMLEntry.getValue().equals(resEntry.getValue())) {
                                    return resEntry.getName();
                                }
                            }
                            return null;
                        }

                        private List<String> getRefs() {
                            List<String> refs = new ArrayList<>();
                            for (ResEntry resEntry : mResourceMap) {
                                if (resEntry.getName().equals(getRef()) && resEntry.getValue() != null) {
                                    refs.add(resEntry.getValue());
                                }
                            }
                            return refs;
                        }

                        @Override
                        public void doInBackground() {
                            Intent data = result.getData();
                            Uri uri = data.getData();
                            bitmapNew = loadBitmap(uri);
                            tag = mXMLEntry.getTag().trim();

                            String path = mResPath.replace("resources.arsc", getRefs().get(0));
                            imageUri = APKExplorer.getIconFromPath(path);
                        }

                        @Override
                        public void onPostExecute() {
                            View rootView = View.inflate(activity, R.layout.layout_imagepicker, null);
                            AppCompatImageView imageViewOld = rootView.findViewById(R.id.oldIcon);
                            AppCompatImageView imageViewNew = rootView.findViewById(R.id.newIcon);

                            if (imageUri != null) {
                                imageViewOld.setImageURI(imageUri);
                            }
                            imageViewNew.setImageBitmap(bitmapNew);
                            mProgressDialog.dismiss();
                            new MaterialAlertDialogBuilder(activity)
                                    .setIcon(R.drawable.ic_edit)
                                    .setTitle(getString(R.string.replace_question, tag))
                                    .setCancelable(false)
                                    .setView(rootView)
                                    .setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
                                    })
                                    .setPositiveButton(R.string.apply, (dialogInterface, i) ->
                                            new sExecutor() {
                                                private ProgressDialog mProgressDialog;
                                                @Override
                                                public void onPreExecute() {
                                                    mProgressDialog = new ProgressDialog(activity);
                                                    mProgressDialog.setTitle(activity.getString(R.string.quick_edits_progress_message));
                                                    mProgressDialog.setIcon(R.mipmap.ic_launcher);
                                                    mProgressDialog.setIndeterminate(true);
                                                    mProgressDialog.show();
                                                }

                                                private String updatedXMLString(FileInputStream fis, String newTxt) throws XmlPullParserException, IOException {
                                                    List<XMLEntry> xmlEntries = new aXMLDecoder(fis, mResourceMap).decode();
                                                    StringBuilder sb = new StringBuilder();
                                                    for (XMLEntry entry : xmlEntries) {
                                                        if (entry.getTag().trim().equals("android:drawable") && entry.getValue().startsWith("res/")) {
                                                            sb.append(entry.getTag()).append(entry.getMiddleTag()).append(newTxt).append(entry.getEndTag()).append("\n");
                                                        } else {
                                                            sb.append(entry.getText(mResourceMap)).append("\n");
                                                        }
                                                    }
                                                    return sb.toString().trim();
                                                }

                                                private void encodeXML(String xml, String path) {
                                                    try (FileOutputStream fos = new FileOutputStream(path)) {
                                                        aXMLEncoder aXMLEncoder = new aXMLEncoder();
                                                        byte[] bs = aXMLEncoder.encodeString(activity, xml);
                                                        fos.write(bs);
                                                    } catch (IOException | XmlPullParserException ignored) {
                                                    }
                                                }

                                                @Override
                                                public void doInBackground() {
                                                    mProgressDialog.setMax(getRefs().size());
                                                    for (String paths : getRefs()) {
                                                        String filePath = mResPath.replace("resources.arsc", paths);
                                                        if (filePath.endsWith(".xml")) {
                                                            try (FileInputStream fis = new FileInputStream(filePath)) {
                                                                encodeXML(updatedXMLString(fis, getRefs().get(0)), filePath);
                                                            } catch (IOException |
                                                                     XmlPullParserException ignored) {}
                                                        } else {
                                                            Uri fileUri = APKExplorer.getIconFromPath(filePath);
                                                            String fileExt = XMLEditor.getExt(filePath);

                                                            try {
                                                                Bitmap bitmap;
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                                    ImageDecoder.Source source = ImageDecoder.createSource(activity.getContentResolver(), Objects.requireNonNull(fileUri));
                                                                    bitmap = ImageDecoder.decodeBitmap(source);
                                                                } else {
                                                                    bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), fileUri);
                                                                }
                                                                if (bitmap == null) return;

                                                                int width = bitmap.getWidth() > 0 ? bitmap.getWidth() : 1;
                                                                int height = bitmap.getHeight() > 0 ? bitmap.getHeight() : 1;

                                                                Bitmap scaled = Bitmap.createScaledBitmap(bitmapNew, width, height, true);
                                                                FileOutputStream out = new FileOutputStream(filePath);
                                                                scaled.compress(fileExt.equalsIgnoreCase("webp") ? Bitmap.CompressFormat.WEBP : fileExt.equalsIgnoreCase("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, out);
                                                                out.flush();
                                                                out.close();
                                                            } catch (IOException ignored) {
                                                            }
                                                        }
                                                        mProgressDialog.updateProgress(1);
                                                    }
                                                }

                                                @Override
                                                public void onPostExecute() {
                                                    mProgressDialog.dismiss();
                                                }
                                            }.execute()
                                    ).show();
                        }
                    }.execute();
                }
            }
    );

}