package com.dm.wallpaper.board.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.helpers.ColorHelper;
import com.dm.wallpaper.board.utils.Extras;

import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
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

    @BindView(R2.id.progress)
    ProgressBar mProgress;

    private Class<?> mMainActivity;
    private AsyncTask<Void, Void, Boolean> mCheckRszIo;

    @Deprecated
    public void initSplashActivity(@Nullable Bundle savedInstanceState, @NonNull Class<?> mainActivity, int duration) {
        initSplashActivity(savedInstanceState, mainActivity);
    }

    public void initSplashActivity(@Nullable Bundle savedInstanceState, @NonNull Class<?> mainActivity) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        mMainActivity = mainActivity;

        mProgress.getIndeterminateDrawable().setColorFilter(ColorHelper.getAttributeColor(
                this, R.attr.colorAccent), PorterDuff.Mode.SRC_IN);

        int color = ContextCompat.getColor(this, R.color.splashColor);
        int titleColor = ColorHelper.getTitleTextColor(color);
        TextView splashTitle = ButterKnife.findById(this, R.id.splash_title);
        splashTitle.setTextColor(ColorHelper.setColorAlpha(titleColor, 0.6f ));

        TextView splashLoading = ButterKnife.findById(this, R.id.splash_loading);
        splashLoading.setText(String.format(
                getResources().getString(R.string.splash_screen_loading),
                getResources().getString(R.string.app_name)));

        checkRszIo();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        if (mCheckRszIo != null) mCheckRszIo.cancel(true);
        super.onDestroy();
    }

    private void checkRszIo() {
        mCheckRszIo = new AsyncTask<Void, Void, Boolean>() {

            final String rszio = "https://rsz.io/";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress.setVisibility(View.VISIBLE);
            }

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
                        Log.d(Extras.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                WallpaperBoardActivity.sRszIoAvailable = aBoolean;
                Log.d(Extras.LOG_TAG, "rsz.io availability: " +WallpaperBoardActivity.sRszIoAvailable);
                mCheckRszIo = null;

                startActivity(new Intent(WallpaperBoardSplashActivity.this, mMainActivity));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }.execute();
    }
}
