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

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.utils.LogUtil;

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
        TextView splashTitle = ButterKnife.findById(this, R.id.splash_title);
        splashTitle.setTextColor(ColorHelper.getBodyTextColor(color));

        prepareApp();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
                mPrepareApp = null;
                if (aBoolean) {
                    startActivity(new Intent(WallpaperBoardSplashActivity.this, mMainActivity));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
