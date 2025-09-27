package com.apk.editor.utils;

import android.app.Activity;

import com.apk.axml.aXMLEncoder;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.axml.serializableItems.XMLEntry;
import com.apk.editor.R;
import com.apk.editor.utils.dialogs.ProgressDialog;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 11, 2025
 */
public class XMLEditor {

    public static boolean isXMLValid(String xmlString) {
        try {
            SAXParserFactory.newInstance().newSAXParser().getXMLReader().parse(new InputSource(new StringReader(xmlString)));
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false;
        }
    }

    public static String getExt(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        String normalized = filePath.replace("\\", "/");

        int lastSlash = normalized.lastIndexOf("/");
        String fileName = (lastSlash == -1) ? normalized : normalized.substring(lastSlash + 1);

        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDot + 1);
    }

    public static String xmlEntriesToXML(List<XMLEntry> xmlEntries, List<ResEntry> resEntries) {
        StringBuilder sb = new StringBuilder();

        for (XMLEntry xmlEntry : xmlEntries) {
            if (!xmlEntry.getTag().trim().equals("android:debuggable") && !xmlEntry.getTag().trim().equals("android:testOnly")) {
                if (resEntries != null && !resEntries.isEmpty()) {
                    sb.append(xmlEntry.getText(resEntries)).append("\n");
                } else {
                    sb.append(xmlEntry.getText()).append("\n");
                }
            }
        }

        return sb.toString().trim();
    }

    public static sExecutor encodeToBinaryXML(String xmlString, String filePath, Activity activity) {
        return new sExecutor() {
            boolean invalid = false;
            private ProgressDialog progressDialog;
            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle(activity.getString(R.string.saving));
                progressDialog.setIcon(R.mipmap.ic_launcher);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
            }

            @Override
            public void doInBackground() {
                if (XMLEditor.isXMLValid(xmlString)) {
                    invalid = false;
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        aXMLEncoder aXMLEncoder = new aXMLEncoder();
                        byte[] bs = aXMLEncoder.encodeString(activity, xmlString);
                        fos.write(bs);
                    } catch (IOException | XmlPullParserException ignored) {
                    }
                } else {
                    invalid = true;
                }

            }

            @Override
            public void onPostExecute() {
                progressDialog.dismiss();
                if (invalid) {
                    sCommonUtils.toast(activity.getString(R.string.xml_corrupted), activity).show();
                }
                activity.finish();
            }
        };
    }

}