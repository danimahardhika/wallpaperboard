package com.dm.wallpaper.board.items;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

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

@JsonObject
public class WallpaperJson {

    @JsonField(name = "name")
    public String name;

    @JsonField(name = "author")
    public String author;

    @JsonField(name = "url")
    public String url;

    @JsonField(name = "thumbUrl")
    public String thumbUrl;

    @JsonField(name = "category")
    public String category;

    @JsonField(name = "Wallpapers")
    public List<WallpaperJson> getWallpapers;

    @JsonField(name = "Categories")
    public List<WallpaperJson> getCategories;

}
