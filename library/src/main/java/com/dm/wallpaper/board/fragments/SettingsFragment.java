package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.SettingsAdapter;
import com.dm.wallpaper.board.helpers.FileHelper;
import com.dm.wallpaper.board.helpers.ViewHelper;
import com.dm.wallpaper.board.items.Setting;
import com.dm.wallpaper.board.preferences.Preferences;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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

public class SettingsFragment extends Fragment {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        if (!Preferences.get(getActivity()).isShadowEnabled()) {
            View shadow = ButterKnife.findById(view, R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);
        ViewHelper.resetViewBottomPadding(mRecyclerView, false);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        initSettings();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetViewBottomPadding(mRecyclerView, false);
    }

    private void initSettings() {
        List<Setting> settings = new ArrayList<>();

        double cache = (double) FileHelper.getCacheSize(getActivity().getCacheDir()) / FileHelper.MB;
        NumberFormat formatter = new DecimalFormat("#0.00");

        settings.add(new Setting(R.drawable.ic_toolbar_storage,
                getActivity().getResources().getString(R.string.pref_data_header),
                "", "", "", Setting.Type.HEADER, -1));

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_data_cache),
                getActivity().getResources().getString(R.string.pref_data_cache_desc),
                String.format(getActivity().getResources().getString(R.string.pref_data_cache_size),
                        formatter.format(cache) + " MB"),
                Setting.Type.CACHE, -1));

        settings.add(new Setting(R.drawable.ic_toolbar_theme,
                getActivity().getResources().getString(R.string.pref_theme_header),
                "", "", "", Setting.Type.HEADER, -1));

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_theme_dark),
                getActivity().getResources().getString(R.string.pref_theme_dark_desc),
                "", Setting.Type.THEME, Preferences.get(getActivity()).isDarkTheme() ? 1 : 0));

        settings.add(new Setting(R.drawable.ic_toolbar_wallpapers,
                getActivity().getResources().getString(R.string.pref_wallpaper_header),
                "", "", "", Setting.Type.HEADER, -1));

        String directory = getActivity().getResources().getString(R.string.pref_wallpaper_location_desc);
        if (Preferences.get(getActivity()).getWallsDirectory().length() > 0) {
            directory = Preferences.get(getActivity()).getWallsDirectory() + File.separator;
        }

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_wallpaper_location),
                directory, "", Setting.Type.WALLPAPER, -1));

        settings.add(new Setting(R.drawable.ic_toolbar_others,
                getActivity().getResources().getString(R.string.pref_others_header),
                "", "", "", Setting.Type.HEADER, -1));

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_others_colored_wallpaper_card),
                "", "", Setting.Type.COLORED_CARD, Preferences.get(getActivity()).isColoredWallpapersCard() ? 1 : 0));

        settings.add(new Setting(-1, "",
                getActivity().getResources().getString(R.string.pref_others_reset_tutorial),
                "", "", Setting.Type.RESET_TUTORIAL, -1));

        mRecyclerView.setAdapter(new SettingsAdapter(getActivity(), settings));
    }
}
