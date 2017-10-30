package com.dm.wallpaper.board.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;

import com.dm.wallpaper.board.items.ColorPalette;
import com.dm.wallpaper.board.utils.LogUtil;

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

public class WallpaperPaletteLoaderTask {

    private Callback mCallback;
    private Bitmap mBitmap;

    private WallpaperPaletteLoaderTask(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public WallpaperPaletteLoaderTask callback(@Nullable Callback callback) {
        mCallback = callback;
        return this;
    }

    public AsyncTask start() {
        if (mBitmap == null) {
            LogUtil.e("PaletteLoader cancelled, bitmap is null");
            return null;
        }

        try {
            return Palette.from(mBitmap).generate(palette -> {
                int dominant = palette.getDominantColor(0);
                int vibrant = palette.getVibrantColor(0);
                int vibrantLight = palette.getLightVibrantColor(0);
                int vibrantDark = palette.getDarkVibrantColor(0);
                int muted = palette.getMutedColor(0);
                int mutedLight = palette.getLightMutedColor(0);
                int mutedDark = palette.getDarkMutedColor(0);

                ColorPalette colorPalette = new ColorPalette();
                colorPalette.add(dominant);
                colorPalette.add(vibrant);
                colorPalette.add(vibrantLight);
                colorPalette.add(vibrantDark);
                colorPalette.add(muted);
                colorPalette.add(mutedLight);
                colorPalette.add(mutedDark);
                if (mCallback != null) {
                    mCallback.onPaletteGenerated(colorPalette);
                }
            });
        } catch (Exception ignored) {}
        return null;
    }

    public static WallpaperPaletteLoaderTask with(Bitmap bitmap) {
        return new WallpaperPaletteLoaderTask(bitmap);
    }

    public interface Callback {
        void onPaletteGenerated(ColorPalette palette);
    }
}
