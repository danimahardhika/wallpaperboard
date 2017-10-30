package com.dm.wallpaper.board.items;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

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

public class Collection {

    private final int mIcon;
    private final Fragment mFragment;
    private final String mTag;

    public Collection(@DrawableRes int icon, @NonNull Fragment fragment, @NonNull String tag) {
        mIcon = icon;
        mFragment = fragment;
        mTag = tag;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    @NonNull
    public Fragment getFragment() {
        return mFragment;
    }

    public String getTag() {
        return mTag;
    }
}
