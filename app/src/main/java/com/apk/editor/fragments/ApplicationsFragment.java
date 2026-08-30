package com.apk.editor.fragments;

import static android.view.View.GONE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.ApplicationsAdapter;
import com.apk.editor.utils.AppData;
import com.apk.editor.utils.AppSettings;
import com.apk.editor.utils.Serializables.PackageItems;
import com.apk.editor.utils.dialogs.BatchOptionsDialog;
import com.apk.editor.utils.menu.ExploreOptionsMenu;
import com.apk.editor.viewModels.FragmentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;
import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 */
public class ApplicationsFragment extends BaseFragment {

    private ApplicationsAdapter mRecycleViewAdapter;
    private boolean mExit = false, mSelectAll = false;
    private int mTabPosition = 2;
    private final List<String> mPackageNames = new CopyOnWriteArrayList<>();
    private ContentLoadingProgressBar mProgress;
    private MaterialAutoCompleteTextView mSearchWord;
    private MaterialButton mBatchButton;
    private String mSearchText = null;

    public ApplicationsFragment() {
    }

    public static ApplicationsFragment newInstance(int position) {
        ApplicationsFragment fragment = new ApplicationsFragment();

        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTabPosition = getArguments().getInt("tabPosition");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_applications, container, false);

        mSearchWord = mRootView.findViewById(R.id.search_word);
        mBatchButton = mRootView.findViewById(R.id.batch_options);
        mProgress = mRootView.findViewById(R.id.progress);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        FragmentViewModel viewModel = new ViewModelProvider(requireActivity()).get(FragmentViewModel.class);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecycleViewAdapter = new ApplicationsAdapter(new CopyOnWriteArrayList<>(), mPackageNames, mBatchButton, mSearchText, packageName -> ExploreOptionsMenu.getMenu(packageName, null, null, false, requireActivity()));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mBatchButton.setOnClickListener(v -> new BatchOptionsDialog(mPackageNames, mSelectAll, requireActivity()) {
            @Override
            public void selectAllLister(boolean checked) {
                if (checked) {
                    mSelectAll = false;
                    mPackageNames.clear();
                } else {
                    mSelectAll = true;
                }
                loadApps(mSearchText);
                mRecycleViewAdapter.refreshData();
            }
        });

        mSearchWord.setOnEditorActionListener((v, actionId, event) -> {
            AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            mSearchWord.clearFocus();
            return true;
        });

        mSearchWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadApps(s.toString().trim().toLowerCase());
            }
        });

        viewModel.getSearchTrigger().observe(getViewLifecycleOwner(), isVisible -> {
            if (isVisible) {
                mSearchWord.setVisibility(View.VISIBLE);
                if (!mSearchWord.hasFocus()) {
                    mSearchWord.requestFocus();
                    AppData.toggleKeyboard(1, mSearchWord, requireActivity());
                }
            } else {
                mSearchWord.setVisibility(GONE);
                AppData.toggleKeyboard(0, mSearchWord, requireActivity());
            }
        });

        viewModel.getSortTrigger().observe(getViewLifecycleOwner(), timestamp -> loadApps(mSearchText));

        loadApps(mSearchText);

        AppSettings.applyMargin(mRecyclerView, requireActivity());

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mProgress.getVisibility() == View.VISIBLE) {
                    return;
                }
                if (mSearchWord.getVisibility() == View.VISIBLE) {
                    if (mSearchText != null) {
                        mSearchText = null;
                        mSearchWord.setText(null);
                    }
                    mSearchWord.setVisibility(GONE);
                    return;
                }
                if (mBatchButton.getVisibility() == View.VISIBLE) {
                    mSelectAll = false;
                    mPackageNames.clear();
                    loadApps(mSearchText);
                    return;
                }
                if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    sCommonUtils.toast(getString(R.string.press_back), requireActivity()).show();
                    mExit = true;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> mExit = false, 2000);
                }
            }
        };

        return mRootView;
    }

    private void loadApps(String searchWord) {
        new sExecutor() {
            private List<PackageItems> data;

            @Override
            public void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void doInBackground() {
                data = AppData.getData(searchWord, mTabPosition, requireActivity());
                if (mSelectAll) {
                    mPackageNames.clear();
                    for (PackageItems items : data) {
                        mPackageNames.add(items.getPackageName());
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) {
                    return;
                }

                mProgress.setVisibility(View.GONE);
                mSearchText = searchWord;
                mRecycleViewAdapter.updateData(data);
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSearchText != null) {
            mSearchWord.setText(null);
            mSearchWord.setVisibility(GONE);
        }

        mPackageNames.clear();
    }
    
}