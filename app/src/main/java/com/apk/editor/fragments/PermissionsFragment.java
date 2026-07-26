package com.apk.editor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.axml.APKParser;
import com.apk.editor.R;
import com.apk.editor.adapters.ExploredInfoAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.PermissionUtils.sPermissionUtils;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on November 07, 2021
 */
public class PermissionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.layout_recyclerview, container, false);

        RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        new sExecutor() {
            private ExploredInfoAdapter adapter;
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                adapter = new ExploredInfoAdapter(getData());
            }

            private List<HashMap<String, String>> getData() {
                List<HashMap<String, String>> data = new ArrayList<>();
                APKParser mAPKParser = new APKParser();

                if (mAPKParser.getPermissions() != null) {
                    try {
                        for (String permission : mAPKParser.getPermissions()) {
                            data.add(new HashMap<>() {{
                                         put("title", permission);
                                         put("description", sPermissionUtils.getDescription(getNameAdjusted(
                                                 permission.replace("android.permission.", "")), requireActivity()));
                                     }}
                            );
                        }
                    } catch (Exception ignored) {
                    }
                }
                return data;
            }

            @Override
            public void onPostExecute() {
                recyclerView.setAdapter(adapter);
            }
        }.execute();

        return mRootView;
    }

    private String getNameAdjusted(String string) {
        if (string.endsWith("/>")) {
            return string.replace("/>", "");
        }
        return string;
    }

}