package com.apk.editor.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.editor.BuildConfig;
import com.apk.editor.R;
import com.apk.editor.utils.APKEditorUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 10, 2021
 */
public class CreditsActivity extends AppCompatActivity {

    private final ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        AppCompatImageButton mBack = findViewById(R.id.back);
        MaterialTextView mVersion = findViewById(R.id.version);
        MaterialTextView mCopyright = findViewById(R.id.copyright);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mData.add(new RecycleViewItem("Willi Ye", "Kernel Adiutor", "https://github.com/Grarak/KernelAdiutor"));
        mData.add(new RecycleViewItem("Hsiafan", "APK parser", "https://github.com/hsiafan/apk-parser"));
        mData.add(new RecycleViewItem("Srikanth Reddy Lingala", "Zip4j", "https://github.com/srikanth-lingala/zip4j"));
        if (APKEditorUtils.isFullVersion(this)) {
            mData.add(new RecycleViewItem("Aefyr", "PseudoApkSigner", "https://github.com/Aefyr/PseudoApkSigner"));
        }
        mData.add(new RecycleViewItem("Connor Tumbleson", "Apktool", "https://github.com/iBotPeaches/Apktool/"));
        mData.add(new RecycleViewItem("Ben Gruver", "smali/baksmali", "https://github.com/JesusFreke/smali/"));

        mData.add(new RecycleViewItem("sunilpaulmathew", "Package Manager", "https://github.com/SmartPack/PackageManager"));
        mData.add(new RecycleViewItem("Gospel Gilbert", "App Icon", "https://t.me/gilgreat0295"));
        mData.add(new RecycleViewItem("Mohammed Qubati", "Arabic Translation", "https://t.me/Alqubati_MrK"));
        mData.add(new RecycleViewItem("wushidi", "Chinese (Simplified) Translation", "https://t.me/wushidi"));
        mData.add(new RecycleViewItem("fossdd", "German Translation", "https://chaos.social/@fossdd"));
        mData.add(new RecycleViewItem("bruh", "Vietnamese Translation", null));
        mData.add(new RecycleViewItem("Bruno", "French Translation", null));
        mData.add(new RecycleViewItem("Miloš Koliáš", "Czech Translation", null));
        mData.add(new RecycleViewItem("Mehmet Un", "Turkish Translation", null));
        mData.add(new RecycleViewItem("Jander Mander", "Arabic Translation", null));
        mData.add(new RecycleViewItem("Diego", "Spanish Translation", "https://github.com/sguinetti"));
        mData.add(new RecycleViewItem("tommynok", "Russian Translation", null));
        mData.add(new RecycleViewItem("Alexander Steiner", "Russian Translation", null));
        mData.add(new RecycleViewItem("Hoa Gia Đại Thiếu", "Vietnamese Translation", null));
        mData.add(new RecycleViewItem("mezysinc", "Portuguese (Brazilian) Translation", "https://github.com/mezysinc"));
        mData.add(new RecycleViewItem("Andreaugustoqueiroz999", "Portuguese (Portugal) Translation", null));
        mData.add(new RecycleViewItem("Dodi Studio", "Indonesian Translation", "null"));
        mData.add(new RecycleViewItem("Cooky", "Polish Translation", null));

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
        mCopyright.setText(getString(R.string.copyright, "2021-2022, APK Explorer & Editor"));

        mBack.setOnClickListener(v -> finish());
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private static ArrayList<RecycleViewItem> data;

        public RecycleViewAdapter(ArrayList<RecycleViewItem> data) {
            RecycleViewAdapter.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_credits, parent, false);
            return new RecycleViewAdapter.ViewHolder(rowItem);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            holder.Title.setText(data.get(position).getTitle());
            holder.Description.setText(data.get(position).getDescription());
            holder.Description.setPaintFlags(holder.Description.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.Description.setTextColor(APKEditorUtils.getThemeAccentColor(holder.Title.getContext()));
            holder.Description.setOnClickListener(v -> {
                if (data.get(position).getURL() != null) {
                    APKEditorUtils.launchUrl(data.get(position).getURL(), (Activity) v.getContext());
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final MaterialTextView Title, Description;

            public ViewHolder(View view) {
                super(view);
                this.Title = view.findViewById(R.id.title);
                this.Description = view.findViewById(R.id.description);
            }
        }
    }

    private static class RecycleViewItem implements Serializable {

        private final String mDescription, mTitle, mURL;

        public RecycleViewItem(String title, String description, String url) {
            this.mTitle = title;
            this.mDescription = description;
            this.mURL = url;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public String getURL() {
            return mURL;
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.credits_message), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.translate, v -> {
            APKEditorUtils.launchUrl("https://poeditor.com/join/project?hash=QztabxONOp", this);
            snackbar.dismiss();
        });
        snackbar.show();
    }


}