package com.dm.wallpaper.board;

import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.Wallpaper;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
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

public class WallpaperTest {

    @Test
    public void testWallpapersLoading() {
        int categorySize = 1456;
        int wallpaperSize = 123647;

        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < categorySize; i++) {
            Category category = Category.Builder()
                    .name("Category " +i)
                    .build();
            categories.add(category);
        }

        List<Wallpaper> wallpapers = new ArrayList<>();
        for (int i = 0; i < wallpaperSize; i++) {
            Wallpaper wallpaper = Wallpaper.Builder()
                    .name("Wallpaper " +i)
                    .url("http://www.url.com/image.jpg")
                    .thumbUrl("http://www.url.com/image.jpg")
                    .author("Unknown")
                    .addedOn(TimeHelper.getLongDateTime())
                    .build();
            wallpapers.add(wallpaper);
        }

        //Estimating for loop process time
        long forLopStart = System.currentTimeMillis();
        for (Category category : categories) {
            String name = category.getName();
        }

        for (Wallpaper wallpaper : wallpapers) {
            String name = wallpaper.getName();
        }
        long forLoopEnd = (System.currentTimeMillis() - forLopStart);

        //Estimating do while process time
        long doWhileStart = System.currentTimeMillis();
        Iterator categoryIterator = categories.iterator();
        Iterator wallpaperIterator = wallpapers.iterator();

        int size = categorySize > wallpaperSize ? categorySize : wallpaperSize;
        int i = 0;
        do {
            if (categoryIterator.hasNext()) {
                Object object = categoryIterator.next();
            }

            if (wallpaperIterator.hasNext()) {
                Object object = wallpaperIterator.next();
            }
            i++;
        } while (i < size);
        long doWhileEnd = (System.currentTimeMillis() - doWhileStart);

        System.out.println("forLoopTime: " +forLoopEnd);
        System.out.println("doWhileTime: " +doWhileEnd);
        Assert.assertTrue(doWhileEnd < forLoopEnd);
    }

    @Test
    public void testIsWallpaperEquals() {
        Wallpaper wallpaper = Wallpaper.Builder()
                .name("Wallpaper test")
                .url("http://www.url.com/image.jpg")
                .thumbUrl("http://www.url.com/image.jpg")
                .author("Unknown")
                .category("Category")
                .addedOn(TimeHelper.getLongDateTime())
                .build();

        Wallpaper wallpaper1 = Wallpaper.Builder()
                .name("Wallpaper test")
                .url("http://www.url.com/image.jpg")
                .thumbUrl("http://www.url.com/image.jpg")
                .author("Unknown")
                .category("Category 1")
                .addedOn(TimeHelper.getLongDateTime())
                .build();
        Assert.assertFalse(wallpaper.equals(wallpaper1));
    }

    @Test
    public void testIsCategoryEquals() {
        Category category = Category.Builder()
                .name("Category test")
                .build();

        Category category1 = Category.Builder()
                .name("Category test 1")
                .build();
        Assert.assertFalse(category.equals(category1));
    }

    @Test
    public void testCategoryCount() {
        Category category = Category.Builder()
                .name("Category test")
                .count(125)
                .build();

        String c1 = category.getCategoryCount();

        category.setCount(1245);
        String c2 = category.getCategoryCount();

        category.setCount(6457);
        String c3 = category.getCategoryCount();

        category.setCount(12457);
        String c4 = category.getCategoryCount();

        Assert.assertTrue(c1.equals("99+") &&
                c2.equals("1K+") &&
                c3.equals("6K+") &&
                c4.equals("9K+"));
    }
}
