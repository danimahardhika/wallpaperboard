package com.dm.wallpaper.board.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.items.Collection;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.AppBarListener;
import com.dm.wallpaper.board.utils.listeners.TabListener;

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

public class CollectionFragment extends Fragment {

    @BindView(R2.id.appbar)
    AppBarLayout mAppBar;
    @BindView(R2.id.toolbar)
    Toolbar mToolbar;
    @BindView(R2.id.tab)
    TabLayout mTab;
    @BindView(R2.id.pager)
    ViewPager mPager;

    private CollectionPagerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);
        ButterKnife.bind(this, view);
        initViewPager();
        mTab.setupWithViewPager(mPager);
        mTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
                tab.setIcon(mAdapter.getIcon(tab.getPosition(), true));

                try {
                    TabListener listener = (TabListener) getActivity();
                    listener.onTabScroll(mAdapter.get(tab.getPosition()).getTag());
                } catch (IllegalStateException e) {
                    LogUtil.e("Parent activity must implements TabListener");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setIcon(mAdapter.getIcon(tab.getPosition(), false));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewHelper.setupToolbar(mToolbar);

        mToolbar.setTitle("");
        initAppBar();

        for (int i = 0; i < mTab.getTabCount(); i++) {
            TabLayout.Tab tab = mTab.getTabAt(i);
            if (tab != null) {
                tab.setIcon(mAdapter.getIcon(i, i == 0));
            }
        }

        try {
            TabListener listener = (TabListener) getActivity();
            listener.onTabScroll(mAdapter.get(0).getTag());
        } catch (IllegalStateException e) {
            LogUtil.e("Parent activity must implements TabListener");
        }
    }

    private void initViewPager() {
        List<Collection> collection = new ArrayList<>();
        collection.add(new Collection(R.drawable.ic_collection_latest,
                new LatestFragment(), Extras.TAG_LATEST));
        collection.add(new Collection(R.drawable.ic_collection_wallpapers,
                new WallpapersFragment(), Extras.TAG_WALLPAPERS));
        collection.add(new Collection(R.drawable.ic_collection_categories,
                new CategoriesFragment(), Extras.TAG_CATEGORIES));

        mPager.setOffscreenPageLimit(2);
        mAdapter = new CollectionPagerAdapter(getChildFragmentManager(), collection);
        mPager.setAdapter(mAdapter);
    }

    private void initAppBar() {
        mAppBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int maxScroll = mAppBar.getTotalScrollRange();
            float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

            try {
                AppBarListener listener = (AppBarListener) getActivity();
                listener.onAppBarScroll(percentage);
            } catch (IllegalStateException e) {
                LogUtil.e("Parent activity must implements AppBarListener");
            }

            if (percentage == 0f) {
                ColorHelper.setupStatusBarIconColor(getActivity());
            } else if (percentage == 1f) {
                ColorHelper.setupStatusBarIconColor(getActivity(), false);
            }
        });
    }

    public void refreshWallpapers() {
        if (mAdapter == null) return;

        int index = 1;
        if (index > mAdapter.getCount()) return;
        Fragment fragment = mAdapter.getItem(index);
        if (fragment != null && fragment instanceof WallpapersFragment) {
            WallpapersFragment f = (WallpapersFragment) fragment;
            f.getWallpapers();
        }
    }

    private class CollectionPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Collection> mCollection;

        CollectionPagerAdapter(FragmentManager fm, @NonNull List<Collection> collection) {
            super(fm);
            mCollection = collection;
        }

        @Override
        public Fragment getItem(int position) {
            return mCollection.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return mCollection.size();
        }

        Collection get(int position) {
            return mCollection.get(position);
        }

        Drawable getIcon(int position, boolean selected) {
            int color = ColorHelper.getAttributeColor(getActivity(), R.attr.tab_icon);
            if (selected) {
                color = ColorHelper.getAttributeColor(getActivity(), R.attr.tab_icon_selected);
            }

            Drawable drawable = DrawableHelper.get(getActivity(), mCollection.get(position).getIcon());
            return DrawableHelper.getTintedDrawable(drawable, color);
        }
    }
}
