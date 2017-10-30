package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;

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

public class TypefaceHelper {

    private static HashMap<String, WeakReference<Typeface>> mTypefaces = new HashMap<>();
    private static final String REGULAR = "regular";
    private static final String MEDIUM = "medium";
    private static final String BOLD = "bold";
    private static final String LOGO = "logo";

    @Nullable
    public static Typeface getRegular(@NonNull Context context) {
        try {
            if (!mTypefaces.containsKey(REGULAR) || mTypefaces.get(REGULAR).get() == null) {
                Typeface typeface = Typeface.createFromAsset(
                        context.getAssets(), "fonts/Font-Regular.ttf");
                mTypefaces.put(REGULAR, new WeakReference<>(typeface));
            }
            return mTypefaces.get(REGULAR).get();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Typeface getMedium(@NonNull Context context) {
        try {
            if (!mTypefaces.containsKey(MEDIUM) || mTypefaces.get(MEDIUM).get() == null) {
                Typeface typeface = Typeface.createFromAsset(
                        context.getAssets(), "fonts/Font-Medium.ttf");
                mTypefaces.put(MEDIUM, new WeakReference<>(typeface));
            }
            return mTypefaces.get(MEDIUM).get();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Typeface getBold(@NonNull Context context) {
        try {
            if (!mTypefaces.containsKey(BOLD) || mTypefaces.get(BOLD).get() == null) {
                Typeface typeface = Typeface.createFromAsset(
                        context.getAssets(), "fonts/Font-Bold.ttf");
                mTypefaces.put(BOLD, new WeakReference<>(typeface));
            }
            return mTypefaces.get(BOLD).get();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Typeface getLogo(@NonNull Context context) {
        try {
            if (!mTypefaces.containsKey(LOGO) || mTypefaces.get(LOGO).get() == null) {
                Typeface typeface = Typeface.createFromAsset(
                        context.getAssets(), "fonts/Font-Logo.ttf");
                mTypefaces.put(LOGO, new WeakReference<>(typeface));
            }
            return mTypefaces.get(LOGO).get();
        } catch (Exception e) {
            return null;
        }
    }
}
