package com.dm.wallpaper.board.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.wallpaper.board.helpers.JsonHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wallpaper_board_database";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_WALLPAPERS = "wallpapers";
    private static final String TABLE_CATEGORIES = "categories";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME= "name";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_URL = "url";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_SELECTED = "selected";
    private static final String KEY_MUZEI_SELECTED = "muzeiSelected";
    private static final String KEY_ADDED_ON = "addedOn";

    private final Context mContext;

    @NonNull
    public static Database get(@NonNull Context context) {
        return new Database(context);
    }

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_CATEGORY = "CREATE TABLE " +TABLE_CATEGORIES+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                KEY_NAME + " TEXT NOT NULL," +
                KEY_SELECTED + " INTEGER DEFAULT 1," +
                KEY_MUZEI_SELECTED + " INTEGER DEFAULT 1, " +
                "UNIQUE (" +KEY_NAME+ "))";
        String CREATE_TABLE_WALLPAPER = "CREATE TABLE IF NOT EXISTS " +TABLE_WALLPAPERS+ "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME+ " TEXT NOT NULL, " +
                KEY_AUTHOR + " TEXT, " +
                KEY_URL + " TEXT NOT NULL, " +
                KEY_THUMB_URL + " TEXT NOT NULL, " +
                KEY_CATEGORY + " TEXT NOT NULL," +
                KEY_FAVORITE + " INTEGER DEFAULT 0," +
                KEY_ADDED_ON + " TEXT NOT NULL, " +
                "UNIQUE (" +KEY_URL+ "))";
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_WALLPAPER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    private void resetDatabase(SQLiteDatabase db) {
        Preferences.get(mContext).setAutoIncrement(0);
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type=\'table\'", null);
        SparseArrayCompat<String> tables = new SparseArrayCompat<>();
        if (cursor.moveToFirst()) {
            do {
                tables.append(tables.size(), cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (int i = 0; i < tables.size(); i++) {
            try {
                String dropQuery = "DROP TABLE IF EXISTS " + tables.get(i);
                if (!tables.get(i).equalsIgnoreCase("SQLITE_SEQUENCE"))
                    db.execSQL(dropQuery);
            } catch (Exception ignored) {}
        }
        onCreate(db);
    }

    public void close() {
        if (this.getWritableDatabase().isOpen()) {
            try {
                this.getWritableDatabase().close();
                LogUtil.e("database forced to close");
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        }
    }

    public void addCategories(List<?> list) {
        String query = "INSERT OR IGNORE INTO " +TABLE_CATEGORIES+ " (" +KEY_NAME+ ") VALUES (?);";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(query);
        db.beginTransaction();

        for (int i = 0; i < list.size(); i++) {
            statement.clearBindings();

            Category category;
            if (list.get(i) instanceof Category) {
                category = (Category) list.get(i);
            } else {
                category = JsonHelper.getCategory(list.get(i));
            }

            if (category != null) {
                statement.bindString(1, category.getName());
                statement.execute();
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void addWallpapers(@NonNull List<?> list) {
        String query = "INSERT OR IGNORE INTO " +TABLE_WALLPAPERS+ " (" +KEY_NAME+ "," +KEY_AUTHOR+ "," +KEY_URL+ ","
                +KEY_THUMB_URL+ "," +KEY_CATEGORY+ "," +KEY_ADDED_ON+ ") VALUES (?,?,?,?,?,?);";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(query);
        db.beginTransaction();

        for (int i = 0; i < list.size(); i++) {
            statement.clearBindings();

            Wallpaper wallpaper;
            if (list.get(i) instanceof Wallpaper) {
                wallpaper = (Wallpaper) list.get(i);
            } else {
                wallpaper = JsonHelper.getWallpaper(list.get(i));
            }

            if (wallpaper != null) {
                if (wallpaper.getUrl() != null) {
                    String name = JsonHelper.getGeneratedName(mContext, wallpaper.getName());

                    statement.bindString(1, name);

                    if (wallpaper.getAuthor() != null) {
                        statement.bindString(2, wallpaper.getAuthor());
                    } else {
                        statement.bindNull(2);
                    }

                    statement.bindString(3, wallpaper.getUrl());
                    statement.bindString(4, wallpaper.getThumbUrl());
                    statement.bindString(5, wallpaper.getCategory());
                    statement.bindString(6, TimeHelper.getLongDateTime());
                    statement.execute();
                }
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void selectCategory(int id, boolean isSelected) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SELECTED, isSelected ? 1 : 0);
        db.update(TABLE_CATEGORIES, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void selectCategoryForMuzei(int id, boolean isSelected) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MUZEI_SELECTED, isSelected ? 1 : 0);
        db.update(TABLE_CATEGORIES, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void favoriteWallpaper(int id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_WALLPAPERS, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    private List<String> getSelectedCategories(boolean isMuzei) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String column = isMuzei ? KEY_MUZEI_SELECTED : KEY_SELECTED;
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{KEY_NAME}, column +" = ?",
                new String[]{"1"}, null, null, KEY_NAME);
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, KEY_NAME);
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2) == 1,
                        cursor.getInt(3) == 1,
                        0);
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public int getCategoryCount(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, "LOWER(" +KEY_CATEGORY+ ") LIKE ?",
                new String[]{"%" +category.toLowerCase(Locale.getDefault())+ "%"}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public List<Wallpaper> getFilteredWallpapers() {
        List<Wallpaper> wallpapers = new ArrayList<>();
        List<String> selected = getSelectedCategories(false);
        List<String> selection = new ArrayList<>();
        if (selected.size() == 0) return wallpapers;

        StringBuilder CONDITION = new StringBuilder();
        for (String item : selected) {
            if (CONDITION.length() > 0 ) {
                CONDITION.append(" OR ").append("LOWER(").append(KEY_CATEGORY).append(")").append(" LIKE ?");
            } else {
                CONDITION.append("LOWER(").append(KEY_CATEGORY).append(")").append(" LIKE ?");
            }
            selection.add("%" +item.toLowerCase(Locale.getDefault())+ "%");
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, CONDITION.toString(),
                selection.toArray(new String[selection.size()]),
                null, null, KEY_ADDED_ON+ " DESC, " +KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = new Wallpaper(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(6) == 1);
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpapers;
    }

    public List<Wallpaper> getWallpapers() {
        List<Wallpaper> wallpapers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, null, null, null, null,
                KEY_ADDED_ON+ " DESC, " +KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = new Wallpaper(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5));
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpapers;
    }

    @Nullable
    public Wallpaper getRandomWallpaper() {
        Wallpaper wallpaper = null;
        List<String> selected = getSelectedCategories(true);
        List<String> selection = new ArrayList<>();
        if (selected.size() == 0) return null;

        StringBuilder CONDITION = new StringBuilder();
        for (String item : selected) {
            if (CONDITION.length() > 0 ) {
                CONDITION.append(" OR ").append("LOWER(").append(KEY_CATEGORY).append(")").append(" LIKE ?");
            } else {
                CONDITION.append("LOWER(").append(KEY_CATEGORY).append(")").append(" LIKE ?");
            }
            selection.add("%" +item.toLowerCase(Locale.getDefault())+ "%");
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, CONDITION.toString(),
                    selection.toArray(new String[selection.size()]), null, null, "RANDOM()", "1");
        if (cursor.moveToFirst()) {
            do {
                wallpaper = new Wallpaper(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpaper;
    }

    public int getWallpapersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, null, null, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        db.close();
        return rowCount;
    }

    public List<Wallpaper> getFavoriteWallpapers() {
        List<Wallpaper> wallpapers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WALLPAPERS, null, KEY_FAVORITE +" = ?",
                new String[]{"1"}, null, null, KEY_ADDED_ON+ " DESC, " +KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = new Wallpaper(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(6) == 1);
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return wallpapers;
    }

    public void deleteWallpapers(@NonNull List<Wallpaper> wallpapers) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < wallpapers.size(); i++) {
            db.delete(TABLE_WALLPAPERS, KEY_URL +" = ?",
                    new String[]{wallpapers.get(i).getUrl()});
        }
        db.close();
    }

    public void deleteWallpapers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_WALLPAPERS});
        db.delete(TABLE_WALLPAPERS, null, null);
        db.close();
    }

    public void deleteCategories() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_CATEGORIES});
        db.delete(TABLE_CATEGORIES, null, null);
        db.close();
    }
}
