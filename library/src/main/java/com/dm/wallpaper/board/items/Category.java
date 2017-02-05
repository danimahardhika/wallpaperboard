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

public class Category {

    private final int mId;
    private final String mName;
    private final String mThumbUrl;
    private boolean mIsSelected;
    private boolean mIsMuzeiSelected;

    public Category(int id, String name, String thumbUrl, boolean isSelected, boolean isMuzeiSelected) {
        mId = id;
        mName = name;
        mThumbUrl = thumbUrl;
        mIsSelected = isSelected;
        mIsMuzeiSelected = isMuzeiSelected;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public boolean isMuzeiSelected() {
        return mIsMuzeiSelected;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public void setMuzeiSelected(boolean isMuzeiSelected) {
        mIsMuzeiSelected = isMuzeiSelected;
    }
}
