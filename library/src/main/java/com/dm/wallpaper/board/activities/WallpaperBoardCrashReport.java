package com.dm.wallpaper.board.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.helpers.CrashReportHelper;
import com.dm.wallpaper.board.helpers.LocaleHelper;

import java.io.File;

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

public class WallpaperBoardCrashReport extends AppCompatActivity {

    public static final String EXTRA_STACKTRACE = "stacktrace";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                finish();
                return;
            }

            LocaleHelper.setLocale(this);

            final String email = WallpaperBoardApplication.getConfig().getCrashReportEmail();
            if (email == null) {
                finish();
                return;
            }

            String stackTrace = bundle.getString(EXTRA_STACKTRACE);
            String deviceInfo = CrashReportHelper.getDeviceInfoForCrashReport(this);

            String message = String.format(getResources().getString(R.string.crash_report_message),
                    getResources().getString(R.string.app_name));
            new MaterialDialog.Builder(this)
                    .title(R.string.crash_report)
                    .content(message)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .positiveText(R.string.crash_report_send)
                    .negativeText(R.string.close)
                    .onPositive((dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL, email);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "WallpaperBoard: Crash Report");

                        intent = prepareUri(deviceInfo, stackTrace, intent);

                        startActivity(Intent.createChooser(intent,
                                getResources().getString(R.string.email_client)));
                        dialog.dismiss();
                    })
                    .dismissListener(dialogInterface -> finish())
                    .show();
        } catch (Exception e) {
            finish();
        }
    }

    private Intent prepareUri(String deviceInfo, String stackTrace, Intent intent) {
        String crashLog = CrashReportHelper.buildCrashLog(this, getCacheDir(), stackTrace);
        boolean granted = PermissionHelper.isStorageGranted(this);
        if (crashLog != null) {
            Uri uri = FileHelper.getUriFromFile(this, getPackageName(), new File(crashLog));
            if (uri != null) {
                intent.putExtra(Intent.EXTRA_TEXT, deviceInfo +"\n");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return intent;
            } else {
                if (granted) {
                    uri = Uri.fromFile(new File(crashLog));
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    return intent;
                }
            }
        }

        intent.putExtra(Intent.EXTRA_TEXT, deviceInfo + stackTrace);
        return intent;
    }
}
