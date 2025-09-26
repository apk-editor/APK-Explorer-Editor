package com.apk.editor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.ResourceTableParser;
import com.apk.axml.serializableItems.ResEntry;
import com.apk.editor.R;
import com.apk.editor.adapters.StringViewAdapter;
import com.apk.editor.utils.AppSettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 25, 2025
 */
public class StringViewFragment extends Fragment {

    private String mResFilePath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) return;

        mResFilePath = arguments.getString("resFilePath");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.layout_recyclerview, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        
        new sExecutor() {
            private StringViewAdapter mAdapter;
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                mAdapter = new StringViewAdapter(getData());
            }

            private List<ResEntry> getData() {
                List<ResEntry> data = new ArrayList<>();
                try (FileInputStream fis = new FileInputStream(mResFilePath)) {
                    for (ResEntry entry : new ResourceTableParser(fis).parse()) {
                        if (entry.getName().startsWith("@string/") && entry.getValue() != null && !entry.getValue().isEmpty()) {
                            data.add(entry);
                        }
                    }
                } catch (IOException ignored) {
                }
                return data;
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(mAdapter);
            }
        }.execute();

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AppSettings.navigateToFragment(requireActivity(), 0);
            }
        });

        return mRootView;
    }
    
}