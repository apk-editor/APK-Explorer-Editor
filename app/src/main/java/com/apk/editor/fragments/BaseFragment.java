package com.apk.editor.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on August 13, 2026
 */
public abstract class BaseFragment extends Fragment {

    protected OnBackPressedCallback onBackPressedCallback;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (onBackPressedCallback != null) {
            onBackPressedCallback.setEnabled(!hidden);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (onBackPressedCallback != null) {
            onBackPressedCallback.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (onBackPressedCallback != null) {
            onBackPressedCallback.setEnabled(true);
        }
    }

}