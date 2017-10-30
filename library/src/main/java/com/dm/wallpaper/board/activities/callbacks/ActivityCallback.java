package com.dm.wallpaper.board.activities.callbacks;

import android.support.annotation.NonNull;

import com.dm.wallpaper.board.activities.configurations.ActivityConfiguration;

/**
 * Author: Dani Mahardhika
 * Created on: 10/27/2017
 * https://github.com/danimahardhika
 */

public interface ActivityCallback {

    @NonNull ActivityConfiguration onInit();
}
