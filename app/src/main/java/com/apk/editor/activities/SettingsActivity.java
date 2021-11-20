package com.apk.editor.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.R;
import com.apk.editor.adapters.SettingsAdapter;
import com.apk.editor.utils.AppSettings;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 25, 2021
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AppCompatImageButton mBack = findViewById(R.id.back_button);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        SettingsAdapter mRecycleViewAdapter = new SettingsAdapter(AppSettings.getData(this));
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mRecycleViewAdapter.setOnItemClickListener((position, v) ->
                AppSettings.handleSettingsActions(mRecycleViewAdapter, position, this));

        mBack.setOnClickListener(v -> onBackPressed());
    }

}