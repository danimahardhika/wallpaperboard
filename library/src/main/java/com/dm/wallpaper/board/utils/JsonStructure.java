package com.dm.wallpaper.board.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

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

public class JsonStructure {

    private Builder mBuilder;

    private JsonStructure(@NonNull Builder builder) {
        mBuilder = builder;
    }

    @Nullable
    public String jsonOutputUrl() {
        return mBuilder.mJsonOutputUrl;
    }

    @NonNull
    public List<NameValuePair> jsonOutputPost() {
        return mBuilder.mValues;
    }

    @NonNull
    public CategoryStructure categoryStructure() {
        return mBuilder.mCategoryStructure;
    }

    @NonNull
    public WallpaperStructure wallpaperStructure() {
        return mBuilder.mWallpaperStructure;
    }

    public static class Builder {

        private String mJsonOutputUrl = null;
        private List<NameValuePair> mValues = new ArrayList<>();

        private CategoryStructure mCategoryStructure = new CategoryStructure("Categories");
        private WallpaperStructure mWallpaperStructure = new WallpaperStructure("Wallpapers");

        public Builder jsonOutputUrl(@Nullable String url) {
            if (url != null && URLUtil.isValidUrl(url)) {
                mJsonOutputUrl = url;
            }
            return this;
        }

        public Builder jsonOutputPost(@NonNull String tag, @NonNull String value) {
            mValues.add(new BasicNameValuePair(tag, value));
            return this;
        }

        public Builder categoryStructure(@NonNull CategoryStructure categoryStructure) {
            mCategoryStructure = categoryStructure;
            return this;
        }

        public Builder wallpaperStructure(@NonNull WallpaperStructure wallpaperStructure) {
            mWallpaperStructure = wallpaperStructure;
            return this;
        }

        public JsonStructure build() {
            return new JsonStructure(this);
        }
    }

    public static class WallpaperStructure {

        private final String mArrayName;
        private String mName = "name";
        private String mAuthor = "author";
        private String mUrl = "url";
        private String mThumbUrl = "thumbUrl";
        private String mCategory = "category";

        private String mAuthorUrl = null;
        private String mAuthorThumbnail = null;

        public WallpaperStructure(@NonNull String arrayName) {
            mArrayName = arrayName;
        }

        public WallpaperStructure name(@Nullable String name) {
            mName = name;
            return this;
        }

        public WallpaperStructure author(@NonNull String author) {
            mAuthor = author;
            return this;
        }

        //Todo: make it public
        private WallpaperStructure authorUrl(@Nullable String authorUrl) {
            mAuthorUrl = authorUrl;
            return this;
        }

        //Todo: make it public
        private WallpaperStructure authorThumbnail(@Nullable String authorThumbnail) {
            mAuthorThumbnail = authorThumbnail;
            return this;
        }

        public WallpaperStructure url(@NonNull String url) {
            mUrl = url;
            return this;
        }

        public WallpaperStructure thumbUrl(@Nullable String thumbUrl) {
            mThumbUrl = thumbUrl;
            return this;
        }

        public WallpaperStructure category(@NonNull String category) {
            mCategory = category;
            return this;
        }

        @NonNull
        public String getArrayName() {
            return mArrayName;
        }

        @Nullable
        public String getName() {
            return mName;
        }

        @NonNull
        public String getAuthor() {
            return mAuthor;
        }

        @NonNull
        public String getUrl() {
            return mUrl;
        }

        @Nullable
        public String getThumbUrl() {
            return mThumbUrl;
        }

        @NonNull
        public String getCategory() {
            return mCategory;
        }

        //Todo: make it public
        @Nullable
        private String getAuthorUrl() {
            return mAuthorUrl;
        }

        //Todo: make it public
        @Nullable
        private String getAuthorThumbnail() {
            return mAuthorThumbnail;
        }
    }

    public static class CategoryStructure {

        private final String mArrayName;
        private String mName = "name";

        public CategoryStructure(@NonNull String arrayName) {
            mArrayName = arrayName;
        }

        public CategoryStructure name(@Nullable String name) {
            mName = name;
            return this;
        }

        @NonNull
        public String getArrayName() {
            return mArrayName;
        }

        @Nullable
        public String getName() {
            return mName;
        }
    }
}
