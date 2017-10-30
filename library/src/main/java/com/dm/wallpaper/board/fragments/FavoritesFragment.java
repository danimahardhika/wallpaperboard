package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.WallpapersAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.applications.WallpaperBoardConfiguration;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.ConfigurationHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.NavigationListener;

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

public class FavoritesFragment extends Fragment {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.favorite_empty)
    ImageView mFavoriteEmpty;
    @BindView(R2.id.toolbar)
    Toolbar mToolbar;

    private AsyncTask mAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
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
        ViewHelper.setupToolbar(mToolbar);

        WindowHelper.setTranslucentStatusBar(getActivity(), false);
        ColorHelper.setStatusBarColor(getActivity(), Color.TRANSPARENT, true);

        TextView textView = getActivity().findViewById(R.id.title);
        textView.setText(getActivity().getResources().getString(
                R.string.navigation_view_favorites));
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
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count)));
        mRecyclerView.setHasFixedSize(false);

        if (WallpaperBoardApplication.getConfig().getWallpapersGrid() ==
                WallpaperBoardConfiguration.GridStyle.FLAT) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
        }
        resetViewBottomPadding(mRecyclerView, true);

        mAsyncTask = new FavoritesLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView, getActivity().getResources().getInteger(
                R.integer.wallpapers_column_count));
        resetViewBottomPadding(mRecyclerView, true);
    }

    @Override
    public void onDestroy() {
        WindowHelper.setTranslucentStatusBar(getActivity(), true);
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    private class FavoritesLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Wallpaper> wallpapers;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wallpapers = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    wallpapers = Database.get(getActivity()).getFavoriteWallpapers();
                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            if (aBoolean) {
                mRecyclerView.setAdapter(new WallpapersAdapter(getActivity(),
                        wallpapers, true, false));

                if (mRecyclerView.getAdapter().getItemCount() == 0) {
                    int color = ColorHelper.getAttributeColor(getActivity(),
                            android.R.attr.textColorSecondary);

                    mFavoriteEmpty.setImageDrawable(
                            DrawableHelper.getTintedDrawable(getActivity(),
                                    R.drawable.ic_wallpaper_favorite_empty,
                                    ColorHelper.setColorAlpha(color, 0.7f)));
                    mFavoriteEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
