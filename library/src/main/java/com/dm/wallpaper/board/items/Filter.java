package com.dm.wallpaper.board.items;

import android.support.annotation.Nullable;

import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

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

public class Filter {

    private List<Options> mOptions;

    public Filter() {
        mOptions = new ArrayList<>();
    }

    public Filter add(Options options) {
        if (mOptions.contains(options)) {
            LogUtil.e("filter already contains options");
            return this;
        }
        mOptions.add(options);
        return this;
    }

    @Nullable
    public Options get(int index) {
        if (index < 0 || index > mOptions.size()) {
            LogUtil.e("filter: index out of bounds");
            return null;
        }
        return mOptions.get(index);
    }

    public int size() {
        return mOptions.size();
    }

    public static Options Create(Column column) {
        return new Options(column);
    }

    public static class Options {

        private Column mColumn;
        private String mQuery;

        private Options(Column column) {
            mColumn = column;
            mQuery = "";
        }

        public Options setQuery(String query) {
            mQuery = query;
            return this;
        }

        public Column getColumn() {
            return mColumn;
        }

        public String getQuery() {
            return mQuery;
        }

        @Override
        public boolean equals(Object object) {
            boolean equals = false;
            if (object != null && object instanceof Options) {
                equals = mColumn == ((Options) object).getColumn() &&
                        mQuery.equals(((Options) object).getQuery());
            }
            return equals;
        }
    }

    public enum Column {
        ID(Database.KEY_ID),
        NAME(Database.KEY_NAME),
        AUTHOR(Database.KEY_AUTHOR),
        CATEGORY(Database.KEY_CATEGORY);

        private String mName;

        Column(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }
}
