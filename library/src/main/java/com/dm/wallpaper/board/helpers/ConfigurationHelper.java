package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.applications.WallpaperBoardConfiguration;

import static com.danimahardhika.android.helpers.core.DrawableHelper.getTintedDrawable;

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

public abstract class ConfigurationHelper {

    @Nullable
    public static Drawable getNavigationIcon(@NonNull Context context, @WallpaperBoardConfiguration.NavigationIcon int navigationIcon) {
        int color = ColorHelper.getAttributeColor(context, R.attr.toolbar_icon);
        switch (navigationIcon) {
            case WallpaperBoardConfiguration.NavigationIcon.STYLE_1:
                return getTintedDrawable(context, R.drawable.ic_toolbar_navigation_1, color);
            case WallpaperBoardConfiguration.NavigationIcon.STYLE_2:
                return getTintedDrawable(context, R.drawable.ic_toolbar_navigation_2, color);
            case WallpaperBoardConfiguration.NavigationIcon.STYLE_3:
                return getTintedDrawable(context, R.drawable.ic_toolbar_navigation_3, color);
            case WallpaperBoardConfiguration.NavigationIcon.STYLE_4:
                return getTintedDrawable(context, R.drawable.ic_toolbar_navigation_4, color);
            case WallpaperBoardConfiguration.NavigationIcon.DEFAULT:
            default:
                return getTintedDrawable(context, R.drawable.ic_toolbar_navigation, color);
        }
    }
}
