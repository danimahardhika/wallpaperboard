package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.utils.JsonStructure;
import com.dm.wallpaper.board.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class MuzeiHelper {

    private final Context mContext;

    public MuzeiHelper(@NonNull Context context) {
        mContext = context;
    }

    @Nullable
    public Wallpaper getRandomWallpaper(String wallpaperUrl) throws Exception {
        if (Database.get(mContext).getWallpapersCount() == 0) {
            URL url = new URL(wallpaperUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                Map<String, List> map = LoganSquare.parseMap(stream, List.class);
                if (map == null) return null;

                JsonStructure.WallpaperStructure wallpaperStructure = WallpaperBoardApplication
                        .getConfiguration().getJsonStructure().getWallpaper();
                List wallpaperList = map.get(wallpaperStructure.getArrayName());
                stream.close();
                if (wallpaperList == null) {
                    LogUtil.e("Muzei: Json error: wallpaper array with name "
                            +wallpaperStructure.getArrayName() +" not found");
                    return null;
                }

                if (wallpaperList.size() > 0) {
                    int position = getRandomInt(wallpaperList.size());
                    return JsonHelper.getWallpaper(wallpaperList.get(position));
                }
            }
            return null;
        } else {
            return Database.get(mContext).getRandomWallpaper();
        }
    }

    private int getRandomInt(int size) {
        try {
            Random random = new Random();
            return random.nextInt(size);
        } catch (Exception e) {
            return 0;
        }
    }
}
