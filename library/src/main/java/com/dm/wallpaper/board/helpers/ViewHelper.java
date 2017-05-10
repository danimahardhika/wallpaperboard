package com.dm.wallpaper.board.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    public static void setupToolbar(@NonNull Toolbar toolbar, boolean adjustHeight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Context context = ContextHelper.getBaseContext(toolbar);
            int statusBarSize = ViewHelper.getStatusBarHeight(context);
            toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    toolbar.getPaddingTop() + statusBarSize,
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom()
            );

            if (adjustHeight) {
                toolbar.getLayoutParams().height = getToolbarHeight(context) + statusBarSize;
            }
        }
    }

    public static int getToolbarHeight(@NonNull Context context) {
        TypedValue typedValue = new TypedValue();
        int[] actionBarSize = new int[] { R.attr.actionBarSize };
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, actionBarSize);
        int size = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return size;
    }

    public static void resetSpanCount(@NonNull Context context, @NonNull RecyclerView recyclerView) {
        try {
            GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
            manager.setSpanCount(context.getResources().getInteger(R.integer.wallpapers_column_count));
            manager.requestLayout();
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public static void resetSpanCount(@NonNull Context context, @NonNull RecyclerView recyclerView, @IntegerRes int res) {
        try {
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                manager.setSpanCount(context.getResources().getInteger(res));
                manager.requestLayout();
            } else if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                manager.setSpanCount(context.getResources().getInteger(res));
                manager.requestLayout();
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public static void resetNavigationBarTranslucent(@NonNull Context context, int orientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = context.getResources().getBoolean(R.bool.tablet_mode);

            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                ((AppCompatActivity) context).getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((AppCompatActivity) context).getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                ColorHelper.setNavigationBarColor(context, Color.BLACK);
            }
        }
    }

    public static void disableTranslucentNavigationBar(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((AppCompatActivity) context).getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    public static void resetViewBottomPadding(@Nullable View view, boolean scroll) {
        if (view == null) return;

        Context context = ContextHelper.getBaseContext(view);
        int orientation = context.getResources().getConfiguration().orientation;

        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();
        int top = view.getPaddingTop();
        int navBar = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = context.getResources().getBoolean(R.bool.tablet_mode);
            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                navBar = getNavigationBarHeight(context);
            }

            if (!scroll) {
                navBar += ViewHelper.getStatusBarHeight(context);
            }
        }

        if (bottom > navBar) bottom -= getNavigationBarHeight(context);
        if (!scroll) {
            navBar += ViewHelper.getToolbarHeight(context);
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
            boolean tabletMode = context.getResources().getBoolean(R.bool.tablet_mode);
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

    public static int getStatusBarHeight(@NonNull Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getNavigationBarHeight(@NonNull Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        if (appUsableSize.x < realScreenSize.x) {
            Point point = new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
            return point.x;
        }

        if (appUsableSize.y < realScreenSize.y) {
            Point point = new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
            return point.y;
        }
        return 0;
    }

    private static Point getAppUsableScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(@NonNull Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
                size.x = display.getWidth();
                size.y = display.getHeight();
            }
        }
        return size;
    }

    public static void setApplicationWindowColor(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync("drawable://"
                    +DrawableHelper.getResourceId(context, "icon"));
            ((AppCompatActivity) context).setTaskDescription(new ActivityManager.TaskDescription (
                    context.getResources().getString(R.string.app_name),
                    bitmap,
                    typedValue.data));
        }
    }

    public static void changeSearchViewTextColor(@Nullable View view, int text, int hint) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(text);
                ((TextView) view).setHintTextColor(hint);
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i), text, hint);
                }
            }
        }
    }

    public static void removeSearchViewSearchIcon(@Nullable View view) {
        if (view != null) {
            ImageView searchIcon = (ImageView) view;
            ViewGroup linearLayoutSearchView = (ViewGroup) view.getParent();
            if (linearLayoutSearchView != null) {
                linearLayoutSearchView.removeView(searchIcon);
                linearLayoutSearchView.addView(searchIcon);

                searchIcon.setAdjustViewBounds(true);
                searchIcon.setMaxWidth(0);
                searchIcon.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                searchIcon.setImageDrawable(null);
            }
        }
    }

    public static float intToDp(@NonNull Context context, int value) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return value / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
