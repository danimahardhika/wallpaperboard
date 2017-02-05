package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.helpers.ViewHelper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Wallpaper Board
 *
 * Copyright (c) 2017 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class SettingsFragment extends Fragment implements View.OnClickListener {

    @BindView(R2.id.pref_cache_clear)
    LinearLayout mCacheClear;
    @BindView(R2.id.pref_cache_size)
    TextView mCacheSize;
    @BindView(R2.id.pref_dark_theme)
    LinearLayout mDarkTheme;
    @BindView(R2.id.pref_dark_theme_check)
    AppCompatCheckBox mDarkThemeCheck;
    @BindView(R2.id.pref_walls_directory)
    TextView mWallsDirectory;
    @BindView(R2.id.scrollview)
    NestedScrollView mScrollView;

    private File mCache;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mScrollView, false);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);
        mCacheClear.setOnClickListener(this);
        mDarkTheme.setOnClickListener(this);

        if (Preferences.getPreferences(getActivity()).getWallsDirectory().length() > 0) {
            String directory = Preferences.getPreferences(
                    getActivity()).getWallsDirectory() + File.separator;
            mWallsDirectory.setText(directory);
        }

        initSettings();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.pref_cache_clear) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.pref_data_cache)
                    .content(R.string.pref_data_cache_clear_dialog)
                    .positiveText(R.string.clear)
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog, which) -> {
                        try {
                            clearCache(mCache);
                            initSettings();

                            Toast.makeText(getActivity(), getActivity()
                                            .getResources().getString(
                                            R.string.pref_data_cache_cleared),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.d(Extras.LOG_TAG, Log.getStackTraceString(e));
                        }
                    })
                    .show();
        } else if (id == R.id.pref_dark_theme) {
            Preferences.getPreferences(getActivity()).setDarkTheme(!mDarkThemeCheck.isChecked());
            mDarkThemeCheck.setChecked(!mDarkThemeCheck.isChecked());
            getActivity().recreate();
        }
    }

    private void initSettings() {
        mCache = new File(getActivity().getCacheDir().toString());

        double cache = (double) cacheSize(mCache)/1024/1024;
        NumberFormat formatter = new DecimalFormat("#0.00");
        String cacheSize = getActivity().getResources().getString(
                R.string.pref_data_cache_size)
                +" "+ (formatter.format(cache)) + " MB";

        mCacheSize.setText(cacheSize);
        mDarkThemeCheck.setChecked(Preferences.getPreferences(getActivity()).isDarkTheme());
    }

    private void clearCache(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                clearCache(child);
        fileOrDirectory.delete();
    }

    private long cacheSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    result += cacheSize(aFileList);
                } else {
                    result += aFileList.length();
                }
            }
            return result;
        }
        return 0;
    }
}
