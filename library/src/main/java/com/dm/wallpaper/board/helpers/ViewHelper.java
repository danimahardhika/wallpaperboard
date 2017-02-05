package com.dm.wallpaper.board.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.utils.Extras;
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

    public static void resetSpanCount(@NonNull Context context, @NonNull RecyclerView recyclerView) {
        try {
            GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
            manager.setSpanCount(context.getResources().getInteger(R.integer.column_num));
            manager.requestLayout();
        } catch (Exception e) {
            Log.d(Extras.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    public static void resetNavigationBarTranslucent(@NonNull Context context, int orientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
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

    public static void resetNavigationBarBottomPadding(@NonNull Context context, @Nullable View view,
                                                       int orientation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (view != null) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    view.setPadding(0, 0, 0, getNavigationBarHeight(context));
                else
                    view.setPadding(0, 0, 0, 0);
            }
        }
    }

    public static void disableAppBarDrag(@Nullable AppBarLayout appBar) {
        if (appBar != null) {
            if (ViewCompat.isLaidOut(appBar)) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                        appBar.getLayoutParams();
                AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                if (behavior != null) {
                    behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                        @Override
                        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                            return false;
                        }
                    });
                }
            }
        }
    }

    public static int getNavigationBarHeight(@NonNull Context context) {
        Resources resources = context.getResources();
        int orientation = resources.getConfiguration().orientation;
        int resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ?
                "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static void setApplicationWindowColor(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            theme.resolveAttribute(R.attr.toolbar_color, typedValue, true);
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

    public static void changeSearchViewActionModeColor(@NonNull Context context, @Nullable View view,
                                                       @AttrRes int original, @AttrRes int target) {
        if (view != null) {
            CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) view;
            int originalColor = ColorHelper.getAttributeColor(context, original);
            int targetColor = ColorHelper.getAttributeColor(context, target);

            ColorDrawable cd1 = new ColorDrawable(originalColor);
            ColorDrawable cd2 = new ColorDrawable(targetColor);

            TransitionDrawable td = new TransitionDrawable(new Drawable[]{cd1, cd2});
            collapsingToolbar.setContentScrim(td);
            td.startTransition(200);
        }
    }

}
