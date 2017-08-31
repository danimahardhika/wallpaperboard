package com.dm.wallpaper.board.applications;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.activities.WallpaperBoardCrashReport;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.helpers.UrlHelper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.JsonStructure;
import com.dm.wallpaper.board.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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

public class WallpaperBoardApplication extends Application {

    private static Configuration mConfiguration;
    private Thread.UncaughtExceptionHandler mHandler;

    public static Configuration getConfiguration() {
        if (mConfiguration == null) {
            mConfiguration = new Configuration();
        }
        return mConfiguration;
    }

    public void initApplication() {
        initApplication(new Configuration());
    }

    public void initApplication(@NonNull Configuration configuration) {
        super.onCreate();
        mConfiguration = configuration;

        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageConfig.getImageLoaderConfiguration(this));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Font-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        //Enable logging
        LogUtil.setLoggingEnabled(true);

        if (mConfiguration.mIsCrashReportEnabled) {
            String[] urls = getResources().getStringArray(R.array.about_social_links);
            boolean isContainsValidEmail = false;
            for (String url : urls) {
                if (UrlHelper.getType(url) == UrlHelper.Type.EMAIL) {
                    isContainsValidEmail = true;
                    mConfiguration.setCrashReportEmail(url);
                    break;
                }
            }

            if (isContainsValidEmail) {
                mHandler = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
            } else {
                mConfiguration.setCrashReportEnabled(false);
                mConfiguration.setCrashReportEmail(null);
            }
        }

        if (Preferences.get(this).isTimeToSetLanguagePreference()) {
            Preferences.get(this).setLanguagePreference();
            return;
        }

        LocaleHelper.setLocale(this);
    }

    private void handleUncaughtException(Thread thread, Throwable throwable) {
        try {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateTime = dateFormat.format(new Date());
            sb.append("Crash Time : ").append(dateTime).append("\n");
            sb.append("Class Name : ").append(throwable.getClass().getName()).append("\n");
            sb.append("Caused By : ").append(throwable.toString()).append("\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\n");
                sb.append(element.toString());
            }

            Intent intent = new Intent(this, WallpaperBoardCrashReport.class);
            intent.putExtra(WallpaperBoardCrashReport.EXTRA_STACKTRACE, sb.toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } catch (Exception e) {
            if (mHandler != null) {
                mHandler.uncaughtException(thread, throwable);
                return;
            }
        }
        System.exit(1);
    }

    public static class Configuration {

        private NavigationIcon mNavigationIcon = NavigationIcon.DEFAULT;
        private NavigationViewHeader mNavigationViewHeader = NavigationViewHeader.NORMAL;
        private GridStyle mWallpapersGrid = GridStyle.CARD;

        private boolean mIsDashboardThemingEnabled = true;
        private boolean mIsShadowEnabled = true;
        private int mLatestWallpapersDisplayMax = 20;

        private boolean mIsCrashReportEnabled = true;
        private String mCrashReportEmail = null;

        private JsonStructure mJsonStructure = new JsonStructure.Builder().build();

        public Configuration setNavigationIcon(@NonNull NavigationIcon navigationIcon) {
            mNavigationIcon = navigationIcon;
            return this;
        }

        public Configuration setNavigationViewHeaderStyle(@NonNull NavigationViewHeader navigationViewHeader) {
            mNavigationViewHeader = navigationViewHeader;
            return this;
        }

        public Configuration setWallpapersGridStyle(@NonNull GridStyle gridStyle) {
            mWallpapersGrid = gridStyle;
            return this;
        }

        public Configuration setDashboardThemingEnabled(boolean dashboardThemingEnabled) {
            mIsDashboardThemingEnabled = dashboardThemingEnabled;
            return this;
        }

        public Configuration setShadowEnabled(boolean shadowEnabled) {
            mIsShadowEnabled = shadowEnabled;
            return this;
        }

        public Configuration setLatestWallpapersDisplayMax(int count) {
            mLatestWallpapersDisplayMax = count;
            return this;
        }

        public Configuration setCrashReportEnabled(boolean crashReportEnabled) {
            mIsCrashReportEnabled = crashReportEnabled;
            return this;
        }

        private Configuration setCrashReportEmail(String email) {
            mCrashReportEmail = email;
            return this;
        }

        public Configuration setJsonStructure(@NonNull JsonStructure jsonStructure) {
            mJsonStructure = jsonStructure;
            return this;
        }

        public NavigationIcon getNavigationIcon() {
            return mNavigationIcon;
        }

        public NavigationViewHeader getNavigationViewHeader() {
            return mNavigationViewHeader;
        }

        public GridStyle getWallpapersGrid() {
            return mWallpapersGrid;
        }

        public boolean isDashboardThemingEnabled() {
            return mIsDashboardThemingEnabled;
        }

        public boolean isShadowEnabled() {
            return mIsShadowEnabled;
        }

        public int getLatestWallpapersDisplayMax() {
            return mLatestWallpapersDisplayMax;
        }

        public  String getCrashReportEmail() {
            return mCrashReportEmail;
        }

        public JsonStructure getJsonStructure() {
            return mJsonStructure;
        }
    }

    public enum NavigationIcon {
        DEFAULT,
        STYLE_1,
        STYLE_2,
        STYLE_3,
        STYLE_4
    }

    public enum NavigationViewHeader {
        NORMAL,
        MINI,
        NONE
    }

    public enum GridStyle {
        CARD,
        FLAT
    }
}
