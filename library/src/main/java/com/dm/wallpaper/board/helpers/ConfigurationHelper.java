package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;

import static com.danimahardhika.android.helpers.core.DrawableHelper.get;

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

public class ConfigurationHelper {

    @NonNull
    public static Drawable getNavigationIcon(@NonNull Context context, @NonNull WallpaperBoardApplication.NavigationIcon navigationIcon) {
        switch (navigationIcon) {
            case DEFAULT:
                return get(context, R.drawable.ic_toolbar_navigation);
            case STYLE_1:
                return get(context, R.drawable.ic_toolbar_navigation_1);
            case STYLE_2:
                return get(context, R.drawable.ic_toolbar_navigation_2);
            case STYLE_3:
                return get(context, R.drawable.ic_toolbar_navigation_3);
            case STYLE_4:
                return get(context, R.drawable.ic_toolbar_navigation_4);
            default:
                return get(context, R.drawable.ic_toolbar_navigation);
        }
    }
}
