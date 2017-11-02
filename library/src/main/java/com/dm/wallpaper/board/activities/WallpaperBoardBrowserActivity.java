package com.dm.wallpaper.board.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.fragments.CategoryWallpapersFragment;
import com.dm.wallpaper.board.fragments.WallpaperSearchFragment;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

public class WallpaperBoardBrowserActivity extends AppCompatActivity {

    private FragmentManager mFragManager;

    private int mFragmentId;
    private int mCategoryCount;
    private String mCategoryName;
    private String mFragmentTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.BrowserThemeDark : R.style.BrowserTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_browser);
        ButterKnife.bind(this);

        int color = ColorHelper.getAttributeColor(this, R.attr.colorPrimary);
        ColorHelper.setupStatusBarIconColor(this, ColorHelper.isLightColor(color));

        WindowHelper.resetNavigationBarTranslucent(this,
                WindowHelper.NavigationBarTranslucent.PORTRAIT_ONLY);

        SoftKeyboardHelper softKeyboardHelper = new SoftKeyboardHelper(this,
                findViewById(R.id.container));
        softKeyboardHelper.enable();

        mFragManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mFragmentId = savedInstanceState.getInt(Extras.EXTRA_FRAGMENT_ID);
            mCategoryName = savedInstanceState.getString(Extras.EXTRA_CATEGORY);
            mCategoryCount = savedInstanceState.getInt(Extras.EXTRA_COUNT);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mFragmentId = bundle.getInt(Extras.EXTRA_FRAGMENT_ID);
            mCategoryName = bundle.getString(Extras.EXTRA_CATEGORY);
            mCategoryCount = bundle.getInt(Extras.EXTRA_COUNT);
        }

        setFragment();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mCategoryName != null) {
            outState.putString(Extras.EXTRA_CATEGORY, mCategoryName);
        }
        outState.putInt(Extras.EXTRA_COUNT, mCategoryCount);
        outState.putInt(Extras.EXTRA_FRAGMENT_ID, mFragmentId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getExtras() != null) {
            this.setIntent(intent);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mFragmentId = bundle.getInt(Extras.EXTRA_FRAGMENT_ID);
                mCategoryName = bundle.getString(Extras.EXTRA_CATEGORY);
                mCategoryCount = bundle.getInt(Extras.EXTRA_COUNT);
            }

            setFragment();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowHelper.resetNavigationBarTranslucent(this, WindowHelper.NavigationBarTranslucent.PORTRAIT_ONLY);
        LocaleHelper.setLocale(this);
    }

    @Override
    protected void onDestroy() {
        WindowHelper.setTranslucentStatusBar(this, true);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mFragmentId == Extras.ID_WALLPAPER_SEARCH) {
            Fragment fragment = mFragManager.findFragmentByTag(Extras.TAG_WALLPAPER_SEARCH);
            if (fragment != null) {
                WallpaperSearchFragment wallpaperSearchFragment = (WallpaperSearchFragment) fragment;
                if (!wallpaperSearchFragment.isSearchQueryEmpty()) {
                    wallpaperSearchFragment.filterSearch("");
                    return;
                }
            }

            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

        super.onBackPressed();
    }

    private void setFragment() {
        Fragment fragment = null;
        if (mFragmentId == Extras.ID_CATEGORY_WALLPAPERS) {
            fragment = CategoryWallpapersFragment.newInstance(mCategoryName, mCategoryCount);
        } else if (mFragmentId == Extras.ID_WALLPAPER_SEARCH) {
            fragment = new WallpaperSearchFragment();
            mFragmentTag = Extras.TAG_WALLPAPER_SEARCH;
        }

        if (fragment == null) {
            finish();
            return;
        }

        FragmentTransaction ft = mFragManager.beginTransaction()
                .replace(R.id.container, fragment, mFragmentTag);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }
    }
}
