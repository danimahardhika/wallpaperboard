package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.AboutAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.helpers.ConfigurationHelper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.NavigationListener;

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

public class AboutFragment extends Fragment {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);

        if (!Preferences.get(getActivity()).isShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        resetRecyclerViewPadding(getResources().getConfiguration().orientation);
        ViewHelper.setupToolbar(mToolbar);

        WindowHelper.setTranslucentStatusBar(getActivity(), false);
        ColorHelper.setStatusBarColor(getActivity(), Color.TRANSPARENT, true);

        TextView textView = getActivity().findViewById(R.id.title);
        textView.setText(getActivity().getResources().getString(
                R.string.navigation_view_about));
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

        int spanCount = getActivity().getResources().getInteger(R.integer.about_column_count);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                spanCount, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new AboutAdapter(getActivity(), spanCount));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetRecyclerViewPadding(newConfig.orientation);

        ViewHelper.resetSpanCount(mRecyclerView, getActivity().getResources().getInteger(
                R.integer.about_column_count));

        StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.setAdapter(new AboutAdapter(getActivity(), manager.getSpanCount()));
    }

    @Override
    public void onDestroy() {
        WindowHelper.setTranslucentStatusBar(getActivity(), true);
        super.onDestroy();
    }

    private void resetRecyclerViewPadding(int orientation) {
        if (mRecyclerView == null) return;

        int padding = 0;
        int navBar = 0;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            padding = getActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);
            navBar = padding;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = getResources().getBoolean(R.bool.android_helpers_tablet_mode);
            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                navBar = WindowHelper.getNavigationBarHeight(getActivity());
            }
        }
        mRecyclerView.setPadding(padding, padding, 0, navBar);
    }
}
