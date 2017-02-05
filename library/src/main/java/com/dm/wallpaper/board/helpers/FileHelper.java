package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.dm.wallpaper.board.utils.Extras;
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

public class FileHelper {

    public static final String IMAGE_EXTENSION = ".jpeg";

    @Nullable
    public static Uri getUriFromFile(Context context, String applicationId, File file) {
        try {
            return FileProvider.getUriForFile(context, applicationId + ".fileProvider", file);
        } catch (IllegalArgumentException e) {
            Log.d(Extras.LOG_TAG, Log.getStackTraceString(e));
        }
        return null;
    }
}
