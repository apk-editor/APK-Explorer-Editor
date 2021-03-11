package com.apk.editor.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.apk.editor.R;
import com.apk.editor.fragments.ApplicationsFragment;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 10, 2021
 */
public class DocumentationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        AppCompatImageButton mBack = findViewById(R.id.back);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DocumentationFragment()).commit();

        mBack.setOnClickListener(v -> finish());
    }

    public static class DocumentationFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            WebView mWebView = new WebView(requireActivity());
            mWebView.loadUrl("file:///android_asset/documentation.html");


            return mWebView;
        }

    }

}