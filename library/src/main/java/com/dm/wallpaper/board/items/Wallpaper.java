package com.dm.wallpaper.board.items;

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

public class Wallpaper {

    private int mId;
    private final String mName;
    private final String mAuthor;
    private final String mThumbUrl;
    private final String mUrl;
    private String mCategory;
    private boolean mIsFavorite;

    public Wallpaper(int id, String name, String author, String url, String thumbUrl, boolean isFavorite) {
        mId = id;
        mName = name;
        mAuthor = author;
        mUrl = url;
        mThumbUrl = thumbUrl;
        mIsFavorite = isFavorite;
    }

    public Wallpaper(String name, String author, String url, String thumbUrl, String category) {
        mName = name;
        mAuthor = author;
        mUrl = url;
        mThumbUrl = thumbUrl;
        mCategory = category;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getCategory() {
        return mCategory;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;
        if (object != null && object instanceof Wallpaper) {
            equals = mUrl.equals(((Wallpaper) object).getUrl()) &&
                    mCategory.equals(((Wallpaper) object).getCategory());
        }
        return equals;
    }
}
