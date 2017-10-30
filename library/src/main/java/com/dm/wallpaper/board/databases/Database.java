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
import android.util.Log;

import com.danimahardhika.android.helpers.core.TimeHelper;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.helpers.JsonHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.Filter;
import com.dm.wallpaper.board.items.PopupItem;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.AlphanumComparator;
import com.dm.wallpaper.board.utils.LogUtil;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.lang.ref.WeakReference;
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
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_WALLPAPERS = "wallpapers";
    private static final String TABLE_CATEGORIES = "categories";

    private static final String KEY_URL = "url";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_SIZE = "size";
    private static final String KEY_COLOR = "color";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_COUNT = "count";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_CATEGORY = "category";

    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_SELECTED = "selected";
    private static final String KEY_MUZEI_SELECTED = "muzeiSelected";
    private static final String KEY_ADDED_ON = "addedOn";

    private final Context mContext;

    private static WeakReference<Database> mDatabase;
    private SQLiteDatabase mSQLiteDatabase;
    private static List<String> mFavoriteUrlsBackup;

    public static Database get(@NonNull Context context) {
        if (mDatabase == null || mDatabase.get() == null) {
            mDatabase = new WeakReference<>(new Database(context));
        }
        return mDatabase.get();
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
         * Need to clear shared preferences with version 1.5.0b-3
         */
        if (newVersion == 5) {
            Preferences.get(mContext).clearPreferences();
        }
        resetDatabase(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db);
    }

    private void resetDatabase(SQLiteDatabase db) {
        List<String> tables = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type=\'table\'", null);
        if (cursor.moveToFirst()) {
            do {
                tables.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        mFavoriteUrlsBackup = new ArrayList<>();
        cursor = db.query(TABLE_WALLPAPERS, new String[]{KEY_URL}, KEY_FAVORITE +" = ?",
                new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                mFavoriteUrlsBackup.add(cursor.getString(0));
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

    public void restoreFavorites() {
        if (mFavoriteUrlsBackup == null) return;

        for (String url : mFavoriteUrlsBackup) {
            ContentValues values = new ContentValues();
            values.put(KEY_FAVORITE, 1);
            mDatabase.get().mSQLiteDatabase.update(TABLE_WALLPAPERS,
                    values, KEY_URL +" = ?", new String[]{url});
        }

        mFavoriteUrlsBackup.clear();
        mFavoriteUrlsBackup = null;
    }

    public boolean openDatabase() {
        try {
            if (mDatabase == null || mDatabase.get() == null) {
                LogUtil.e("Database error: openDatabase() database instance is null");
                return false;
            }

            if (mDatabase.get().mSQLiteDatabase == null) {
                mDatabase.get().mSQLiteDatabase = mDatabase.get().getWritableDatabase();
            }

            if (!mDatabase.get().mSQLiteDatabase.isOpen()) {
                LogUtil.e("Database error: database openable false, trying to open the database again");
                mDatabase.get().mSQLiteDatabase = mDatabase.get().getWritableDatabase();
            }
            return mDatabase.get().mSQLiteDatabase.isOpen();
        } catch (SQLiteException | NullPointerException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean closeDatabase() {
        try {
            if (mDatabase == null || mDatabase.get() == null) {
                LogUtil.e("Database error: closeDatabase() database instance is null");
                return false;
            }

            if (mDatabase.get().mSQLiteDatabase == null) {
                LogUtil.e("Database error: trying to close database which is not opened");
                return false;
            }
            mDatabase.get().mSQLiteDatabase.close();
            return true;
        } catch (SQLiteException | NullPointerException e) {
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
        SQLiteStatement statement = mDatabase.get().mSQLiteDatabase.compileStatement(query);
        mDatabase.get().mSQLiteDatabase.beginTransaction();

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
        mDatabase.get().mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.get().mSQLiteDatabase.endTransaction();
    }

    public void addWallpapers(@NonNull List<?> list) {
        if (!openDatabase()) {
            LogUtil.e("Database error: addWallpapers() failed to open database");
            return;
        }

        String query = "INSERT OR IGNORE INTO " +TABLE_WALLPAPERS+ " (" +KEY_NAME+ "," +KEY_AUTHOR+ "," +KEY_URL+ ","
                +KEY_THUMB_URL+ "," +KEY_CATEGORY+ "," +KEY_ADDED_ON+ ") VALUES (?,?,?,?,?,?);";
        SQLiteStatement statement = mDatabase.get().mSQLiteDatabase.compileStatement(query);
        mDatabase.get().mSQLiteDatabase.beginTransaction();

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
                    String name = wallpaper.getName();
                    if (name == null) name = "";

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
        mDatabase.get().mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.get().mSQLiteDatabase.endTransaction();
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
            mDatabase.get().mSQLiteDatabase.update(TABLE_WALLPAPERS,
                    values, KEY_URL +" = ?", new String[]{wallpaper.getUrl()});
        }
    }

    public void updateWallpapers(@NonNull List<Wallpaper> wallpapers) {
        if (!openDatabase()) {
            LogUtil.e("Database error: updateWallpapers() failed to open database");
            return;
        }

        String query = "UPDATE " +TABLE_WALLPAPERS+ " SET " +KEY_FAVORITE+ " = ?, " +KEY_SIZE+ " = ?, "
                +KEY_MIME_TYPE+ " = ?, " +KEY_WIDTH+ " = ?," +KEY_HEIGHT+ " = ?, " +KEY_COLOR+ " = ? "
                +"WHERE " +KEY_URL+ " = ?";
        SQLiteStatement statement = mDatabase.get().mSQLiteDatabase.compileStatement(query);
        mDatabase.get().mSQLiteDatabase.beginTransaction();

        for (Wallpaper wallpaper : wallpapers) {
            statement.clearBindings();

            statement.bindLong(1, wallpaper.isFavorite() ? 1 : 0);
            statement.bindLong(2, wallpaper.getSize());

            String mimeType = wallpaper.getMimeType();
            if (mimeType != null) {
                statement.bindString(3, wallpaper.getMimeType());
            } else {
                statement.bindNull(3);
            }

            ImageSize dimension = wallpaper.getDimensions();
            int width = dimension == null ? 0 : dimension.getWidth();
            int height = dimension == null ? 0 : dimension.getHeight();
            statement.bindLong(4, width);
            statement.bindLong(5, height);

            statement.bindLong(6, wallpaper.getColor());
            statement.bindString(7, wallpaper.getUrl());
            statement.execute();
        }

        mDatabase.get().mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.get().mSQLiteDatabase.endTransaction();
    }

    public void selectCategory(int id, boolean isSelected) {
        if (!openDatabase()) {
            LogUtil.e("Database error: selectCategory() failed to open database");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_SELECTED, isSelected ? 1 : 0);
        mDatabase.get().mSQLiteDatabase.update(TABLE_CATEGORIES, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
    }

    public void selectCategoryForMuzei(int id, boolean isSelected) {
        if (!openDatabase()) {
            LogUtil.e("Database error: selectCategoryForMuzei() failed to open database");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_MUZEI_SELECTED, isSelected ? 1 : 0);
        mDatabase.get().mSQLiteDatabase.update(TABLE_CATEGORIES, values, KEY_ID +" = ?", new String[]{String.valueOf(id)});
    }

    public void favoriteWallpaper(String url, boolean isFavorite) {
        if (!openDatabase()) {
            LogUtil.e("Database error: favoriteWallpaper() failed to open database");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_FAVORITE, isFavorite ? 1 : 0);
        mDatabase.get().mSQLiteDatabase.update(TABLE_WALLPAPERS, values,
                KEY_URL +" = ?", new String[]{url});
    }

    private List<String> getSelectedCategories(boolean isMuzei) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getSelectedCategories() failed to open database");
            return new ArrayList<>();
        }

        List<String> categories = new ArrayList<>();
        String column = isMuzei ? KEY_MUZEI_SELECTED : KEY_SELECTED;
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_CATEGORIES, new String[]{KEY_NAME}, column +" = ?",
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
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_CATEGORIES,
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
        return categories;
    }

    public Category getCategoryPreview(@NonNull Category category) {
        String name = category.getName().toLowerCase(Locale.getDefault());
        String query = "SELECT wallpapers.thumbUrl, wallpapers.color, " +
                "(SELECT COUNT(*) FROM wallpapers WHERE LOWER(wallpapers.category) LIKE ?) AS " +KEY_COUNT+
                " FROM wallpapers WHERE LOWER(wallpapers.category) LIKE ? ORDER BY RANDOM() LIMIT 1";
        Cursor cursor = mDatabase.get().mSQLiteDatabase.rawQuery(query, new String[]{"%" +name+ "%", "%" +name+ "%"});
        if (cursor.moveToFirst()) {
            do {
                category.setColor(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)));
                category.setThumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)));
                category.setCount(cursor.getInt(cursor.getColumnIndex(KEY_COUNT)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return category;
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
                    "(SELECT wallpapers.thumbUrl FROM wallpapers WHERE LOWER(wallpapers.category) LIKE ? ORDER BY RANDOM() LIMIT 1) AS thumbUrl, " +
                    "(SELECT COUNT(*) FROM wallpapers WHERE LOWER(wallpapers.category) LIKE ?) AS " +KEY_COUNT+
                    " FROM categories WHERE LOWER(categories.name) = ? LIMIT 1";
            Cursor cursor = mDatabase.get().mSQLiteDatabase.rawQuery(query, new String[]{"%" +s+ "%", "%" +s+ "%", s});
            if (cursor.moveToFirst()) {
                do {
                    Category c = Category.Builder()
                            .id(cursor.getInt(cursor.getColumnIndex(KEY_ID)))
                            .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                            .thumbUrl(cursor.getString(2))
                            .count(cursor.getInt(cursor.getColumnIndex(KEY_COUNT)))
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

        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS, null, "LOWER(" +KEY_CATEGORY+ ") LIKE ?",
                new String[]{"%" +category.toLowerCase(Locale.getDefault())+ "%"}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Nullable
    public Wallpaper getWallpaper(String url) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpaper() failed to open database");
            return null;
        }

        Wallpaper wallpaper = null;
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, KEY_URL +" = ?", new String[]{url}, null, null, null, "1");
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width  > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                int wId = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ wId;
                }

                wallpaper = Wallpaper.Builder()
                        .id(wId)
                        .name(name)
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

        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS, null, condition.toString(),
                selection.toArray(new String[selection.size()]),
                null, null, KEY_NAME);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                Wallpaper wallpaper = Wallpaper.Builder()
                        .id(id)
                        .name(name)
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .category(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)))
                        .addedOn(cursor.getString(cursor.getColumnIndex(KEY_ADDED_ON)))
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
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, null, null, null, null, getSortBy(Preferences.get(mContext).getSortBy()));
        if (cursor.moveToFirst()) {
            do {
                int colorIndex = cursor.getColumnIndex(KEY_COLOR);
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                Wallpaper.Builder builder = Wallpaper.Builder()
                        .id(id)
                        .name(name)
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

        if (Preferences.get(mContext).getSortBy() == PopupItem.Type.SORT_NAME) {
            Collections.sort(wallpapers, new AlphanumComparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    String s1 = ((Wallpaper) o1).getName();
                    String s2 = ((Wallpaper) o2).getName();
                    return super.compare(s1, s2);
                }
            });
        }
        return wallpapers;
    }

    public List<Wallpaper> getLatestWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getLatestWallpapers() failed to open database");
            return new ArrayList<>();
        }

        List<Wallpaper> wallpapers = new ArrayList<>();
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS, null, null, null, null, null,
                KEY_ADDED_ON+ " DESC, " +KEY_ID,
                String.valueOf(WallpaperBoardApplication.getConfig().getLatestWallpapersDisplayMax()));
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width  > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                Wallpaper wallpaper = Wallpaper.Builder()
                        .id(id)
                        .name(name)
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

        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS, null, CONDITION.toString(),
                selection.toArray(new String[selection.size()]), null, null, "RANDOM()", "1");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                wallpaper = Wallpaper.Builder()
                        .name(name)
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

        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS, null, null, null, null, null, null, null);
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
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS, null, KEY_FAVORITE +" = ?",
                new String[]{"1"}, null, null, KEY_NAME +", "+ KEY_ID);
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width  > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper "+ id;
                }

                Wallpaper wallpaper = Wallpaper.Builder()
                        .id(id)
                        .name(name)
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

        if (Preferences.get(mContext).getSortBy() == PopupItem.Type.SORT_NAME) {
            Collections.sort(wallpapers, new AlphanumComparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    String s1 = ((Wallpaper) o1).getName();
                    String s2 = ((Wallpaper) o2).getName();
                    return super.compare(s1, s2);
                }
            });
        }
        return wallpapers;
    }

    public void deleteWallpapers(@NonNull List<Wallpaper> wallpapers) {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteWallpapers() failed to open database");
            return;
        }

        String query = "DELETE FROM " +TABLE_WALLPAPERS+ " WHERE " +KEY_URL+ " = ?";
        SQLiteStatement statement = mDatabase.get().mSQLiteDatabase.compileStatement(query);
        mDatabase.get().mSQLiteDatabase.beginTransaction();

        for (Wallpaper wallpaper : wallpapers) {
            statement.clearBindings();
            statement.bindString(1, wallpaper.getUrl());
            statement.execute();
        }

        mDatabase.get().mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.get().mSQLiteDatabase.endTransaction();
    }

    public void resetAutoIncrement() {
        if (!openDatabase()) {
            LogUtil.e("Database error: resetAutoIncrement() failed to open database");
            return;
        }

        mSQLiteDatabase.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_WALLPAPERS});
    }

    public void deleteCategories(@NonNull List<Category> categories) {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteCategories() failed to open database");
            return;
        }

        String query = "DELETE FROM " +TABLE_CATEGORIES+ " WHERE " +KEY_NAME+ " = ?";
        SQLiteStatement statement = mDatabase.get().mSQLiteDatabase.compileStatement(query);
        mDatabase.get().mSQLiteDatabase.beginTransaction();

        for (Category category : categories) {
            statement.clearBindings();
            statement.bindString(1, category.getName());
            statement.execute();
        }

        mDatabase.get().mSQLiteDatabase.setTransactionSuccessful();
        mDatabase.get().mSQLiteDatabase.endTransaction();
    }

    private String getSortBy(PopupItem.Type type) {
        switch (type) {
            case SORT_LATEST:
                return KEY_ADDED_ON +" DESC, "+ KEY_ID;
            case SORT_OLDEST:
                return KEY_ADDED_ON +", "+ KEY_ID +" DESC";
            case SORT_NAME:
                return KEY_NAME;
            case SORT_RANDOM:
                return "RANDOM()";
            default:
                return KEY_ADDED_ON +" DESC, "+ KEY_ID;
        }
    }
}
