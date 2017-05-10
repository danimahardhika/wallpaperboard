package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.dm.wallpaper.board.R;

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

public class PreferencesHelper {

    private final Context mContext;

    private static final String PREFERENCES_NAME = "wallpaper_board_preferences";

    private static final String KEY_LICENSED = "licensed";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_ROTATE_TIME = "rotate_time";
    private static final String KEY_ROTATE_MINUTE = "rotate_minute";
    private static final String KEY_WIFI_ONLY = "wifi_only";
    private static final String KEY_WALLS_DIRECTORY = "wallpaper_directory";
    private static final String KEY_AVAILABLE_WALLPAPERS_COUNT = "available_wallpapers_count";
    private static final String KEY_WALLPAPER_CROP = "wallpaper_crop";
    private static final String KEY_APPLY_LOCKSCREEN = "apply_lockscreen";
    private static final String KEY_WALLPAPERS_INTRO = "wallpapers_intro";
    private static final String KEY_WALLPAPER_PREVIEW_INTRO = "wallpaper_preview_intro";
    private static final String KEY_COLORED_WALLPAPERS_CARD = "colored_wallpapers_card";

    public PreferencesHelper(@NonNull Context context) {
        mContext = context;
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLicensed() {
        return getSharedPreferences().getBoolean(KEY_LICENSED, false);
    }

    public void setLicensed(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_LICENSED, bool).apply();
    }

    public boolean isFirstRun() {
        return getSharedPreferences().getBoolean(KEY_FIRST_RUN, true);
    }

    void setFirstRun(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_FIRST_RUN, bool).apply();
    }

    public boolean isDarkTheme() {
        return getSharedPreferences().getBoolean(KEY_DARK_THEME,
                mContext.getResources().getBoolean(R.bool.use_dark_theme));
    }

    public void setDarkTheme(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_DARK_THEME, bool).apply();
    }

    public boolean isColoredWallpapersCard() {
        return getSharedPreferences().getBoolean(KEY_COLORED_WALLPAPERS_CARD, false);
    }

    public void setColoredWallpapersCard(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_COLORED_WALLPAPERS_CARD, bool).apply();
    }

    public boolean isShadowEnabled() {
        return mContext.getResources().getBoolean(R.bool.enable_shadow);
    }

    public boolean isTimeToShowWallpapersIntro() {
        return getSharedPreferences().getBoolean(KEY_WALLPAPERS_INTRO, true);
    }

    public void setTimeToShowWallpapersIntro(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WALLPAPERS_INTRO, bool).apply();
    }

    public boolean isTimeToShowWallpaperPreviewIntro() {
        return getSharedPreferences().getBoolean(KEY_WALLPAPER_PREVIEW_INTRO, true);
    }

    public void setTimeToShowWallpaperPreviewIntro(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WALLPAPER_PREVIEW_INTRO, bool).apply();
    }

    public void setRotateTime (int time) {
        getSharedPreferences().edit().putInt(KEY_ROTATE_TIME, time).apply();
    }

    public int getRotateTime() {
        return getSharedPreferences().getInt(KEY_ROTATE_TIME, 3600000);
    }

    public void setRotateMinute (boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_ROTATE_MINUTE, bool).apply();
    }

    public boolean isRotateMinute() {
        return getSharedPreferences().getBoolean(KEY_ROTATE_MINUTE, false);
    }

    public boolean isWifiOnly() {
        return getSharedPreferences().getBoolean(KEY_WIFI_ONLY, false);
    }

    public void setWifiOnly (boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WIFI_ONLY, bool).apply();
    }

    void setWallsDirectory(String directory) {
        getSharedPreferences().edit().putString(KEY_WALLS_DIRECTORY, directory).apply();
    }

    public String getWallsDirectory() {
        return getSharedPreferences().getString(KEY_WALLS_DIRECTORY, "");
    }

    public boolean isWallpaperCrop() {
        return getSharedPreferences().getBoolean(KEY_WALLPAPER_CROP, false);
    }

    public void setWallpaperCrop(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WALLPAPER_CROP, bool).apply();
    }

    public boolean isApplyLockscreen() {
        return getSharedPreferences().getBoolean(KEY_APPLY_LOCKSCREEN, false);
    }

    public void setApplyLockscreen(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_APPLY_LOCKSCREEN, bool).apply();
    }

    public int getAvailableWallpapersCount() {
        return getSharedPreferences().getInt(KEY_AVAILABLE_WALLPAPERS_COUNT, 0);
    }

    public void setAvailableWallpapersCount(int count) {
        getSharedPreferences().edit().putInt(KEY_AVAILABLE_WALLPAPERS_COUNT, count).apply();
    }

    public boolean isConnectedToNetwork() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnectedAsPreferred() {
        try {
            if (isWifiOnly()) {
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        activeNetworkInfo.isConnected();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
