package com.dm.wallpaper.board.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.activities.callbacks.SplashScreenCallback;
import com.dm.wallpaper.board.activities.configurations.SplashScreenConfiguration;
import com.dm.wallpaper.board.utils.LogUtil;

import java.lang.ref.WeakReference;

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

public abstract class WallpaperBoardSplashActivity extends AppCompatActivity implements SplashScreenCallback {

    private AsyncTask mAsyncTask;

    private SplashScreenConfiguration mConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mConfig = onInit();
        initBottomText();

        mAsyncTask = new SplashScreenLoader(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    private void initBottomText() {
        TextView splashTitle = findViewById(R.id.splash_title);
        if (splashTitle != null) {
            splashTitle.setText(mConfig.getBottomText());

            if (mConfig.getBottomTextColor() != -1) {
                splashTitle.setTextColor(mConfig.getBottomTextColor());
            } else {
                int color = ContextCompat.getColor(this, R.color.splashColor);
                splashTitle.setTextColor(ColorHelper.getBodyTextColor(color));
            }

            splashTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, mConfig.getBottomTextSize());
            splashTitle.setTypeface(mConfig.getBottomTextFont(this));
        }
    }

    private class SplashScreenLoader extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Context> context;

        private SplashScreenLoader(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

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
            if (context.get() == null) return;
            if (context.get() instanceof Activity) {
                if (((Activity) context.get()).isFinishing()) return;
            }

            mAsyncTask = null;
            if (aBoolean) {
                Intent intent = new Intent(context.get(), mConfig.getMainActivity());
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }
    }
}
