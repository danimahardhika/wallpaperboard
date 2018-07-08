package com.dm.wallpaper.board.activities.configurations;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.dm.wallpaper.board.helpers.TypefaceHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: Dani Mahardhika
 * Created on: 10/28/2017
 * https://github.com/danimahardhika
 */

public class SplashScreenConfiguration {

    @NonNull private Class<?> mMainActivity;
    @ColorInt private int mBottomTextColor;
    private String mBottomText;
    @FontSize private int mBottomTextSize;
    @FontStyle private int mBottomTextFont;

    public SplashScreenConfiguration(@NonNull Class<?> mainActivity) {
        mMainActivity = mainActivity;
        mBottomTextColor = -1;
        mBottomTextFont = FontStyle.REGULAR;
        mBottomTextSize = FontSize.REGULAR;
    }

    public SplashScreenConfiguration setBottomText(String text) {
        mBottomText = text;
        return this;
    }

    public SplashScreenConfiguration setBottomTextColor(@ColorInt int color) {
        mBottomTextColor = color;
        return this;
    }

    public SplashScreenConfiguration setBottomTextSize(@FontSize int fontSize) {
        mBottomTextSize = fontSize;
        return this;
    }

    public SplashScreenConfiguration setBottomTextFont(@FontStyle int fontStyle) {
        mBottomTextFont = fontStyle;
        return this;
    }

    public Class<?> getMainActivity() {
        return mMainActivity;
    }

    public String getBottomText() {
        return mBottomText;
    }

    public int getBottomTextColor() {
        return mBottomTextColor;
    }

    public float getBottomTextSize() {
        switch (mBottomTextSize) {
            case FontSize.SMALL:
                return 14f;
            case FontSize.LARGE:
                return 16f;
            case FontSize.REGULAR:
            default:
                return 15f;
        }
    }

    public Typeface getBottomTextFont(@NonNull Context context) {
        switch (mBottomTextFont) {
            case FontStyle.MEDIUM:
                return TypefaceHelper.getMedium(context);
            case FontStyle.BOLD:
                return TypefaceHelper.getBold(context);
            case FontStyle.LOGO:
                return TypefaceHelper.getLogo(context);
            case FontStyle.REGULAR:
            default:
                return TypefaceHelper.getRegular(context);
        }
    }

    @IntDef({FontSize.SMALL, FontSize.REGULAR, FontSize.LARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FontSize {
        int SMALL = 0;
        int REGULAR = 1;
        int LARGE = 2;
    }

    @IntDef({FontStyle.REGULAR, FontStyle.MEDIUM, FontStyle.BOLD, FontStyle.LOGO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FontStyle {
        int REGULAR = 0;
        int MEDIUM = 1;
        int BOLD = 2;
        int LOGO = 3;
    }
}
