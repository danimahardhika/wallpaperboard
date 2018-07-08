package com.dm.wallpaper.board.items;

import com.nostra13.universalimageloader.core.assist.ImageSize;

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
    private final String mCategory;
    private final String mAddedOn;
    private int mColor;
    private String mMimeType;
    private int mSize;
    private boolean mIsFavorite;
    private ImageSize mDimensions;

    private Wallpaper(String name, String author, String url, String thumbUrl, String category, String addedOn) {
        mName = name;
        mAuthor = author;
        mUrl = url;
        mThumbUrl = thumbUrl;
        mCategory = category;
        mAddedOn = addedOn;
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

    public String getUrl() {
        return mUrl;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getAddedOn() {
        return mAddedOn;
    }

    public int getColor() {
        return mColor;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public int getSize() {
        return mSize;
    }

    public ImageSize getDimensions() {
        return mDimensions;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setId(int id) {
        mId = id;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public void setDimensions(ImageSize dimensions) {
        mDimensions = dimensions;
    }

    public void setFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;
        if (object != null && object instanceof Wallpaper) {
            equals = mAuthor.equals(((Wallpaper) object).getAuthor()) &&
                    mUrl.equals(((Wallpaper) object).getUrl()) &&
                    mThumbUrl.equals(((Wallpaper) object).getThumbUrl()) &&
                    mCategory.equals(((Wallpaper) object).getCategory());
        }
        return equals;
    }

    public static Builder Builder() {
        return new Builder();
    }

    public static class Builder {

        private int mId;
        private String mName;
        private String mAuthor;
        private String mThumbUrl;
        private String mUrl;
        private String mCategory;
        private String mAddedOn;
        private int mColor;
        private String mMimeType;
        private int mSize;
        private boolean mIsFavorite;
        private ImageSize mDimensions;

        private Builder() {
            mId = -1;
            mSize = 0;
            mColor = 0;
        }

        public Builder id(int id) {
            mId = id;
            return this;
        }

        public Builder name(String name) {
            mName = name;
            return this;
        }

        public Builder author(String author) {
            mAuthor = author;
            return this;
        }

        public Builder url(String url) {
            mUrl = url;
            return this;
        }

        public Builder thumbUrl(String thumbUrl) {
            mThumbUrl = thumbUrl;
            return this;
        }

        public Builder category(String category) {
            mCategory = category;
            return this;
        }

        public Builder addedOn(String addedOn) {
            mAddedOn = addedOn;
            return this;
        }

        public Builder favorite(boolean isFavorite) {
            mIsFavorite = isFavorite;
            return this;
        }

        public Builder dimensions(ImageSize dimensions) {
            mDimensions = dimensions;
            return this;
        }

        public Builder mimeType(String mimeType) {
            mMimeType = mimeType;
            return this;
        }

        public Builder size(int size) {
            mSize = size;
            return this;
        }

        public Builder color(int color) {
            mColor = color;
            return this;
        }

        public Wallpaper build() {
            Wallpaper wallpaper = new Wallpaper(mName, mAuthor, mUrl, mThumbUrl, mCategory, mAddedOn);
            wallpaper.setId(mId);
            wallpaper.setDimensions(mDimensions);
            wallpaper.setFavorite(mIsFavorite);
            wallpaper.setMimeType(mMimeType);
            wallpaper.setSize(mSize);
            wallpaper.setColor(mColor);
            return wallpaper;
        }
    }
}
