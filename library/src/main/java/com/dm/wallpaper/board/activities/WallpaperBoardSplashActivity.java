package com.dm.wallpaper.board.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.helpers.ColorHelper;

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

    private Runnable mRunnable;
    private Handler mHandler;

    public void initSplashActivity(@Nullable Bundle savedInstanceState, @NonNull Class<?> mainActivity, int duration) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int color = ContextCompat.getColor(this, R.color.splashColor);
        int titleColor = ColorHelper.getTitleTextColor(color);
        TextView splashTitle = ButterKnife.findById(this, R.id.splash_title);
        splashTitle.setTextColor(ColorHelper.setColorAlpha(titleColor, 0.6f ));

        mRunnable = () -> {
            startActivity(new Intent(this, mainActivity));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, duration);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (mRunnable != null && mHandler != null)
            mHandler.removeCallbacks(mRunnable);
        super.onBackPressed();
    }
}
