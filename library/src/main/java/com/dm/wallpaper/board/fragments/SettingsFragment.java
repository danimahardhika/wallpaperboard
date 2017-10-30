package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.SettingsAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.helpers.ConfigurationHelper;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.helpers.WallpaperHelper;
import com.dm.wallpaper.board.items.Language;
import com.dm.wallpaper.board.items.Setting;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.NavigationListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.dm.wallpaper.board.helpers.ViewHelper.resetViewBottomPadding;

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
    @BindView(R2.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        if (!Preferences.get(getActivity()).isShadowEnabled()) {
            View shadow = view.findViewById( R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resetViewBottomPadding(mRecyclerView, true);
        ViewHelper.setupToolbar(mToolbar);

        WindowHelper.setTranslucentStatusBar(getActivity(), false);
        ColorHelper.setStatusBarColor(getActivity(), Color.TRANSPARENT, true);

        TextView textView = getActivity().findViewById(R.id.title);
        textView.setText(getActivity().getResources().getString(
                R.string.navigation_view_settings));

        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(getActivity(),
                WallpaperBoardApplication.getConfig().getNavigationIcon()));
        mToolbar.setNavigationOnClickListener(view -> {
            try {
                NavigationListener listener = (NavigationListener) getActivity();
                listener.onNavigationIconClick();
            } catch (IllegalStateException e) {
                LogUtil.e("Parent activity must implements NavigationListener");
            }
        });

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        initSettings();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetViewBottomPadding(mRecyclerView, true);
    }

    @Override
    public void onDestroy() {
        WindowHelper.setTranslucentStatusBar(getActivity(), true);
        super.onDestroy();
    }

    private void initSettings() {
        List<Setting> settings = new ArrayList<>();

        double cache = (double) FileHelper.getDirectorySize(getActivity().getCacheDir()) / FileHelper.MB;
        NumberFormat formatter = new DecimalFormat("#0.00");

        settings.add(Setting.Builder(Setting.Type.HEADER)
                .icon(R.drawable.ic_toolbar_storage)
                .title(getActivity().getResources().getString(R.string.pref_data_header))
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.CACHE)
                .subtitle(getActivity().getResources().getString(R.string.pref_data_cache))
                .content(getActivity().getResources().getString(R.string.pref_data_cache_desc))
                .footer(String.format(getActivity().getResources().getString(R.string.pref_data_cache_size),
                        formatter.format(cache) + " MB"))
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.HEADER)
                .icon(R.drawable.ic_toolbar_theme)
                .title(getActivity().getResources().getString(R.string.pref_theme_header))
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.THEME)
                .subtitle(getActivity().getResources().getString(R.string.pref_theme_dark))
                .content(getActivity().getResources().getString(R.string.pref_theme_dark_desc))
                .checkboxState(Preferences.get(getActivity()).isDarkTheme() ? 1 : 0)
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.HEADER)
                .icon(R.drawable.ic_toolbar_wallpapers)
                .title(getActivity().getResources().getString(R.string.pref_wallpaper_header))
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.PREVIEW_QUALITY)
                .subtitle(getActivity().getResources().getString(R.string.pref_wallpaper_high_quality_preview))
                .content(Preferences.get(getActivity()).isHighQualityPreviewEnabled() ?
                        getActivity().getResources().getString(R.string.pref_wallpaper_high_quality_preview_high) :
                        getActivity().getResources().getString(R.string.pref_wallpaper_high_quality_preview_low))
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.WALLPAPER)
                .subtitle(getActivity().getResources().getString(R.string.pref_wallpaper_location))
                .content(WallpaperHelper.getDefaultWallpapersDirectory(getActivity()).toString())
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.HEADER)
                .icon(R.drawable.ic_toolbar_language)
                .title(getActivity().getResources().getString(R.string.pref_language_header))
                .build()
        );

        Language language = LocaleHelper.getCurrentLanguage(getActivity());
        settings.add(Setting.Builder(Setting.Type.LANGUAGE)
                .subtitle(language.getName())
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.HEADER)
                .icon(R.drawable.ic_toolbar_others)
                .title(getActivity().getResources().getString(R.string.pref_others_header))
                .build()
        );

        settings.add(Setting.Builder(Setting.Type.RESET_TUTORIAL)
                .subtitle(getActivity().getResources().getString(R.string.pref_others_reset_tutorial))
                .build()
        );

        mRecyclerView.setAdapter(new SettingsAdapter(getActivity(), settings));
    }
}
