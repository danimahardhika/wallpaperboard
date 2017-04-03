package com.dm.wallpaper.board.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.WallpaperBoardListener;

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

public class WallpaperBoardReceiver extends BroadcastReceiver {

    public static final String PROCESS_RESPONSE = "com.dm.wallpaper.board.broadcast.receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            WallpaperBoardListener listener = (WallpaperBoardListener) context;
            listener.onWallpapersChecked(intent);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }
}
