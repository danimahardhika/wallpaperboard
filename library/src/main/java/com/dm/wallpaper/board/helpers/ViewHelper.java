package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.danimahardhika.android.helpers.core.ContextHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.applications.WallpaperBoardConfiguration;

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

    public static void setCardViewToFlat(@Nullable CardView cardView) {
        if (cardView == null) return;

        if (WallpaperBoardApplication.getConfig().getWallpapersGrid() ==
                WallpaperBoardConfiguration.GridStyle.FLAT) {
            cardView.setRadius(0f);
            cardView.setUseCompatPadding(false);

            Context context = ContextHelper.getBaseContext(cardView);
            int margin = context.getResources().getDimensionPixelSize(R.dimen.card_margin);

            if (cardView.getLayoutParams() instanceof GridLayoutManager.LayoutParams) {
                GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) cardView.getLayoutParams();
                params.setMargins(0, 0, margin, margin);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setMarginEnd(margin);
                }
            } else if (cardView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams params =
                        (StaggeredGridLayoutManager.LayoutParams) cardView.getLayoutParams();
                params.setMargins(0, 0, margin, margin);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setMarginEnd(margin);
                }
            }
        }
    }

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
