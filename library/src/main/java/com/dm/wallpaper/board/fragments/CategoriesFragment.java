package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.CategoriesAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.utils.LogUtil;

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

public class CategoriesFragment extends Fragment {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.progress)
    MaterialProgressBar mProgress;

    private List<Category> mCategories;
    private GridLayoutManager mManager;
    private CategoriesAdapter mAdapter;
    private AsyncTask mAsyncTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mManager = new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.categories_column_count));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.setHasFixedSize(false);

        resetRecyclerViewPadding();
        resetViewBottomPadding(mRecyclerView, true);

        if (Database.get(getActivity()).getWallpapersCount() > 0) {
            mAsyncTask = new CategoriesLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }

        mAsyncTask = new CategoriesLoaderTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mAsyncTask != null) return;
        if (mAdapter == null) return;

        int position = mManager.findFirstVisibleItemPosition();

        int spanCount = getActivity().getResources().getInteger(
                R.integer.categories_column_count);
        ViewHelper.resetSpanCount(mRecyclerView, spanCount);
        resetRecyclerViewPadding();
        resetViewBottomPadding(mRecyclerView, true);

        mAdapter = new CategoriesAdapter(getActivity(), mCategories);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.scrollToPosition(position);
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
                R.integer.categories_column_count);
        if (spanCount == 1) {
            mRecyclerView.setPadding(0, 0, 0, 0);
            return;
        }

        if (WallpaperBoardApplication.getConfiguration().getWallpapersGrid() ==
                WallpaperBoardApplication.GridStyle.FLAT) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
            return;
        }

        int paddingTop = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin_top);
        int paddingLeft = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin_right);
        mRecyclerView.setPadding(paddingLeft, paddingTop, 0, 0);
    }

    private class CategoriesLoaderTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    mCategories = Database.get(getActivity()).getCategories();
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
            mAsyncTask = null;

            mProgress.setVisibility(View.GONE);
            if (aBoolean) {
                mAdapter = new CategoriesAdapter(getActivity(), mCategories);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }
}
