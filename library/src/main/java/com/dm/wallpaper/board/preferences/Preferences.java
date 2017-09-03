package com.dm.wallpaper.board.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.items.Language;
import com.dm.wallpaper.board.items.PopupItem;

import java.util.List;
import java.util.Locale;

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

public class Preferences {

    private final Context mContext;

    private static final String PREFERENCES_NAME = "wallpaper_board_preferences";

    private static final String KEY_LICENSED = "licensed";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_ROTATE_TIME = "rotate_time";
    private static final String KEY_ROTATE_MINUTE = "rotate_minute";
    private static final String KEY_WIFI_ONLY = "wifi_only";
    private static final String KEY_WALLS_DIRECTORY = "wallpaper_download_directory";
    private static final String KEY_CROP_WALLPAPER = "crop_wallpaper";
    private static final String KEY_WALLPAPER_PREVIEW_INTRO = "wallpaper_preview_intro";
    private static final String KEY_LANGUAGE_PREFERENCE = "language_preference";
    private static final String KEY_CURRENT_LOCALE = "current_locale";
    private static final String KEY_AUTO_INCREMENT = "auto_increment";
    private static final String KEY_WALLPAPER_TOOLTIP = "wallpaper_tooltip";
    private static final String KEY_SORT_BY = "sort_by";
    private static final String KEY_HIGH_QUALITY_PREVIEW = "high_quality_preview";

    private Preferences(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    public static Preferences get(@NonNull Context context) {
        return new Preferences(context);
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void clearPreferences() {
        boolean isLicensed = isLicensed();
        getSharedPreferences().edit().clear().apply();

        if (isLicensed) {
            setFirstRun(false);
            setLicensed(true);
        }
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

    public void setFirstRun(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_FIRST_RUN, bool).apply();
    }

    public boolean isDarkTheme() {
        boolean useDarkTheme = mContext.getResources().getBoolean(R.bool.use_dark_theme);
        boolean isThemingEnabled = WallpaperBoardApplication.getConfiguration().isDashboardThemingEnabled();
        if (!isThemingEnabled) return useDarkTheme;
        return getSharedPreferences().getBoolean(KEY_DARK_THEME, useDarkTheme);
    }

    public void setDarkTheme(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_DARK_THEME, bool).apply();
    }

    public boolean isShadowEnabled() {
        return WallpaperBoardApplication.getConfiguration().isShadowEnabled();
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

    public boolean isCropWallpaper() {
        return getSharedPreferences().getBoolean(KEY_CROP_WALLPAPER, false);
    }

    public void setCropWallpaper(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_CROP_WALLPAPER, bool).apply();
    }

    public boolean isShowWallpaperTooltip() {
        return getSharedPreferences().getBoolean(KEY_WALLPAPER_TOOLTIP, true);
    }

    public void setShowWallpaperTooltip(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WALLPAPER_TOOLTIP, bool).apply();
    }

    public boolean isHighQualityPreviewEnabled() {
        return getSharedPreferences().getBoolean(KEY_HIGH_QUALITY_PREVIEW,
                WallpaperBoardApplication.getConfiguration().isHighQualityPreviewEnabled());
    }

    public void setHighQualityPreviewEnabled(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_HIGH_QUALITY_PREVIEW, bool).apply();
    }

    public Locale getCurrentLocale() {
        String code = getSharedPreferences().getString(KEY_CURRENT_LOCALE, "en_US");
        return LocaleHelper.getLocale(code);
    }

    public void setCurrentLocale(String code) {
        getSharedPreferences().edit().putString(KEY_CURRENT_LOCALE, code).apply();
    }

    public boolean isTimeToSetLanguagePreference() {
        return getSharedPreferences().getBoolean(KEY_LANGUAGE_PREFERENCE, true);
    }

    private void setTimeToSetLanguagePreference(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_LANGUAGE_PREFERENCE, bool).apply();
    }

    public void setLanguagePreference() {
        Locale locale = Locale.getDefault();
        List<Language> languages = LocaleHelper.getAvailableLanguages(mContext);

        Locale currentLocale = null;
        for (Language language : languages) {
            Locale l = language.getLocale();
            if (locale.toString().equals(l.toString())) {
                currentLocale = l;
                break;
            }
        }

        if (currentLocale == null) {
            for (Language language : languages) {
                Locale l = language.getLocale();
                if (locale.getLanguage().equals(l.getLanguage())) {
                    currentLocale = l;
                    break;
                }
            }
        }

        if (currentLocale != null) {
            setCurrentLocale(currentLocale.toString());
            LocaleHelper.setLocale(mContext);
            setTimeToSetLanguagePreference(false);
        }
    }

    public void setAutoIncrement(int value) {
        getSharedPreferences().edit().putInt(KEY_AUTO_INCREMENT, value).apply();
    }

    public int getAutoIncrement() {
        return getSharedPreferences().getInt(KEY_AUTO_INCREMENT, 0);
    }

    public void setSortBy(PopupItem.Type type) {
        getSharedPreferences().edit().putInt(KEY_SORT_BY, getSortByOrder(type)).apply();
    }

    public int getSortByOrder(PopupItem.Type type) {
        switch (type) {
            case SORT_LATEST:
                return 0;
            case SORT_OLDEST:
                return 1;
            case SORT_NAME:
                return 2;
            case SORT_RANDOM:
                return 3;
            default:
                return 2;
        }
    }

    public PopupItem.Type getSortBy(){
        int sort = getSharedPreferences().getInt(KEY_SORT_BY, 2);
        if (sort == 0) {
            return PopupItem.Type.SORT_LATEST;
        } else if (sort == 1) {
            return PopupItem.Type.SORT_OLDEST;
        } else if (sort == 2) {
            return PopupItem.Type.SORT_NAME;
        } else if (sort == 3) {
            return PopupItem.Type.SORT_RANDOM;
        }
        return PopupItem.Type.SORT_NAME;
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
