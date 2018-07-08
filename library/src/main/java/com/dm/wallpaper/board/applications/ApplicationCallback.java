package com.dm.wallpaper.board.applications;

import android.support.annotation.NonNull;

/**
 * Author: Dani Mahardhika
 * Created on: 10/27/2017
 * https://github.com/danimahardhika
 */

public interface ApplicationCallback {

    @NonNull WallpaperBoardConfiguration onInit();
}
