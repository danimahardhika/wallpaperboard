package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.preferences.Preferences;

import butterknife.BindView;
import butterknife.ButterKnife;

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

public class WallpaperSettingsFragment extends DialogFragment implements View.OnClickListener {

    @BindView(R2.id.wallpaper_crop)
    LinearLayout mWallpaperCrop;
    @BindView(R2.id.wallpaper_crop_checkbox)
    AppCompatCheckBox mWallpaperCropCheck;
    @BindView(R2.id.apply_lockscreen)
    LinearLayout mApplyLockscreen;
    @BindView(R2.id.apply_lockscreen_title)
    TextView mApplyLockscreenTitle;
    @BindView(R2.id.apply_lockscreen_subtitle)
    TextView mApplyLockscreenSubtitle;
    @BindView(R2.id.apply_lockscreen_checkbox)
    AppCompatCheckBox mApplyLockscreenCheck;
    @BindView(R2.id.apply_lockscreen_error)
    TextView mApplyLockscreenError;

    private static final String TAG = "com.dm.wallpaper.board.dialog.wallpaper.settings";

    @NonNull
    private static WallpaperSettingsFragment newInstance() {
        return new WallpaperSettingsFragment();
    }

    public static void showWallpaperSettings(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = WallpaperSettingsFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalStateException | IllegalArgumentException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.typeface("Font-Medium.ttf", "Font-Regular.ttf");
        builder.title(R.string.menu_wallpaper_settings);
        builder.customView(R.layout.fragment_wallpaper_settings, false);
        builder.positiveText(R.string.close);
        MaterialDialog dialog = builder.build();
        dialog.show();

        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWallpaperCropCheck.setChecked(Preferences.get(getActivity()).isWallpaperCrop());
        mApplyLockscreenCheck.setChecked(Preferences.get(getActivity()).isApplyLockscreen());

        mWallpaperCrop.setOnClickListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mApplyLockscreenTitle.setAlpha(0.5f);
            mApplyLockscreenSubtitle.setAlpha(0.5f);
            mApplyLockscreenCheck.setAlpha(0.5f);
            mApplyLockscreenError.setVisibility(View.VISIBLE);
            return;
        }

        mApplyLockscreen.setOnClickListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (Preferences.get(getActivity()).isWallpaperCrop()) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.wallpaper_crop) {
            Preferences.get(getActivity()).setWallpaperCrop(!mWallpaperCropCheck.isChecked());
            mWallpaperCropCheck.setChecked(!mWallpaperCropCheck.isChecked());
        } else if (id == R.id.apply_lockscreen) {
            Preferences.get(getActivity()).setApplyLockscreen(!mApplyLockscreenCheck.isChecked());
            mApplyLockscreenCheck.setChecked(!mApplyLockscreenCheck.isChecked());
        }
    }
}
