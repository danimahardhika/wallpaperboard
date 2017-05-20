package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
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

    public static void resetViewBottomMargin(@Nullable View view) {
        if (view == null) return;

        Context context = ContextHelper.getBaseContext(view);
        int orientation = context.getResources().getConfiguration().orientation;

        if (!(view.getLayoutParams() instanceof CoordinatorLayout.LayoutParams))
            return;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        int left = params.leftMargin;
        int right = params.rightMargin;
        int bottom = params.bottomMargin;
        int top = params.topMargin;
        int bottomNavBar = 0;
        int rightNavBar = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = context.getResources().getBoolean(R.bool.android_helpers_tablet_mode);
            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                bottomNavBar = getNavigationBarHeight(context);
            } else {
                rightNavBar = getNavigationBarHeight(context);
            }
        }

        int navBar = getNavigationBarHeight(context);
        if ((bottom > bottomNavBar) && ((bottom - navBar) > 0))
            bottom -= navBar;
        if ((right > rightNavBar) && ((right - navBar) > 0))
            right -= navBar;

        params.setMargins(left, top, (right + rightNavBar), (bottom + bottomNavBar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginEnd((right + rightNavBar));
        }
        view.setLayoutParams(params);
    }

    public static Point getNavigationViewHeaderStyle(String style) {
        switch (style) {
            case "mini":
                return new Point(16, 9);
            default:
                return new Point(4, 3);
        }
    }
}
