package com.dm.wallpaper.board.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.ListPopupWindow;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.helpers.TypefaceHelper;

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

public class Tooltip {

    private final ListPopupWindow mPopupWindow;
    private final Builder mBuilder;
    private boolean mCheckboxState = false;

    private Tooltip(Builder builder) {
        mBuilder = builder;

        mPopupWindow = new ListPopupWindow(mBuilder.mContext);
        mPopupWindow.setContentWidth(getMeasuredWidth(builder.mContext));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = mPopupWindow.getBackground();
            if (drawable != null) {
                drawable.setColorFilter(ColorHelper.getAttributeColor(
                        builder.mContext, R.attr.card_background), PorterDuff.Mode.SRC_IN);
            }
        } else {
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(
                    ColorHelper.getAttributeColor(builder.mContext, R.attr.card_background)));
        }

        mPopupWindow.setListSelector(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setAnchorView(mBuilder.mTo);
        mPopupWindow.setForceIgnoreOutsideTouch(true);
        mPopupWindow.setAdapter(new TooltipAdapter(mBuilder.mContext, this));
    }

    public boolean getCheckboxState() {
        return mCheckboxState;
    }

    public void show() {
        try {
            mPopupWindow.show();
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public void dismiss() {
        if (mPopupWindow.isShowing())
            mPopupWindow.dismiss();
    }

    private int getMeasuredWidth(@NonNull Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int maxWidth = context.getResources().getDimensionPixelSize(R.dimen.tooltip_max_width);
        int minWidth = context.getResources().getDimensionPixelSize(R.dimen.tooltip_min_width);
        String longestText = mBuilder.mContent;
        if (mBuilder.mContent.length() < mBuilder.mDesc.length()) {
            longestText = mBuilder.mDesc;
        }

        int padding = context.getResources().getDimensionPixelSize(R.dimen.content_margin);
        TextView textView = new TextView(context);
        textView.setPadding(padding, 0, padding, 0);
        textView.setTypeface(TypefaceHelper.getRegular(context));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources()
                .getDimension(R.dimen.text_content_subtitle));
        textView.setText(longestText);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = textView.getMeasuredWidth() + padding;
        if (measuredWidth <= minWidth) {
            return minWidth;
        }

        if (measuredWidth >= minWidth && measuredWidth <= maxWidth) {
            return measuredWidth;
        }
        return maxWidth;
    }

    public static Builder Builder(@NonNull Context context) {
        return new Builder(context);
    }

    public static class Builder {

        private final Context mContext;
        private View mTo;
        private String mContent;
        private String mDesc;
        private int mDescIcon;
        private String mButton;
        private Callback mCallback;
        private boolean mCancelable;
        private boolean mIsDontShowAgainVisible;

        private Builder(@NonNull Context context) {
            mContext = context;
            mContent = "";
            mDescIcon = 0;
            mCancelable = true;
            mButton = mContext.getResources().getString(android.R.string.ok);
            mIsDontShowAgainVisible = false;
        }

        public Builder to(@NonNull View to) {
            mTo = to;
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public Builder content(@StringRes int res) {
            mContent = mContext.getResources().getString(res);
            return this;
        }

        public Builder desc(@StringRes int res) {
            mDesc = mContext.getResources().getString(res);
            return this;
        }

        public Builder descIcon(@DrawableRes int res) {
            mDescIcon = res;
            return this;
        }

        public Builder buttonText(@StringRes int res) {
            mButton = mContext.getResources().getString(res);
            return this;
        }

        public Builder buttonCallback(@Nullable Callback callback) {
            mCallback = callback;
            return this;
        }

        public Builder visibleDontShowAgain(boolean show) {
            mIsDontShowAgainVisible = show;
            return this;
        }

        public Tooltip build() {
            return new Tooltip(this);
        }

        public void show() {
            build().show();
        }
    }

    class TooltipAdapter extends BaseAdapter {

        private final Context mContext;
        private final Tooltip mTooltip;

        TooltipAdapter(Context context, Tooltip tooltip) {
            mContext = context;
            mTooltip = tooltip;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public String getItem(int position) {
            return mBuilder.mContent;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(mContext, R.layout.tooltips_popup, null);
            }

            TextView content = view.findViewById( R.id.content);
            TextView desc = view.findViewById( R.id.desc);
            AppCompatCheckBox checkBox = view.findViewById( R.id.checkbox);
            TextView button = view.findViewById( R.id.button);

            content.setText(mBuilder.mContent);
            button.setText(mBuilder.mButton);

            if (mBuilder.mDesc != null) {
                desc.setVisibility(View.VISIBLE);
                desc.setText(mBuilder.mDesc);

                if (mBuilder.mDescIcon != 0) {
                    int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                    Drawable drawable = DrawableHelper.getTintedDrawable(mContext, mBuilder.mDescIcon, color);
                    desc.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                }
            }


            checkBox.setVisibility(mBuilder.mIsDontShowAgainVisible ? View.VISIBLE : View.GONE);
            checkBox.setOnCheckedChangeListener((compoundButton, b) -> mCheckboxState = b);

            button.setOnClickListener(v -> {
                if (mBuilder.mCallback != null) {
                    mBuilder.mCallback.onButtonClick(mTooltip);
                    return;
                }

                mTooltip.dismiss();
            });
            return view;
        }
    }

    public interface Callback {
        void onButtonClick(Tooltip tooltip);
    }
}
