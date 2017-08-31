package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;

import com.danimahardhika.android.helpers.core.ContextHelper;
import com.dm.wallpaper.board.R;

import static com.danimahardhika.android.helpers.core.ViewHelper.getToolbarHeight;
import static com.danimahardhika.android.helpers.core.WindowHelper.getStatusBarHeight;
import static com.danimahardhika.android.helpers.core.WindowHelper.getNavigationBarHeight;

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

public class ViewHelper {

    public static void resetViewBottomPadding(@Nullable View view, boolean scroll) {
        if (view == null) return;

        Context context = ContextHelper.getBaseContext(view);
        int orientation = context.getResources().getConfiguration().orientation;

        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingTop();
        int top = view.getPaddingTop();
        int navBar = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = context.getResources().getBoolean(R.bool.android_helpers_tablet_mode);
            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                navBar = getNavigationBarHeight(context);
            }

            if (!scroll) {
                navBar += getStatusBarHeight(context);
            }
        }

        if (!scroll) {
            navBar += getToolbarHeight(context);
        }
        view.setPadding(left, top, right, (bottom + navBar));
    }
}
