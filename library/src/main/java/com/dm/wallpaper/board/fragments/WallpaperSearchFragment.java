package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.WallpapersAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.applications.WallpaperBoardConfiguration;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Filter;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

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

public class WallpaperSearchFragment extends Fragment {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.search_result)
    TextView mSearchResult;
    @BindView(R2.id.toolbar)
    Toolbar mToolbar;

    private SearchView mSearchView;
    private WallpapersAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpaper_search, container, false);
        ButterKnife.bind(this, view);

        if (!Preferences.get(getActivity()).isShadowEnabled()) {
            View shadow = view.findViewById( R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }

        ViewHelper.setupToolbar(mToolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        resetViewBottomPadding(mRecyclerView, true);
        initSearchResult();

        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_back, color));
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count)));
        mRecyclerView.setHasFixedSize(false);

        if (WallpaperBoardApplication.getConfig().getWallpapersGrid() ==
                WallpaperBoardConfiguration.GridStyle.FLAT) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_wallpaper_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);
        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        search.setIcon(DrawableHelper.getTintedDrawable(getActivity(),
                R.drawable.ic_toolbar_search, color));

        mSearchView = (SearchView) search.getActionView();
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setQueryHint(getActivity().getResources().getString(R.string.menu_search));
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        search.expandActionView();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.requestFocus();

        ViewHelper.setSearchViewTextColor(mSearchView, color);
        ViewHelper.setSearchViewBackgroundColor(mSearchView, Color.TRANSPARENT);
        ViewHelper.setSearchViewCloseIcon(mSearchView,
                DrawableHelper.getTintedDrawable(getActivity(), R.drawable.ic_toolbar_close, color));
        ViewHelper.setSearchViewSearchIcon(mSearchView, null);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String string) {
                if (string.length() == 0) {
                    clearAdapter();
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String string) {
                mSearchView.clearFocus();
                mAsyncTask = new WallpapersLoader(string.trim()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().finish();
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        if (mAsyncTask != null) mAsyncTask.cancel(true);
        super.onDestroy();
    }

    private void initSearchResult() {
        Drawable drawable = DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_search_large, Color.WHITE);
        mSearchResult.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
    }

    public boolean isSearchQueryEmpty() {
        return mSearchView.getQuery() == null || mSearchView.getQuery().length() == 0;
    }

    public void filterSearch(String query) {
        mAsyncTask = new WallpapersLoader(query).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void clearAdapter() {
        if (mAdapter == null) return;

        mAdapter.clearItems();
        if (mSearchResult.getVisibility() == View.VISIBLE) {
            AnimationHelper.fade(mSearchResult).start();
        }

        AnimationHelper.setBackgroundColor(mRecyclerView,
                ((ColorDrawable) mRecyclerView.getBackground()).getColor(),
                Color.TRANSPARENT)
                .interpolator(new LinearOutSlowInInterpolator())
                .start();
    }

    private class WallpapersLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Wallpaper> wallpapers;
        private String query;

        private WallpapersLoader(String query) {
            this.query = query;
            wallpapers = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (query == null || query.length() == 0) {
                        return false;
                    }

                    Filter filter = new Filter();
                    filter.add(Filter.Create(Filter.Column.NAME).setQuery(query));
                    filter.add(Filter.Create(Filter.Column.AUTHOR).setQuery(query));
                    filter.add(Filter.Create(Filter.Column.CATEGORY).setQuery(query));
                    filter.add(Filter.Create(Filter.Column.ID).setQuery(query));

                    wallpapers = Database.get(getActivity()).getFilteredWallpapers(filter);
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
                mAdapter = new WallpapersAdapter(getActivity(), wallpapers, false, false);
                mRecyclerView.setAdapter(mAdapter);

                if (mAdapter.getItemCount() == 0) {
                    String text = String.format(getActivity().getResources().getString(
                            R.string.search_result_empty), query);

                    mSearchResult.setText(text);
                    if (mSearchResult.getVisibility() == View.GONE) {
                        AnimationHelper.fade(mSearchResult).start();
                    }

                    AnimationHelper.setBackgroundColor(mRecyclerView,
                            ((ColorDrawable) mRecyclerView.getBackground()).getColor(),
                            Color.TRANSPARENT)
                            .interpolator(new LinearOutSlowInInterpolator())
                            .start();
                } else {
                    if (mSearchResult.getVisibility() == View.VISIBLE) {
                        AnimationHelper.fade(mSearchResult).start();
                    }

                    AnimationHelper.setBackgroundColor(mRecyclerView,
                            ((ColorDrawable) mRecyclerView.getBackground()).getColor(),
                            ColorHelper.getAttributeColor(getActivity(), R.attr.main_background))
                            .interpolator(new LinearOutSlowInInterpolator())
                            .start();
                }
            } else {
                if (query == null || query.length() == 0) {
                    mSearchView.setQuery("", false);
                    clearAdapter();

                    mSearchView.requestFocus();
                    SoftKeyboardHelper.openKeyboard(getActivity());
                }
            }
        }
    }
}
