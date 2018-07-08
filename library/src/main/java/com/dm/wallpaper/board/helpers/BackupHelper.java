package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

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

public abstract class BackupHelper {

    public static final String NOMEDIA = ".nomedia";
    public static final String FILE_BACKUP = ".backup";
    private static final String DIRECTORY_BACKUP = ".wallpaperboard";

    public static File getDefaultDirectory(@NonNull Context context) {
        return new File(Environment.getExternalStorageDirectory(),
                DIRECTORY_BACKUP +"/"+ context.getPackageName());
    }
}
