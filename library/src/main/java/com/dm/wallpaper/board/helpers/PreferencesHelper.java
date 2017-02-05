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
    private static final String KEY_SCROLL_WALLPAPER = "scroll_wallpaper";
    private static final String KEY_AVAILABLE_WALLPAPERS_COUNT = "available_wallpapers_count";

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

    public boolean isScrollWallpaper() {
        return getSharedPreferences().getBoolean(KEY_SCROLL_WALLPAPER, true);
    }

    public void setScrollWallpaper(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_SCROLL_WALLPAPER, bool).apply();
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
