package com.dm.wallpaper.board.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.BackupHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.utils.Extras;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

/**
 * Author: Dani Mahardhika
 * Created on: 11/16/2017
 * https://github.com/danimahardhika
 */

public class LocalFavoritesBackupTask extends AsyncTask<Void, Void, Boolean> {

    private boolean mIsStorageGranted;
    private WeakReference<Context> mContext;

    private LocalFavoritesBackupTask(Context context) {
        mContext = new WeakReference<>(context);
        mIsStorageGranted = PermissionHelper.isStorageGranted(mContext.get());
    }

    public AsyncTask start() {
        return start(SERIAL_EXECUTOR);
    }

    public AsyncTask start(@NonNull Executor executor) {
        return executeOnExecutor(executor);
    }

    public static LocalFavoritesBackupTask with(@NonNull Context context) {
        return new LocalFavoritesBackupTask(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mIsStorageGranted) {
            File file = BackupHelper.getDefaultDirectory(mContext.get());
            if (!file.exists()) {
                file.mkdirs();
            }

            File nomedia = new File(file.getParent(), BackupHelper.NOMEDIA);
            if (!nomedia.exists()) {
                try {
                    nomedia.createNewFile();
                } catch (IOException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                Thread.sleep(1);
                if (!mIsStorageGranted) {
                    LogUtil.e("Storage permission not granted, it's required to create local backup");
                    return false;
                }

                List<Wallpaper> favorites = Database.get(mContext.get()).getFavoriteWallpapers();
                File file = new File(BackupHelper.getDefaultDirectory(mContext.get()), BackupHelper.FILE_BACKUP);

                Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file), "UTF8"));

                for (Wallpaper favorite : favorites) {
                    writer.append("<")
                            .append(Extras.EXTRA_ITEM.toLowerCase(Locale.getDefault()))
                            .append(" ")
                            .append(Extras.EXTRA_URL.toLowerCase(Locale.getDefault()))
                            .append("=\"")
                            .append(URLEncoder.encode(favorite.getUrl(), "utf-8"))
                            .append("\"")
                            .append("/>")
                            .append("\n");
                }

                writer.flush();
                writer.close();

                Database.get(mContext.get()).closeDatabase();
                return true;
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
        if (aBoolean) {
            LogUtil.d("Local backup created");
        }
    }
}
