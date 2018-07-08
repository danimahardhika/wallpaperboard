package com.dm.wallpaper.board.utils;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.URLUtil;

import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.helpers.WallpaperHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;

import java.io.File;

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

public class WallpaperDownloader {

    private final Context mContext;
    private Wallpaper mWallpaper;

    private WallpaperDownloader(Context context) {
        mContext = context;
    }

    public WallpaperDownloader wallpaper(@NonNull Wallpaper wallpaper) {
        mWallpaper = wallpaper;
        return this;
    }

    public void start() {
        String fileName = mWallpaper.getName() +"."+ WallpaperHelper.getFormat(mWallpaper.getMimeType());
        File directory = WallpaperHelper.getDefaultWallpapersDirectory(mContext);
        File target = new File(directory, fileName);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                LogUtil.e("Unable to create directory " +directory.toString());
                showCafeBar(R.string.wallpaper_download_failed);
                return;
            }
        }

        if (WallpaperHelper.isWallpaperSaved(mContext, mWallpaper)) {
            CafeBar.builder(mContext)
                    .theme(Preferences.get(mContext).isDarkTheme() ? CafeBarTheme.LIGHT : CafeBarTheme.DARK)
                    .floating(true)
                    .fitSystemWindow()
                    .duration(CafeBar.Duration.MEDIUM)
                    .typeface(TypefaceHelper.getRegular(mContext), TypefaceHelper.getBold(mContext))
                    .content(R.string.wallpaper_already_downloaded)
                    .neutralText(R.string.open)
                    .onNeutral(cafeBar -> {
                        Uri uri = FileHelper.getUriFromFile(mContext, mContext.getPackageName(), target);
                        if (uri == null) {
                            cafeBar.dismiss();
                            return;
                        }

                        try {
                            mContext.startActivity(new Intent()
                                    .setAction(Intent.ACTION_VIEW)
                                    .setDataAndType(uri, "image/*")
                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                        } catch (ActivityNotFoundException e) {
                            LogUtil.e(Log.getStackTraceString(e));
                        }

                        cafeBar.dismiss();
                    })
                    .show();
            return;
        }

        if (!URLUtil.isValidUrl(mWallpaper.getUrl())) {
            LogUtil.e("Download: wallpaper url is not valid");
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mWallpaper.getUrl()));
        request.setMimeType(mWallpaper.getMimeType());
        request.setTitle(fileName);
        request.setDescription(mContext.getResources().getString(R.string.wallpaper_downloading));
        request.allowScanningByMediaScanner();
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(target));

        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        try {
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                return;
            }

            LogUtil.e("Download: download manager is null");
        } catch (IllegalArgumentException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return;
        }

        showCafeBar(R.string.wallpaper_downloading);
    }

    private void showCafeBar(int res) {
        CafeBar.builder(mContext)
                .theme(Preferences.get(mContext).isDarkTheme() ? CafeBarTheme.LIGHT : CafeBarTheme.DARK)
                .contentTypeface(TypefaceHelper.getRegular(mContext))
                .content(res)
                .floating(true)
                .fitSystemWindow()
                .show();
    }

    public static WallpaperDownloader prepare(@NonNull Context context) {
        return new WallpaperDownloader(context);
    }
}
