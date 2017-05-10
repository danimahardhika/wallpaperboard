package com.dm.wallpaper.board.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.fragments.dialogs.FilterFragment;
import com.dm.wallpaper.board.fragments.dialogs.RefreshDurationFragment;
import com.dm.wallpaper.board.helpers.ColorHelper;
import com.dm.wallpaper.board.helpers.DrawableHelper;
import com.dm.wallpaper.board.helpers.TimeHelper;
import com.dm.wallpaper.board.helpers.ViewHelper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.listeners.RefreshDurationListener;

import butterknife.BindView;
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

public class WallpaperBoardMuzeiActivity extends AppCompatActivity implements View.OnClickListener,
        RefreshDurationListener {

    @BindView(R2.id.scrollview)
    NestedScrollView mScrollView;
    @BindView(R2.id.muzei_refresh_duration)
    LinearLayout mRefreshDuration;
    @BindView(R2.id.muzei_rotate_time)
    TextView mRotateTimeText;
    @BindView(R2.id.muzei_wifi_only)
    LinearLayout mWifiOnly;
    @BindView(R2.id.muzei_wifi_only_check)
    AppCompatCheckBox mWifiOnlyCheck;
    @BindView(R2.id.muzei_select_categories)
    LinearLayout mSelectCategories;
    @BindView(R2.id.muzei_save)
    LinearLayout mSave;

    private Class<?> mMuzeiService;
    private int mRotateTime;
    private boolean mIsMinute;

    public void initMuzeiActivity(@Nullable Bundle savedInstanceState, @NonNull Class<?> muzeiService) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.MuzeiThemeDark : R.style.MuzeiTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muzei);
        ButterKnife.bind(this);
        ViewHelper.setApplicationWindowColor(this);
        ViewCompat.setNestedScrollingEnabled(mScrollView, false);
        ViewHelper.disableTranslucentNavigationBar(this);
        ColorHelper.setStatusBarColor(this,
                ColorHelper.getAttributeColor(this, R.attr.colorPrimary));
        ColorHelper.setStatusBarIconColor(this);
        ColorHelper.setNavigationBarColor(this, ColorHelper.getDarkerColor(
                ColorHelper.getAttributeColor(this, R.attr.colorAccent), 0.8f));

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_muzei);
        setSupportActionBar(toolbar);

        mMuzeiService = muzeiService;
        mIsMinute = Preferences.get(this).isRotateMinute();
        mRotateTime = TimeHelper.convertMilliToMinute(
                Preferences.get(this).getRotateTime());
        if (!mIsMinute) mRotateTime = mRotateTime / 60;

        initRefreshDuration();
        initSettings();

        mWifiOnly.setOnClickListener(this);
        mSelectCategories.setOnClickListener(this);
        mRefreshDuration.setOnClickListener(this);
        mSave.setOnClickListener(this);

        mWifiOnlyCheck.setChecked(Preferences.get(this).isWifiOnly());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetViewBottomPadding(mScrollView, false);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.muzei_refresh_duration) {
            RefreshDurationFragment.showRefreshDurationDialog(getSupportFragmentManager(), mRotateTime, mIsMinute);
        } else if (id == R.id.muzei_wifi_only) {
            mWifiOnlyCheck.setChecked(!mWifiOnlyCheck.isChecked());
        } else if (id == R.id.muzei_select_categories) {
            FilterFragment.showFilterDialog(getSupportFragmentManager(), true);
        } else if (id == R.id.muzei_save) {
            int rotateTime = TimeHelper.convertMinuteToMilli(mRotateTime);
            if (!mIsMinute) rotateTime = rotateTime * 60;

            Preferences.get(this).setRotateMinute(mIsMinute);
            Preferences.get(this).setRotateTime(rotateTime);
            Preferences.get(this).setWifiOnly(mWifiOnlyCheck.isChecked());

            Intent intent = new Intent(this, mMuzeiService);
            intent.putExtra("restart", true);
            startService(intent);

            Toast.makeText(this, R.string.muzei_settings_saved,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRefreshDurationSet(int rotateTime, boolean isMinute) {
        mRotateTime = rotateTime;
        mIsMinute = isMinute;
        initRefreshDuration();
    }

    private void initRefreshDuration() {
        String refreshDuration = getResources().getString(R.string.muzei_refresh_duration_desc) +" "+
                mRotateTime +" "+ getResources().getString(mIsMinute ? R.string.minute : R.string.hour);
        mRotateTimeText.setText(refreshDuration);
    }

    private void initSettings() {
        int color = ColorHelper.getAttributeColor(this, R.attr.colorAccent);
        int titleColor = ColorHelper.getTitleTextColor(color);

        ImageView icon = ButterKnife.findById(this, R.id.muzei_save_icon);
        icon.setImageDrawable(DrawableHelper.getTintedDrawable(this, R.drawable.ic_toolbar_save, titleColor));
        TextView text = ButterKnife.findById(this, R.id.muzei_save_text);
        text.setTextColor(titleColor);
    }
}
