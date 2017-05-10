package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.helpers.ColorHelper;
import com.dm.wallpaper.board.helpers.DrawableHelper;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.RefreshDurationListener;

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

public class RefreshDurationFragment extends DialogFragment implements View.OnClickListener {

    @BindView(R2.id.number_picker)
    NumberPicker mNumberPicker;
    @BindView(R2.id.minute)
    AppCompatRadioButton mMinute;
    @BindView(R2.id.hour)
    AppCompatRadioButton mHour;

    private int mRotateTime;
    private boolean mIsMinute;

    private static final String MINUTE = "minute";
    private static final String ROTATE_TIME = "rotate_time";
    private static final String TAG = "com.dm.wallpaper.board.dialog.refresh.duration";

    private static RefreshDurationFragment newInstance(int rotateTime, boolean isMinute) {
        RefreshDurationFragment fragment = new RefreshDurationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ROTATE_TIME, rotateTime);
        bundle.putBoolean(MINUTE, isMinute);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showRefreshDurationDialog(FragmentManager fm, int rotateTime, boolean isMinute) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = RefreshDurationFragment.newInstance(rotateTime, isMinute);
            dialog.show(ft, TAG);
        } catch (IllegalStateException | IllegalArgumentException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_refresh_duration, true);
        builder.typeface("Font-Medium.ttf", "Font-Regular.ttf");
        builder.title(R.string.muzei_refresh_duration);
        builder.positiveText(R.string.close);

        MaterialDialog dialog = builder.build();
        dialog.show();

        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRotateTime = getArguments().getInt(ROTATE_TIME, 1);
        mIsMinute = getArguments().getBoolean(MINUTE, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNumberPicker.setMinValue(1);
        mNumberPicker.setMaxValue(100);
        setDividerColor(mNumberPicker);
        mMinute.setOnClickListener(this);
        mHour.setOnClickListener(this);

        mMinute.setChecked(mIsMinute);
        mHour.setChecked(!mIsMinute);
        mNumberPicker.setValue(mRotateTime);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        RefreshDurationListener listener = (RefreshDurationListener) getActivity();
        listener.onRefreshDurationSet(mNumberPicker.getValue(), mMinute.isChecked());
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.minute) {
            mMinute.setChecked(true);
            mHour.setChecked(false);
        } else if (id == R.id.hour) {
            mHour.setChecked(true);
            mMinute.setChecked(false);
        }
    }

    private void setDividerColor (NumberPicker picker) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    int color = ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent);
                    pf.set(picker, DrawableHelper.getTintedDrawable(
                            getActivity(), R.drawable.numberpicker_divider, color));
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
                break;
            }
        }
    }
}
