package com.dm.wallpaper.board.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.helpers.JsonHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.Filter;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.AlphanumComparator;
import com.dm.wallpaper.board.utils.LogUtil;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.Collections;
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
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_WALLPAPERS = "wallpapers";
    private static final String TABLE_CATEGORIES = "categories";

    private static final String KEY_ID = "id";
    private static final String KEY_URL = "url";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_SIZE = "size";
    private static final String KEY_COLOR = "color";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";

    public static final String KEY_NAME= "name";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_CATEGORY = "category";

    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_SELECTED = "selected";
    private static final String KEY_MUZEI_SELECTED = "muzeiSelected";
    private static final String KEY_ADDED_ON = "addedOn";

    private final Context mContext;

    private static Database mDatabase;
    private SQLiteDatabase mSQLiteDatabase;

    public static Database get(@NonNull Context context) {
        if (mDatabase == null) {
            mDatabase = new Database(context);
        }
        return mDatabase;
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
                KEY_MIME_TYPE + " TEXT, " +
                KEY_SIZE + " INTEGER DEFAULT 0, " +
                KEY_COLOR + " INTEGER DEFAULT 0, " +
                KEY_WIDTH + " INTEGER DEFAULT 0, " +
                KEY_HEIGHT + " INTEGER DEFAULT 0, " +
                KEY_CATEGORY + " TEXT NOT NULL," +
                KEY_FAVORITE + " INTEGER DEFAULT 0," +
                KEY_ADDED_ON + " TEXT NOT NULL, " +
                "UNIQUE (" +KEY_URL+ "))";
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_WALLPAPER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         * Need to clear shared preferences with version 2.0.0b-1
         */
        if (newVersion == 4) {
            Preferences.get(mContext).clearPreferences();
        }
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

    public boolean openDatabase() {
        try {
            if (mDatabase == null) {
                LogUtil.e("Database error: openDatabase() database instance is null");
                return false;
            }

            if (mDatabase.mSQLiteDatabase == null) {
                mDatabase.mSQLiteDatabase = mDatabase.getWritableDatabase();
            }

            if (!mDatabase.mSQLiteDatabase.isOpen()) {
                LogUtil.e("Database error: database openable false, trying to open the database again");
                mDatabase.mSQLiteDatabase = mDatabase.getWritableDatabase();
            }
            return mDatabase.mSQLiteDatabase.isOpen();
        } catch (SQLiteException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean closeDatabase() {
        try {
            if (mDatabase == null) {
                LogUtil.e("Database error: closeDatabase() database instance is null");
                return false;
            }

            if (mDatabase.mSQLiteDatabase == null) {
                LogUtil.e("Database error: trying to close database which is not opened");
                return false;
            }
            mDatabase.mSQLiteDatabase.close();
            return true;
        } catch (SQLiteException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return false;
        }
    }

    public void addCategories(List<?> list) {
        if (!openDatabase()) {
            LogUtil.e("Database error: addCategories() failed to open database");
            return;
        }

        String query = "INSERT OR IGNORE INTO " +TABLE_CATEGORIES+ " (" +KEY_NAME+ ") VALUES (?);";
        SQLiteStatement statement = mDatabase.mSQLiteDatabase.compileStatement(query);
        mDatabase.mSQLiteDatabase.beginTransaction();

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
        mDatabase.mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.mSQLiteDatabase.endTransaction();
    }

    public void addWallpapers(@NonNull List<?> list) {
        if (!openDatabase()) {
            LogUtil.e("Database error: addWallpapers() failed to open database");
            return;
        }

        String query = "INSERT OR IGNORE INTO " +TABLE_WALLPAPERS+ " (" +KEY_NAME+ "," +KEY_AUTHOR+ "," +KEY_URL+ ","
                +KEY_THUMB_URL+ "," +KEY_CATEGORY+ "," +KEY_ADDED_ON+ ") VALUES (?,?,?,?,?,?);";
        SQLiteStatement statement = mDatabase.mSQLiteDatabase.compileStatement(query);
        mDatabase.mSQLiteDatabase.beginTransaction();

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
        mDatabase.mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.mSQLiteDatabase.endTransaction();
    }

    public void updateWallpaper(Wallpaper wallpaper) {
        if (!openDatabase()) {
            LogUtil.e("Database error: updateWallpaper() failed to open database");
            return;
        }

        if (wallpaper == null) return;

        ContentValues values = new ContentValues();
        if (wallpaper.getSize() > 0) {
            values.put(KEY_SIZE, wallpaper.getSize());
        }

        if (wallpaper.getMimeType() != null) {
            values.put(KEY_MIME_TYPE, wallpaper.getMimeType());
        }

        if (wallpaper.getDimensions() != null) {
            values.put(KEY_WIDTH, wallpaper.getDimensions().getWidth());
            values.put(KEY_HEIGHT, wallpaper.getDimensions().getHeight());
        }

        if (wallpaper.getColor() != 0) {
            values.put(KEY_COLOR, wallpaper.getColor());
        }

        if (values.size() > 0) {
            mDatabase.mSQLiteDatabase.update(TABLE_WALLPAPERS,
                    values, KEY_ID +" = ?", new String[]{String.valueOf(wallpaper.getId())});
        }
    }

    public void selectCategory(int id, boolean isSelected) {
        if (!openDatabase()) {
            LogUtil.e("Database error: selectCategory() failed to open database");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_SELECTED, isSelected ? 1 : 0);
        mDatabase.mSQLiteDatabase.update(TABLE_CATEGORIES, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
    }

    public void selectCategoryForMuzei(int id, boolean isSelected) {
        if (!openDatabase()) {
            LogUtil.e("Database error: selectCategoryForMuzei() failed to open database");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_MUZEI_SELECTED, isSelected ? 1 : 0);
        mDatabase.mSQLiteDatabase.update(TABLE_CATEGORIES, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
    }

    public void favoriteWallpaper(int id, boolean isFavorite) {
        if (!openDatabase()) {
            LogUtil.e("Database error: favoriteWallpaper() failed to open database");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_FAVORITE, isFavorite ? 1 : 0);
        mDatabase.mSQLiteDatabase.update(TABLE_WALLPAPERS, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
    }

    private List<String> getSelectedCategories(boolean isMuzei) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getSelectedCategories() failed to open database");
            return new ArrayList<>();
        }

        List<String> categories = new ArrayList<>();
        String column = isMuzei ? KEY_MUZEI_SELECTED : KEY_SELECTED;
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_CATEGORIES, new String[]{KEY_NAME}, column +" = ?",
                new String[]{"1"}, null, null, KEY_NAME);
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public List<Category> getCategories() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getCategories() failed to open database");
            return new ArrayList<>();
        }

        List<Category> categories = new ArrayList<>();
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_CATEGORIES,
                null, null, null, null, null, KEY_NAME);
        if (cursor.moveToFirst()) {
            do {
                Category category = Category.Builder()
                        .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .selected(cursor.getInt(cursor.getColumnIndex(KEY_SELECTED)) == 1)
                        .muzeiSelected(cursor.getInt(cursor.getColumnIndex(KEY_MUZEI_SELECTED)) == 1)
                        .build();
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (Category category : categories) {
            String name = category.getName().toLowerCase(Locale.getDefault());
            String query = "SELECT wallpapers.thumbUrl, wallpapers.color, " +
                    "(SELECT COUNT(*) FROM wallpapers WHERE LOWER(wallpapers.category) LIKE ?) AS count " +
                    "FROM wallpapers WHERE LOWER(wallpapers.category) LIKE ? ORDER BY RANDOM() LIMIT 1";
            cursor = mDatabase.mSQLiteDatabase.rawQuery(query, new String[]{"%" +name+ "%", "%" +name+ "%"});
            if (cursor.moveToFirst()) {
                do {
                    category.setColor(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)));
                    category.setThumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)));
                    category.setCount(cursor.getInt(cursor.getColumnIndex("count")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return categories;
    }

    public List<Category> getWallpaperCategories(String category) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpaperCategories() failed to open database");
            return new ArrayList<>();
        }

        List<Category> categories = new ArrayList<>();
        String[] strings = category.split(",|;");
        for (String string : strings) {
            String s = string.toLowerCase(Locale.getDefault());
            String query = "SELECT categories.id, categories.name, " +
                    "(SELECT wallpapers.thumbUrl FROM wallpapers WHERE LOWER(wallpapers.category) LIKE ? ORDER BY RANDOM() LIMIT 1) AS thumbUrl " +
                    "FROM categories WHERE LOWER(categories.name) = ? LIMIT 1";
            Cursor cursor = mDatabase.mSQLiteDatabase.rawQuery(query, new String[]{"%" +s+ "%", s});
            if (cursor.moveToFirst()) {
                do {
                    Category c = Category.Builder()
                            .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                            .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                            .thumbUrl(cursor.getString(2))
                            .build();
                    categories.add(c);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return categories;
    }

    public int getCategoryCount(String category) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getCategoryCount() failed to open database");
            return 0;
        }

        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS, null, "LOWER(" +KEY_CATEGORY+ ") LIKE ?",
                new String[]{"%" +category.toLowerCase(Locale.getDefault())+ "%"}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Nullable
    public Wallpaper getWallpaper(int id) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpaper() failed to open database");
            return null;
        }

        Wallpaper wallpaper = null;
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, KEY_ID +" = ?", new String[]{String.valueOf(id)}, null, null, null, "1");
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width  > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                wallpaper = Wallpaper.Builder()
                        .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .category(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)))
                        .favorite(cursor.getInt(cursor.getColumnIndex(KEY_FAVORITE)) == 1)
                        .dimensions(dimensions)
                        .mimeType(cursor.getString(cursor.getColumnIndex(KEY_MIME_TYPE)))
                        .size(cursor.getInt(cursor.getColumnIndex(KEY_SIZE)))
                        .color(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)))
                        .build();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpaper;
    }

    public List<Wallpaper> getFilteredWallpapers(Filter filter) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getFilteredWallpapers() failed to open database");
            return new ArrayList<>();
        }

        List<Wallpaper> wallpapers = new ArrayList<>();

        StringBuilder condition = new StringBuilder();
        List<String> selection = new ArrayList<>();
        for (int i = 0; i < filter.size(); i++) {
            Filter.Options options = filter.get(i);
            if (options != null) {
                if (condition.length() > 0 ) {
                    condition.append(" OR ").append("LOWER(")
                            .append(options.getColumn().getName())
                            .append(")").append(" LIKE ?");
                } else {
                    condition.append("LOWER(")
                            .append(options.getColumn().getName()).append(")")
                            .append(" LIKE ?");
                }
                selection.add("%" +options.getQuery().toLowerCase(Locale.getDefault())+ "%");
            }
        }

        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS, null, condition.toString(),
                selection.toArray(new String[selection.size()]),
                null, null, KEY_NAME);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = Wallpaper.Builder()
                        .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .category(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)))
                        .favorite(cursor.getInt(cursor.getColumnIndex(KEY_FAVORITE)) == 1)
                        .color(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)))
                        .build();
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();

        Collections.sort(wallpapers, new AlphanumComparator() {

            @Override
            public int compare(Object o1, Object o2) {
                String s1 = ((Wallpaper) o1).getName();
                String s2 = ((Wallpaper) o2).getName();
                return super.compare(s1, s2);
            }
        });
        return wallpapers;
    }

    public List<Wallpaper> getWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpapers() failed to open database");
            return new ArrayList<>();
        }

        List<Wallpaper> wallpapers = new ArrayList<>();
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, null, null, null, null, KEY_NAME);
        if (cursor.moveToFirst()) {
            do {
                int colorIndex = cursor.getColumnIndex(KEY_COLOR);

                Wallpaper.Builder builder = Wallpaper.Builder()
                        .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .category(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)))
                        .favorite(cursor.getInt(cursor.getColumnIndex(KEY_FAVORITE)) == 1);
                if (colorIndex > -1) {
                    builder.color(cursor.getInt(colorIndex));
                }
                wallpapers.add(builder.build());
            } while (cursor.moveToNext());
        }
        cursor.close();

        Collections.sort(wallpapers, new AlphanumComparator() {

            @Override
            public int compare(Object o1, Object o2) {
                String s1 = ((Wallpaper) o1).getName();
                String s2 = ((Wallpaper) o2).getName();
                return super.compare(s1, s2);
            }
        });
        return wallpapers;
    }

    public List<Wallpaper> getLatestWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getLatestWallpapers() failed to open database");
            return new ArrayList<>();
        }

        List<Wallpaper> wallpapers = new ArrayList<>();
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS, null, null, null, null, null,
                KEY_ADDED_ON+ " DESC, " +KEY_ID,
                String.valueOf(WallpaperBoardApplication.getConfiguration().getLatestWallpapersDisplayMax()));
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width  > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                Wallpaper wallpaper = Wallpaper.Builder()
                        .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .category(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)))
                        .favorite(cursor.getInt(cursor.getColumnIndex(KEY_FAVORITE)) == 1)
                        .dimensions(dimensions)
                        .mimeType(cursor.getString(cursor.getColumnIndex(KEY_MIME_TYPE)))
                        .size(cursor.getInt(cursor.getColumnIndex(KEY_SIZE)))
                        .color(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)))
                        .build();
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpapers;
    }

    @Nullable
    public Wallpaper getRandomWallpaper() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getRandomWallpaper() failed to open database");
            return null;
        }

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

        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS, null, CONDITION.toString(),
                selection.toArray(new String[selection.size()]), null, null, "RANDOM()", "1");
        if (cursor.moveToFirst()) {
            do {
                wallpaper = Wallpaper.Builder()
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .category(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)))
                        .build();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpaper;
    }

    public int getWallpapersCount() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpapersCount() failed to open database");
            return 0;
        }

        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS, null, null, null, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        return rowCount;
    }

    public List<Wallpaper> getFavoriteWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getFavoriteWallpapers() failed to open database");
            return new ArrayList<>();
        }

        List<Wallpaper> wallpapers = new ArrayList<>();
        Cursor cursor = mDatabase.mSQLiteDatabase.query(TABLE_WALLPAPERS, null, KEY_FAVORITE +" = ?",
                new String[]{"1"}, null, null, KEY_ADDED_ON+ " DESC, " +KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = Wallpaper.Builder()
                        .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .category(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)))
                        .favorite(cursor.getInt(cursor.getColumnIndex(KEY_FAVORITE)) == 1)
                        .build();
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();

        Collections.sort(wallpapers, new AlphanumComparator() {

            @Override
            public int compare(Object o1, Object o2) {
                String s1 = ((Wallpaper) o1).getName();
                String s2 = ((Wallpaper) o2).getName();
                return super.compare(s1, s2);
            }
        });
        return wallpapers;
    }

    public void deleteWallpapers(@NonNull List<Wallpaper> wallpapers) {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteWallpapers() failed to open database");
            return;
        }

        for (int i = 0; i < wallpapers.size(); i++) {
            mDatabase.mSQLiteDatabase.delete(TABLE_WALLPAPERS, KEY_URL +" = ?",
                    new String[]{wallpapers.get(i).getUrl()});
        }
    }

    public void deleteWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteWallpapers() failed to open database");
            return;
        }

        mSQLiteDatabase.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_WALLPAPERS});
        mSQLiteDatabase.delete(TABLE_WALLPAPERS, null, null);
    }

    public void deleteCategories() {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteCategories() failed to open database");
            return;
        }

        mDatabase.mSQLiteDatabase.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_CATEGORIES});
        mDatabase.mSQLiteDatabase.delete(TABLE_CATEGORIES, null, null);
    }
}
