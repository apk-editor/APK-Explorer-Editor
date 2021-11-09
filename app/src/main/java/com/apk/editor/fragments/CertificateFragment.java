package com.apk.editor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apk.editor.R;
import com.apk.editor.utils.APKCertificate;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.Common;
import com.apk.editor.utils.recyclerViewItems.APKItems;
import com.google.android.material.textview.MaterialTextView;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 */
public class CertificateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.textview_layout, container, false);

        MaterialTextView mText = mRootView.findViewById(R.id.text);

        APKItems apkData = APKExplorer.getAPKData(Common.getAPKFile().getAbsolutePath(), requireActivity());
        if (apkData != null) {
            try {
                if (APKCertificate.getCertificateDetails(Common.getAPKFile().getAbsolutePath(), requireActivity()) != null) {
                    mText.setText(APKCertificate.getCertificateDetails(Common.getAPKFile().getAbsolutePath(), requireActivity()));
                }
            } catch (Exception ignored) {
            }
        }

        return mRootView;
    }
    
}