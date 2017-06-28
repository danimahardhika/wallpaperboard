package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.dm.wallpaper.board.BuildConfig;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.utils.LogUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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

public class CrashReportHelper {

    @Nullable
    public static String buildCrashLog(@NonNull Context context, @NonNull File folder, String stackTrace) {
        try {
            if (stackTrace.length() == 0) return null;

            File fileDir = new File(folder.toString() + "/crashlog.txt");
            String deviceInfo = getDeviceInfoForCrashReport(context);
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileDir), "UTF8"));
            out.append(deviceInfo).append(stackTrace);
            out.flush();
            out.close();

            return fileDir.toString();
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    @NonNull
    private static String getDeviceInfo(@NonNull Context context) {
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        StringBuilder sb = new StringBuilder();
        final int height = displaymetrics.heightPixels;
        final int width = displaymetrics.widthPixels;

        String appVersion = "";
        try {
            appVersion = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}

        sb.append("Manufacturer : ").append(Build.MANUFACTURER)
                .append("\nModel : ").append(Build.MODEL)
                .append("\nProduct : ").append(Build.PRODUCT)
                .append("\nScreen Resolution : ")
                .append(width).append(" x ").append(height).append(" pixels")
                .append("\nAndroid Version : ").append(Build.VERSION.RELEASE)
                .append("\nApp Version : ").append(appVersion)
                .append("\n");
        return sb.toString();
    }

    @NonNull
    public static String getDeviceInfoForCrashReport(@NonNull Context context) {
        return "WallpaperBoard Version : " + BuildConfig.VERSION_NAME +
                "\nApp Name : " +context.getResources().getString(R.string.app_name)
                + "\n"+ getDeviceInfo(context);
    }
}
