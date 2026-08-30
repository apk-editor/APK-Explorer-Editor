package com.apk.editor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ExploredInfoAdapter;
import com.apk.editor.utils.APKExplorer;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.dialogs.ProjectExitDialog;
import com.apk.editor.utils.tasks.DeleteFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on Sept. 25, 2025
 */
public class ExploredInfoFragment extends BaseFragment {

    private String mBackupPath;

    public static ExploredInfoFragment newInstance(String backupFilePath) {
        ExploredInfoFragment fragment = new ExploredInfoFragment();

        Bundle args = new Bundle();
        args.putString("backupFilePath", backupFilePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mBackupPath = arguments.getString("backupFilePath");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.layout_recyclerview, container, false);

        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        File rootFile = new File(mBackupPath.replace("/.aeeBackup/appData", ""));
        
        new sExecutor() {
            private List<HashMap<String, String>> data = new ArrayList<>();
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                data = new ArrayList<>();
                try {
                    JSONObject jsonObject = APKExplorer.getAppData(mBackupPath);
                    data.add(new HashMap<>() {{
                                 put("title", Objects.requireNonNull(jsonObject).getString("version_info"));
                                 put("description", null);
                             }}
                    );
                    data.add(new HashMap<>() {{
                                 put("title", Objects.requireNonNull(jsonObject).getString("sdk_minimum"));
                                 put("description", null);
                             }}
                    );
                    data.add(new HashMap<>() {{
                                 put("title", Objects.requireNonNull(jsonObject).getString("sdk_compiled"));
                                 put("description", null);
                             }}
                    );
                    data.add(new HashMap<>() {{
                                 put("title", getString(R.string.certificate));
                                 put("description", Objects.requireNonNull(jsonObject).getString("certificate_info"));
                             }}
                    );
                } catch (JSONException | NullPointerException ignored) {}
            }

            @Override
            public void onPostExecute() {
                mRecyclerView.setAdapter(new ExploredInfoAdapter(data));
            }
        }.execute();

        AppSettings.applyMargin(mRecyclerView, requireActivity());

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                retainDialog(rootFile);
            }
        };

        return mRootView;
    }

    private void retainDialog(File rootFile) {
        if (sCommonUtils.getString("projectAction", null, requireActivity()) == null) {
            new ProjectExitDialog(APKExplorer.getAppIcon(mBackupPath), requireActivity()) {
                @Override
                public void onDiscard() {
                    new DeleteFile(rootFile, requireActivity(), true).execute();
                }

                @Override
                public void onSave() {
                    requireActivity().finish();
                }
            };
        } else if (sCommonUtils.getString("projectAction", null, requireActivity()).equals(getString(R.string.delete))) {
            new DeleteFile(rootFile, requireActivity(), true).execute();
        } else {
            requireActivity().finish();
        }
    }
    
}