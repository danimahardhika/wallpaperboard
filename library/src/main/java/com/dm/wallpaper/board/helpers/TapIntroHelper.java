package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.UnitHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.preferences.Preferences;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

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

public class TapIntroHelper {

    public static void showWallpapersIntro(@NonNull Context context, @Nullable RecyclerView recyclerView) {
        if (Preferences.get(context).isTimeToShowWallpapersIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

            new Handler().postDelayed(() -> {
                int primary = ColorHelper.getAttributeColor(context, R.attr.toolbar_icon);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                tapTargetSequence.continueOnCancel(true);

                Typeface title = TypefaceHelper.getMedium(context);
                Typeface description = TypefaceHelper.getRegular(context);

                if (toolbar != null) {
                    TapTarget tapTarget = TapTarget.forToolbarNavigationIcon(toolbar,
                            context.getResources().getString(R.string.tap_intro_wallpapers_navigation),
                            context.getResources().getString(R.string.tap_intro_wallpapers_navigation_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .drawShadow(Preferences.get(context).isShadowEnabled());

                    TapTarget tapTarget1 = TapTarget.forToolbarMenuItem(toolbar, R.id.menu_search,
                            context.getResources().getString(R.string.tap_intro_wallpapers_search),
                            context.getResources().getString(R.string.tap_intro_wallpapers_search_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .drawShadow(Preferences.get(context).isShadowEnabled());

                    TapTarget tapTarget2 = TapTarget.forToolbarMenuItem(toolbar, R.id.menu_filter,
                            context.getResources().getString(R.string.tap_intro_wallpapers_filter),
                            context.getResources().getString(R.string.tap_intro_wallpapers_filter_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .drawShadow(Preferences.get(context).isShadowEnabled());

                    if (title != null) {
                        tapTarget.titleTypeface(title);
                        tapTarget1.titleTypeface(title);
                        tapTarget2.titleTypeface(title);
                    }

                    if (description != null) {
                        tapTarget.descriptionTypeface(description);
                        tapTarget1.descriptionTypeface(description);
                        tapTarget2.descriptionTypeface(description);
                    }

                    tapTargetSequence.target(tapTarget);
                    tapTargetSequence.target(tapTarget1);
                    tapTargetSequence.target(tapTarget2);
                }

                if (recyclerView != null) {
                    int position = 0;
                    if (recyclerView.getAdapter() != null) {
                        if (position < recyclerView.getAdapter().getItemCount()) {
                            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                            if (holder == null) return;

                            View view = holder.itemView.findViewById(R.id.image);
                            if (view != null) {
                                float targetRadius = UnitHelper.toDp(context, (float) view.getMeasuredWidth()) - 10f;

                                String desc = String.format(context.getResources().getString(R.string.tap_intro_wallpapers_option_desc),
                                        context.getResources().getBoolean(R.bool.enable_wallpaper_download) ?
                                                context.getResources().getString(R.string.tap_intro_wallpapers_option_desc_download) : "");

                                TapTarget tapTarget = TapTarget.forView(view,
                                        context.getResources().getString(R.string.tap_intro_wallpapers_option),
                                        desc)
                                        .titleTextColorInt(primary)
                                        .descriptionTextColorInt(secondary)
                                        .targetCircleColorInt(primary)
                                        .targetRadius((int) targetRadius)
                                        .tintTarget(false)
                                        .drawShadow(Preferences.get(context).isShadowEnabled());

                                TapTarget tapTarget1 = TapTarget.forView(view,
                                        context.getResources().getString(R.string.tap_intro_wallpapers_preview),
                                        context.getResources().getString(R.string.tap_intro_wallpapers_preview_desc))
                                        .titleTextColorInt(primary)
                                        .descriptionTextColorInt(secondary)
                                        .targetCircleColorInt(primary)
                                        .targetRadius((int) targetRadius)
                                        .tintTarget(false)
                                        .drawShadow(Preferences.get(context).isShadowEnabled());

                                if (title != null) {
                                    tapTarget.titleTypeface(title);
                                    tapTarget1.titleTypeface(title);
                                }

                                if (description != null) {
                                    tapTarget.descriptionTypeface(description);
                                    tapTarget1.descriptionTypeface(description);
                                }

                                tapTargetSequence.target(tapTarget);
                                tapTargetSequence.target(tapTarget1);
                            }

                            View favorite = holder.itemView.findViewById(R.id.favorite);
                            if (favorite != null) {
                                TapTarget tapTarget = TapTarget.forView(favorite,
                                        context.getResources().getString(R.string.tap_intro_wallpapers_favorite),
                                        context.getResources().getString(R.string.tap_intro_wallpapers_favorite_desc))
                                        .titleTextColorInt(primary)
                                        .descriptionTextColorInt(secondary)
                                        .targetCircleColorInt(primary)
                                        .drawShadow(Preferences.get(context).isShadowEnabled());

                                if (title != null) {
                                    tapTarget.titleTypeface(title);
                                }

                                if (description != null) {
                                    tapTarget.descriptionTypeface(description);
                                }

                                tapTargetSequence.target(tapTarget);
                            }
                        }
                    }

                    tapTargetSequence.listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            Preferences.get(context).setTimeToShowWallpapersIntro(false);
                        }

                        @Override
                        public void onSequenceStep(TapTarget tapTarget, boolean b) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget tapTarget) {

                        }
                    });
                    tapTargetSequence.start();
                }
            }, 200);
        }
    }

    public static void showWallpaperPreviewIntro(@NonNull Context context, @ColorInt int color) {
        if (Preferences.get(context).isTimeToShowWallpaperPreviewIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

            new Handler().postDelayed(() -> {
                int primary = ColorHelper.getTitleTextColor(color);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                tapTargetSequence.continueOnCancel(true);

                Typeface title = TypefaceHelper.getMedium(context);
                Typeface description = TypefaceHelper.getRegular(context);

                if (toolbar != null) {
                    TapTarget tapTarget = TapTarget.forToolbarMenuItem(toolbar, R.id.menu_wallpaper_settings,
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_settings),
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_settings_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .outerCircleColorInt(color)
                            .drawShadow(Preferences.get(context).isShadowEnabled());

                    if (title != null) {
                        tapTarget.titleTypeface(title);
                    }

                    if (description != null) {
                        tapTarget.descriptionTypeface(description);
                    }

                    tapTargetSequence.target(tapTarget);

                    if (context.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                        TapTarget tapTarget1 = TapTarget.forToolbarMenuItem(toolbar, R.id.menu_save,
                                context.getResources().getString(R.string.tap_intro_wallpaper_preview_save),
                                context.getResources().getString(R.string.tap_intro_wallpaper_preview_save_desc))
                                .titleTextColorInt(primary)
                                .descriptionTextColorInt(secondary)
                                .targetCircleColorInt(primary)
                                .outerCircleColorInt(color)
                                .drawShadow(Preferences.get(context).isShadowEnabled());

                        if (title != null) {
                            tapTarget1.titleTypeface(title);
                        }

                        if (description != null) {
                            tapTarget1.descriptionTypeface(description);
                        }

                        tapTargetSequence.target(tapTarget1);
                    }
                }

                View fab = activity.findViewById(R.id.fab);
                if (fab != null) {
                    TapTarget tapTarget = TapTarget.forView(fab,
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_apply),
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_apply_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .outerCircleColorInt(color)
                            .tintTarget(false)
                            .drawShadow(Preferences.get(context).isShadowEnabled());

                    if (title != null) {
                        tapTarget.titleTypeface(title);
                    }

                    if (description != null) {
                        tapTarget.descriptionTypeface(description);
                    }

                    tapTargetSequence.target(tapTarget);
                }

                tapTargetSequence.listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Preferences.get(context).setTimeToShowWallpaperPreviewIntro(false);
                    }

                    @Override
                    public void onSequenceStep(TapTarget tapTarget, boolean b) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget tapTarget) {

                    }
                });
                tapTargetSequence.start();
            }, 100);
        }
    }
}
