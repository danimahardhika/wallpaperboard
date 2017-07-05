package com.dm.wallpaper.board.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.webkit.URLUtil;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.helpers.JsonHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.receivers.WallpaperBoardReceiver;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.JsonStructure;
import com.dm.wallpaper.board.utils.LogUtil;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.NameValuePair;

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

public class WallpaperBoardService extends IntentService {

    private static final String SERVICE = "com.dm.wallpaper.board.service";

    public WallpaperBoardService() {
        super(SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String wallpaperUrl = WallpaperBoardApplication.getConfiguration().getJsonStructure().jsonOutputUrl();
            if (wallpaperUrl == null) {
                wallpaperUrl = getResources().getString(R.string.wallpaper_json);
            }
            if (!URLUtil.isValidUrl(wallpaperUrl)) return;

            URL url = new URL(wallpaperUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            if (WallpaperBoardApplication.getConfiguration().getJsonStructure().jsonOutputUrl() != null) {
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                List<NameValuePair> values = WallpaperBoardApplication.getConfiguration()
                        .getJsonStructure().jsonOutputPost();
                if (values.size() > 0) {
                    DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                    dStream.writeBytes(JsonHelper.getQuery(values));
                    dStream.flush();
                    dStream.close();
                }
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                Map<String, List> map = LoganSquare.parseMap(stream, List.class);
                if (map == null) return;

                JsonStructure.WallpaperStructure wallpaperStructure = WallpaperBoardApplication
                        .getConfiguration().getJsonStructure().wallpaperStructure();
                List wallpaperList = map.get(wallpaperStructure.getArrayName());
                if (wallpaperList == null) {
                    LogUtil.e("Service: Json error: wallpaper array with name "
                            +wallpaperStructure.getArrayName() +" not found");
                    return;
                }

                List<Wallpaper> wallpapers = new ArrayList<>();
                for (int i = 0; i < wallpaperList.size(); i++) {
                    Wallpaper wallpaper = JsonHelper.getWallpaper(wallpaperList.get(i));
                    if (wallpaper != null) {
                        if (!wallpapers.contains(wallpaper)) {
                            wallpapers.add(wallpaper);
                        } else {
                            LogUtil.e("Duplicate wallpaper found: " +wallpaper.getUrl());
                        }
                    }
                }

                int size = wallpapers.size();

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(WallpaperBoardReceiver.PROCESS_RESPONSE);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

                broadcastIntent.putExtra(Extras.EXTRA_PACKAGE_NAME, getPackageName());
                broadcastIntent.putExtra(Extras.EXTRA_SIZE, size);
                sendBroadcast(broadcastIntent);
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }
}
