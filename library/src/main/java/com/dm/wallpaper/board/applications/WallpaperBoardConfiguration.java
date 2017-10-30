package com.dm.wallpaper.board.applications;

import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.dm.wallpaper.board.utils.JsonStructure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: Dani Mahardhika
 * Created on: 10/27/2017
 * https://github.com/danimahardhika
 */

public class WallpaperBoardConfiguration {

    private @NavigationIcon int mNavigationIcon = NavigationIcon.DEFAULT;
    private @NavigationViewHeader int mNavigationViewHeader = NavigationViewHeader.NORMAL;
    private @GridStyle int mWallpapersGrid = GridStyle.CARD;

    private boolean mIsHighQualityPreviewEnabled = false;
    private boolean mIsDashboardThemingEnabled = true;
    private boolean mIsShadowEnabled = true;
    private int mLatestWallpapersDisplayMax = 15;

    @ColorInt private int mAppLogoColor = -1;

    private boolean mIsCrashReportEnabled = true;
    private String mCrashReportEmail = null;

    private JsonStructure mJsonStructure = new JsonStructure.Builder().build();

    public WallpaperBoardConfiguration setNavigationIcon(@NavigationIcon int navigationIcon) {
        mNavigationIcon = navigationIcon;
        return this;
    }

    public WallpaperBoardConfiguration setAppLogoColor(@ColorInt int color) {
        mAppLogoColor = color;
        return this;
    }

    public WallpaperBoardConfiguration setNavigationViewHeaderStyle(@NavigationViewHeader int navigationViewHeader) {
        mNavigationViewHeader = navigationViewHeader;
        return this;
    }

    public WallpaperBoardConfiguration setWallpapersGridStyle(@GridStyle int gridStyle) {
        mWallpapersGrid = gridStyle;
        return this;
    }

    public WallpaperBoardConfiguration setDashboardThemingEnabled(boolean dashboardThemingEnabled) {
        mIsDashboardThemingEnabled = dashboardThemingEnabled;
        return this;
    }

    public WallpaperBoardConfiguration setShadowEnabled(boolean shadowEnabled) {
        mIsShadowEnabled = shadowEnabled;
        return this;
    }

    public WallpaperBoardConfiguration setLatestWallpapersDisplayMax(@IntRange(from = 5, to = 15) int count) {
        int finalCount = count;
        if (finalCount < 5) {
            finalCount = 5;
        } else if (finalCount > 15) {
            finalCount = 15;
        }
        mLatestWallpapersDisplayMax = finalCount;
        return this;
    }

    public WallpaperBoardConfiguration setHighQualityPreviewEnabled(boolean highQualityPreviewEnabled) {
        mIsHighQualityPreviewEnabled = highQualityPreviewEnabled;
        return this;
    }

    public WallpaperBoardConfiguration setCrashReportEnabled(boolean crashReportEnabled) {
        mIsCrashReportEnabled = crashReportEnabled;
        return this;
    }

    public WallpaperBoardConfiguration setCrashReportEmail(String email) {
        mCrashReportEmail = email;
        return this;
    }

    public WallpaperBoardConfiguration setJsonStructure(@NonNull JsonStructure jsonStructure) {
        mJsonStructure = jsonStructure;
        return this;
    }

    public @NavigationIcon int getNavigationIcon() {
        return mNavigationIcon;
    }

    public int getAppLogoColor() {
        return mAppLogoColor;
    }

    public @NavigationViewHeader int getNavigationViewHeader() {
        return mNavigationViewHeader;
    }

    public @GridStyle int getWallpapersGrid() {
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

    public boolean isHighQualityPreviewEnabled() {
        return mIsHighQualityPreviewEnabled;
    }

    public boolean isCrashReportEnabled() {
        return mIsCrashReportEnabled;
    }

    public  String getCrashReportEmail() {
        return mCrashReportEmail;
    }

    public JsonStructure getJsonStructure() {
        return mJsonStructure;
    }

    @IntDef({NavigationIcon.DEFAULT,
            NavigationIcon.STYLE_1,
            NavigationIcon.STYLE_2,
            NavigationIcon.STYLE_3,
            NavigationIcon.STYLE_4})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NavigationIcon {
        int DEFAULT = 0;
        int STYLE_1 = 1;
        int STYLE_2 = 2;
        int STYLE_3 = 3;
        int STYLE_4 = 4;
    }

    @IntDef({NavigationViewHeader.NORMAL, NavigationViewHeader.MINI, NavigationViewHeader.NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NavigationViewHeader {
        int NORMAL = 0;
        int MINI = 1;
        int NONE = 2;
    }

    @IntDef({GridStyle.CARD, GridStyle.FLAT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GridStyle {
        int CARD = 0;
        int FLAT = 1;
    }
}
