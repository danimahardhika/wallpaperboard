package com.dm.wallpaper.board;

import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.Wallpaper;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author: Dani Mahardhika
 * Created on: 11/25/2017
 * https://github.com/danimahardhika
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

        //Estimating for time process time
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
}
