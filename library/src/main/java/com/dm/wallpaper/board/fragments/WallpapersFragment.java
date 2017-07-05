package com.dm.wallpaper.board.fragments;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ListHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.WallpapersAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.fragments.dialogs.FilterFragment;
import com.dm.wallpaper.board.helpers.JsonHelper;
import com.dm.wallpaper.board.helpers.TapIntroHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.JsonStructure;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.SearchListener;
import com.dm.wallpaper.board.utils.listeners.WallpaperBoardListener;
import com.dm.wallpaper.board.utils.listeners.WallpaperListener;
import com.rafakob.drawme.DrawMeButton;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.NameValuePair;

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

public class WallpapersFragment extends Fragment implements WallpaperListener {

    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.swipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R2.id.popup_bubble)
    DrawMeButton mPopupBubble;
    @BindView(R2.id.progress)
    ProgressBar mProgress;

    private WallpapersAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mGetWallpapers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);
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
        initPopupBubble();

        mProgress.getIndeterminateDrawable().setColorFilter(ColorHelper.getAttributeColor(
                getActivity(), R.attr.colorAccent), PorterDuff.Mode.SRC_IN);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count)));
        mRecyclerView.setHasFixedSize(false);

        if (WallpaperBoardApplication.getConfiguration().getWallpapersGrid() ==
                WallpaperBoardApplication.GridStyle.FLAT) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
        }
        resetViewBottomPadding(mRecyclerView, true);

        mSwipe.setColorSchemeColors(ColorHelper.getAttributeColor(
                getActivity(), R.attr.colorAccent));
        mSwipe.setOnRefreshListener(() -> {
            if (mProgress.getVisibility() == View.GONE) {
                getWallpapers(true);
                return;
            }
            mSwipe.setRefreshing(false);
        });

        getWallpapers(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView, getActivity().getResources().getInteger(
                R.integer.wallpapers_column_count));
        resetViewBottomPadding(mRecyclerView, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_wallpapers, menu);
        MenuItem search = menu.findItem(R.id.menu_search);

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm == null) return false;

                setHasOptionsMenu(false);
                SearchListener listener = (SearchListener) getActivity();
                listener.onSearchExpanded(true);

                FragmentTransaction ft = fm.beginTransaction()
                        .replace(R.id.container, new WallpaperSearchFragment(),
                                Extras.TAG_WALLPAPER_SEARCH)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack(null);
                try {
                    ft.commit();
                } catch (Exception e) {
                    ft.commitAllowingStateLoss();
                }
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_filter) {
            FilterFragment.showFilterDialog(getActivity().getSupportFragmentManager(), false);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if (mGetWallpapers != null) mGetWallpapers.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onWallpaperSelected(int position) {
        if (mAdapter == null) return;
        if (position < 0 || position > mAdapter.getItemCount()) return;

        mRecyclerView.scrollToPosition(position);
    }

    public boolean scrollWallpapersToTop() {
        if (mRecyclerView != null) {
            if (!(mRecyclerView.getLayoutManager() instanceof GridLayoutManager)) return false;

            GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            int position = manager.findFirstVisibleItemPosition();
            if (position > 0) {
                mRecyclerView.smoothScrollToPosition(0);
                return true;
            }
        }
        return false;
    }

    public void initPopupBubble() {
        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent);
        mPopupBubble.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_arrow_up, ColorHelper.getTitleTextColor(color)), null, null, null);
        mPopupBubble.setOnClickListener(view -> {
            WallpaperBoardListener listener = (WallpaperBoardListener) getActivity();
            listener.onWallpapersChecked(null);

            AnimationHelper.hide(getActivity().findViewById(R.id.popup_bubble)).start();

            getWallpapers(true);
        });
    }

    public void showPopupBubble() {
        int wallpapersCount = Database.get(getActivity()).getWallpapersCount();
        if (wallpapersCount == 0) return;
        if (mPopupBubble.getVisibility() == View.VISIBLE) return;

        if (Preferences.get(getActivity()).getAvailableWallpapersCount() > wallpapersCount) {
            AnimationHelper.show(mPopupBubble)
                    .interpolator(new LinearOutSlowInInterpolator())
                    .start();
        }
    }

    public void filterWallpapers() {
        if (mAdapter == null) return;
        mAdapter.filter();
    }

    public void downloadWallpaper() {
        if (mAdapter == null) return;
        mAdapter.downloadLastSelectedWallpaper();
    }

    private void getWallpapers(boolean refreshing) {
        mGetWallpapers = new AsyncTask<Void, Void, Boolean>() {

            List<Wallpaper> wallpapers;
            Database database;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (refreshing) {
                    mSwipe.setRefreshing(true);
                    WallpaperBoardListener listener = (WallpaperBoardListener) getActivity();
                    listener.onWallpapersChecked(null);
                } else {
                    mProgress.setVisibility(View.VISIBLE);
                }

                wallpapers = new ArrayList<>();
                database = Database.get(getActivity());

                if (mPopupBubble.getVisibility() == View.VISIBLE) {
                    AnimationHelper.hide(mPopupBubble).start();
                }
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (!refreshing && database.getWallpapersCount() > 0) {
                            wallpapers = database.getFilteredWallpapers();
                            return true;
                        }

                        String wallpaperUrl = WallpaperBoardApplication.getConfiguration().getJsonStructure().jsonOutputUrl();
                        if (wallpaperUrl == null) {
                            wallpaperUrl = getActivity().getResources().getString(R.string.wallpaper_json);
                        }

                        URL url = new URL(wallpaperUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(15000);

                        if (WallpaperBoardApplication.getConfiguration().getJsonStructure().jsonOutputUrl() != null) {
                            connection.setRequestMethod("POST");
                            connection.setUseCaches(false);
                            connection.setDoOutput(true);

                            List<NameValuePair> values = WallpaperBoardApplication.getConfiguration()
                                    .getJsonStructure().jsonOutputPost();
                            if (values.size() > 0) {
                                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                                dStream.writeBytes(JsonHelper.getQuery(values));
                                dStream.flush();
                                dStream.close();
                            }
                        }

                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream stream = connection.getInputStream();
                            JsonStructure.CategoryStructure categoryStructure = WallpaperBoardApplication
                                    .getConfiguration().getJsonStructure().categoryStructure();
                            JsonStructure.WallpaperStructure wallpaperStructure = WallpaperBoardApplication
                                    .getConfiguration().getJsonStructure().wallpaperStructure();

                            Map<String, List> map = LoganSquare.parseMap(stream, List.class);
                            if (map == null) return false;

                            List categoryList = map.get(categoryStructure.getArrayName());
                            if (categoryList == null) {
                                LogUtil.e("Json error: category array with name "
                                        +categoryStructure.getArrayName() +" not found");
                                return false;
                            }

                            if (categoryList.size() == 0) {
                                LogUtil.e("Json error: make sure to add category correctly");
                                return false;
                            }

                            List wallpaperList = map.get(wallpaperStructure.getArrayName());
                            if (wallpaperList == null) {
                                LogUtil.e("Json error: wallpaper array with name "
                                        +wallpaperStructure.getArrayName() +" not found");
                                return false;
                            }

                            if (refreshing) {
                                if (database.getWallpapersCount() > 0) {
                                    wallpapers = database.getWallpapers();
                                    List<Wallpaper> newWallpapers = new ArrayList<>();
                                    for (int i = 0; i < wallpaperList.size(); i++) {
                                        Wallpaper wallpaper = JsonHelper.getWallpaper(wallpaperList.get(i));
                                        if (wallpaper != null) {
                                            newWallpapers.add(wallpaper);
                                        }
                                    }

                                    List<Wallpaper> intersection = (List<Wallpaper>)
                                            ListHelper.intersect(newWallpapers, wallpapers);
                                    List<Wallpaper> deleted = (List<Wallpaper>)
                                            ListHelper.difference(intersection, wallpapers);
                                    List<Wallpaper> newlyAdded = (List<Wallpaper>)
                                            ListHelper.difference(intersection, newWallpapers);

                                    database.deleteCategories();
                                    database.addCategories(categoryList);

                                    database.deleteWallpapers(deleted);
                                    database.addWallpapers(newlyAdded);

                                    Preferences.get(getActivity()).setAvailableWallpapersCount(
                                            database.getWallpapersCount());
                                    wallpapers = database.getFilteredWallpapers();
                                    return true;
                                }
                            }

                            if (database.getWallpapersCount() > 0)
                                database.deleteWallpapers();

                            database.addCategories(categoryList);
                            database.addWallpapers(wallpaperList);
                            wallpapers = database.getFilteredWallpapers();
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        database.close();
                        LogUtil.e(Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (refreshing) mSwipe.setRefreshing(false);
                mProgress.setVisibility(View.GONE);
                if (aBoolean) {
                    setHasOptionsMenu(true);
                    mAdapter = new WallpapersAdapter(getActivity(), wallpapers, false, false);
                    mRecyclerView.setAdapter(mAdapter);

                    try {
                        TapIntroHelper.showWallpapersIntro(getActivity(), mRecyclerView);
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.connection_failed, Toast.LENGTH_LONG).show();
                }
                mGetWallpapers = null;
                showPopupBubble();
            }
        }.execute();
    }
}
