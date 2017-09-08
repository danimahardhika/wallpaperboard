package com.dm.wallpaper.board.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.ListHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarDuration;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.JsonHelper;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.utils.JsonStructure;
import com.dm.wallpaper.board.utils.LogUtil;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

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

public class WallpapersLoaderTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;

    private WallpapersLoaderTask(Context context) {
        mContext = context;
    }

    public static AsyncTask start(@NonNull Context context) {
        return start(context, SERIAL_EXECUTOR);
    }

    public static AsyncTask start(@NonNull Context context, @NonNull Executor executor) {
        return new WallpapersLoaderTask(context).executeOnExecutor(executor);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                Thread.sleep(1);
                String wallpaperUrl = WallpaperBoardApplication.getConfiguration().getJsonStructure().getUrl();
                if (wallpaperUrl == null) {
                    wallpaperUrl = mContext.getResources().getString(R.string.wallpaper_json);
                }

                URL url = new URL(wallpaperUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);

                if (WallpaperBoardApplication.getConfiguration().getJsonStructure().getUrl() != null) {
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);

                    List<NameValuePair> values = WallpaperBoardApplication.getConfiguration()
                            .getJsonStructure().getPosts();
                    if (values.size() > 0) {
                        DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                        stream.writeBytes(JsonHelper.getQuery(values));
                        stream.flush();
                        stream.close();
                    }
                }

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream stream = connection.getInputStream();
                    JsonStructure.CategoryStructure categoryStructure = WallpaperBoardApplication
                            .getConfiguration().getJsonStructure().getCategory();
                    JsonStructure.WallpaperStructure wallpaperStructure = WallpaperBoardApplication
                            .getConfiguration().getJsonStructure().getWallpaper();

                    Map<String, List> map = LoganSquare.parseMap(stream, List.class);
                    if (map == null) return false;

                    stream.close();
                    List categoryList = map.get(categoryStructure.getArrayName());
                    if (categoryList == null) {
                        LogUtil.e("Json error: category array with name "
                                +categoryStructure.getArrayName() +" not found");
                        return false;
                    }

                    if (categoryList.size() == 0) {
                        LogUtil.e("Json error: make sure to add category correctly");
                        return false;
                    }

                    List wallpaperList = map.get(wallpaperStructure.getArrayName());
                    if (wallpaperList == null) {
                        LogUtil.e("Json error: wallpaper array with name "
                                +wallpaperStructure.getArrayName() +" not found");
                        return false;
                    }

                    List<Wallpaper> wallpapers;
                    if (Database.get(mContext).getWallpapersCount() > 0) {
                        List<Category> categories = Database.get(mContext).getCategories();
                        List<Category> newCategories = new ArrayList<>();
                        for (int i = 0; i < categoryList.size(); i++) {
                            Category category = JsonHelper.getCategory(categoryList.get(i));
                            if (category != null) {
                                newCategories.add(category);
                            }
                        }

                        //Same categories
                        List<Category> intersectionC = (List<Category>)
                                ListHelper.intersect(newCategories, categories);

                        //Deleted categories
                        List<Category> differenceC = (List<Category>)
                                ListHelper.difference(intersectionC, categories);

                        //New categories
                        List<Category> newlyAddedC = (List<Category>) ListHelper.difference(
                                intersectionC, newCategories);

                        Database.get(mContext).deleteCategories(differenceC);
                        Database.get(mContext).addCategories(newlyAddedC);

                        wallpapers = Database.get(mContext).getWallpapers();
                        List<Wallpaper> newWallpapers = new ArrayList<>();
                        for (int i = 0; i < wallpaperList.size(); i++) {
                            Wallpaper wallpaper = JsonHelper.getWallpaper(wallpaperList.get(i));
                            if (wallpaper != null) {
                                newWallpapers.add(wallpaper);
                            }
                        }

                        //A: Wallpapers in json that also available in database
                        //Considered as same wallpapers
                        List<Wallpaper> intersectionW = (List<Wallpaper>)
                                ListHelper.intersect(wallpapers, newWallpapers);

                        //C: Wallpapers in json that not available in A
                        //Considered as new wallpapers
                        List<Wallpaper> newlyAddedW = (List<Wallpaper>)
                                ListHelper.difference(intersectionW, newWallpapers);

                        //No new wallpapers, immediately returns true
                        if (newlyAddedW.size() == 0) {
                            LogUtil.d("Task from " +mContext.getPackageName() +": no new wallpapers");
                            return true;
                        }

                        //B: Wallpapers in database that not available in A
                        //Considered as different wallpaper
                        List<Wallpaper> differenceW = (List<Wallpaper>)
                                ListHelper.difference(intersectionW, wallpapers);

                        List<Wallpaper> favorites = Database.get(mContext).getFavoriteWallpapers();

                        Database.get(mContext).deleteWallpapers(differenceW);

                        if (intersectionW.size() == 0) {
                            Database.get(mContext).resetAutoIncrement();
                        }

                        Database.get(mContext).addWallpapers(newlyAddedW);

                        if (differenceW.size() > 0) {
                            Database.get(mContext).updateWallpapers(favorites);
                        }
                        return true;
                    }

                    Database.get(mContext).addCategories(categoryList);
                    Database.get(mContext).addWallpapers(wallpaperList);

                    Database.get(mContext).restoreFavorites();
                    return true;
                }
                return false;
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (mContext == null) return;
        if (((AppCompatActivity) mContext).isFinishing()) return;

        if (!aBoolean) {
            int res = R.string.connection_failed;
            if (Database.get(mContext).getWallpapersCount() > 0) {
                res = R.string.wallpapers_loader_failed;
            }
            CafeBar.builder(mContext)
                    .theme(new CafeBarTheme.Custom(ColorHelper.getAttributeColor(
                            mContext, R.attr.card_background)))
                    .contentTypeface(TypefaceHelper.getRegular(mContext))
                    .content(res)
                    .fitSystemWindow()
                    .duration(CafeBarDuration.MEDIUM.getDuration())
                    .show();
        }
    }
}
