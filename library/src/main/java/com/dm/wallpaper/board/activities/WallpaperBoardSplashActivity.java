package com.dm.wallpaper.board.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.helpers.ColorHelper;
import com.dm.wallpaper.board.utils.LogUtil;

import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

public class WallpaperBoardSplashActivity extends AppCompatActivity {

    private Class<?> mMainActivity;
    private AsyncTask<Void, Void, Boolean> mCheckRszIo;
    private AsyncTask<Void, Void, Boolean> mPrepareApp;

    @Deprecated
    public void initSplashActivity(@Nullable Bundle savedInstanceState, @NonNull Class<?> mainActivity, int duration) {
        initSplashActivity(savedInstanceState, mainActivity);
    }

    public void initSplashActivity(@Nullable Bundle savedInstanceState, @NonNull Class<?> mainActivity) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mMainActivity = mainActivity;

        int color = ContextCompat.getColor(this, R.color.splashColor);
        int titleColor = ColorHelper.getTitleTextColor(color);
        TextView splashTitle = ButterKnife.findById(this, R.id.splash_title);
        splashTitle.setTextColor(ColorHelper.setColorAlpha(titleColor, 0.6f));

        prepareApp();
        checkRszIo();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (mCheckRszIo != null) mCheckRszIo.cancel(true);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mPrepareApp != null) mPrepareApp.cancel(true);
        super.onDestroy();
    }

    private void prepareApp() {
        mPrepareApp = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(500);
                        return true;
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                        return false;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    mPrepareApp = null;

                    startActivity(new Intent(WallpaperBoardSplashActivity.this, mMainActivity));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkRszIo() {
        mCheckRszIo = new AsyncTask<Void, Void, Boolean>() {

            final String rszio = "https://rsz.io/";

            @Override
            protected Boolean doInBackground(Void... voids) {
                while ((!isCancelled())) {
                    try {
                        Thread.sleep(1);
                        URL url = new URL(rszio);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setReadTimeout(6000);
                        connection.setConnectTimeout(6000);
                        int code = connection.getResponseCode();
                        return code == 200;
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
                mCheckRszIo = null;

                WallpaperBoardActivity.sRszIoAvailable = aBoolean;
                LogUtil.e("rsz.io availability: " +WallpaperBoardActivity.sRszIoAvailable);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
