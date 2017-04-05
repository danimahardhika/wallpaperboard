package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;

import com.dm.wallpaper.board.R;

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

public class DrawableHelper {

    public static int getResourceId(@NonNull Context context, String resName) {
        try {
            return context.getResources().getIdentifier(
                    resName, "drawable", context.getPackageName());
        } catch (Exception ignored) {}
        return -1;
    }

    @Nullable
    public static Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int res, @ColorInt int color) {
        try {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            return drawable.mutate();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Nullable
    public static Drawable getDefaultImage(@NonNull Context context, @DrawableRes int res,
                                           @ColorInt int color, int padding) {
        try {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            Bitmap tintedBitmap = Bitmap.createBitmap(
                    bitmap.getWidth() + padding,
                    bitmap.getHeight() + padding,
                    Bitmap.Config.ARGB_8888);
            Canvas tintedCanvas = new Canvas(tintedBitmap);
            int background = ColorHelper.getAttributeColor(context, R.attr.card_background);
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            tintedCanvas.drawColor(background, PorterDuff.Mode.ADD);
            tintedCanvas.drawBitmap(bitmap,
                    (tintedCanvas.getWidth() - bitmap.getWidth())/2,
                    (tintedCanvas.getHeight() - bitmap.getHeight())/2, paint);
            return new BitmapDrawable(context.getResources(), tintedBitmap);
        } catch (Exception | OutOfMemoryError e) {
            return null;
        }
    }
}
