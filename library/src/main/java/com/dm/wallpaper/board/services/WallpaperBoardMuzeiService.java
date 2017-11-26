package com.dm.wallpaper.board.services;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.dm.wallpaper.board.activities.WallpaperBoardPreviewActivity;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.MuzeiHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

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

public abstract class WallpaperBoardMuzeiService extends RemoteMuzeiArtSource {

    public WallpaperBoardMuzeiService(String name) {
        super(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            boolean restart = intent.getBooleanExtra("restart", false);
            if (restart) {
                try {
                    onTryUpdate(UPDATE_REASON_USER_NEXT);
                } catch (RetryException ignored) {}
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RemoteMuzeiArtSource.RetryException {
        try {
            if (Preferences.get(this).isConnectedAsPreferred()) {
                Wallpaper wallpaper = MuzeiHelper.getRandomWallpaper(this);

                if (wallpaper != null) {
                    Uri uri = Uri.parse(wallpaper.getUrl());

                    Intent intent = new Intent(this, WallpaperBoardPreviewActivity.class);
                    intent.putExtra(Extras.EXTRA_URL, wallpaper.getUrl());
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    publishArtwork(new Artwork.Builder()
                            .title(wallpaper.getName())
                            .byline(wallpaper.getAuthor())
                            .imageUri(uri)
                            .viewIntent(intent)
                            .build());

                    scheduleUpdate(System.currentTimeMillis() +
                            Preferences.get(this).getRotateTime());
                }

                Database.get(this).closeDatabase();
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }
}
