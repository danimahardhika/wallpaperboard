package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.LatestAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.applications.WallpaperBoardConfiguration;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.tasks.WallpapersLoaderTask;
import com.dm.wallpaper.board.utils.LogUtil;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

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

public class LatestFragment extends Fragment {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.swipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R2.id.progress)
    MaterialProgressBar mProgress;

    private List<Wallpaper> mWallpapers;
    private LatestAdapter mAdapter;
    private AsyncTask mAsyncTask;
    private StaggeredGridLayoutManager mManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latest, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mManager = new StaggeredGridLayoutManager(
                getActivity().getResources().getInteger(R.integer.latest_wallpapers_column_count),
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mManager);

        mWallpapers = new ArrayList<>();
        mAdapter = new LatestAdapter(getActivity(), mWallpapers);
        mRecyclerView.setAdapter(mAdapter);

        mSwipe.setColorSchemeColors(ColorHelper.getAttributeColor(
                getActivity(), R.attr.colorAccent));
        mSwipe.setOnRefreshListener(() -> {
            if (mAsyncTask != null) {
                mSwipe.setRefreshing(false);
                return;
            }

            WallpapersLoaderTask.start(getActivity());
            mAsyncTask = new WallpapersLoader().execute();
        });

        resetRecyclerViewPadding();
        resetViewBottomPadding(mRecyclerView, true);

        mAsyncTask = new WallpapersLoader().execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mAsyncTask != null) return;
        if (mAdapter == null) return;

        int[] positions = mManager.findFirstVisibleItemPositions(null);

        int spanCount = getActivity().getResources().getInteger(
                R.integer.latest_wallpapers_column_count);
        ViewHelper.resetSpanCount(mRecyclerView, spanCount);
        resetRecyclerViewPadding();
        resetViewBottomPadding(mRecyclerView, true);

        mAdapter = new LatestAdapter(getActivity(), mWallpapers);
        mRecyclerView.setAdapter(mAdapter);

        if (positions.length > 0)
            mRecyclerView.scrollToPosition(positions[0]);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    private void resetRecyclerViewPadding() {
        int spanCount = getActivity().getResources().getInteger(
                R.integer.latest_wallpapers_column_count);
        if (spanCount == 1) {
            mRecyclerView.setPadding(0, 0, 0, 0);
            return;
        }

        if (WallpaperBoardApplication.getConfig().getWallpapersGrid() ==
                WallpaperBoardConfiguration.GridStyle.FLAT) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
            return;
        }

        int paddingTop = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin_top);
        int paddingLeft = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin_right);
        mRecyclerView.setPadding(paddingLeft, paddingTop, 0, 0);
    }

    private class WallpapersLoader extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mSwipe.isRefreshing()) {
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    mWallpapers = Database.get(getActivity()).getLatestWallpapers();

                    for (int i = 0; i < mWallpapers.size(); i++) {
                        publishProgress(i);
                    }
                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            new WallpaperDimensionLoader(values[0]).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
        }
    }

    private class WallpaperDimensionLoader extends AsyncTask<Void, Void, Boolean> {

        private int mPosition;

        private WallpaperDimensionLoader(int position) {
            mPosition = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (mWallpapers.get(mPosition).getDimensions() != null)
                        return true;

                    URL url = new URL(mWallpapers.get(mPosition).getUrl());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000);

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream stream = connection.getInputStream();

                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(stream, null, options);

                        ImageSize imageSize = new ImageSize(options.outWidth, options.outHeight);
                        mWallpapers.get(mPosition).setDimensions(imageSize);

                        Database.get(getActivity()).updateWallpaper(
                                mWallpapers.get(mPosition));
                        stream.close();
                        return true;
                    }
                    return false;
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

            mSwipe.setRefreshing(false);
            mProgress.setVisibility(View.GONE);
            if (aBoolean) {
                if (mPosition == (mWallpapers.size() - 1)) {
                    if (mAdapter != null) {
                        mAdapter.setWallpapers(mWallpapers);
                        return;
                    }

                    mAdapter = new LatestAdapter(getActivity(), mWallpapers);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        }
    }
}
