package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.JsonStructure;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

public class JsonHelper {

    @NonNull
    public static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first) first = false;
            else result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    @Nullable
    public static Wallpaper getWallpaper(@NonNull Object object) {
        if (object instanceof Map) {
            JsonStructure jsonStructure = WallpaperBoardApplication.getConfiguration().getJsonStructure();
            JsonStructure.WallpaperStructure wallpaperStructure = jsonStructure.getWallpaper();

            Map map = (Map) object;
            return Wallpaper.Builder()
                    .name((String) map.get(wallpaperStructure.getName()))
                    .author((String) map.get(wallpaperStructure.getAuthor()))
                    .url((String) map.get(wallpaperStructure.getUrl()))
                    .thumbUrl(getThumbUrl(map))
                    .category((String) map.get(wallpaperStructure.getCategory()))
                    .build();
        }
        return null;
    }

    @Nullable
    public static Category getCategory(@NonNull Object object) {
        if (object instanceof Map) {
            JsonStructure jsonStructure = WallpaperBoardApplication.getConfiguration().getJsonStructure();
            JsonStructure.CategoryStructure categoryStructure = jsonStructure.getCategory();

            Map map = (Map) object;
            return Category.Builder()
                    .name((String) map.get(categoryStructure.getName()))
                    .build();
        }
        return null;
    }

    public static String getGeneratedName(@NonNull Context context, @Nullable String name) {
        if (name == null) {
            String generatedName = "Wallpaper " +Preferences.get(context).getAutoIncrement();
            Preferences.get(context).setAutoIncrement(
                    Preferences.get(context).getAutoIncrement() + 1);
            return generatedName;
        }
        return name;
    }

    public static String getThumbUrl(@NonNull Map map) {
        JsonStructure jsonStructure = WallpaperBoardApplication.getConfiguration().getJsonStructure();
        JsonStructure.WallpaperStructure wallpaperStructure = jsonStructure.getWallpaper();

        String url = (String) map.get(wallpaperStructure.getUrl());
        if (wallpaperStructure.getThumbUrl() == null) return url;

        String thumbUrl = (String) map.get(wallpaperStructure.getThumbUrl());
        if (thumbUrl == null) return url;
        return thumbUrl;
    }
}
