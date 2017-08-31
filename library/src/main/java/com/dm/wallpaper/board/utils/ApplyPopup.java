package com.dm.wallpaper.board.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.ListPopupWindow;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

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

public class ApplyPopup {

    private ListPopupWindow mPopupWindow;
    private ApplyPopupAdapter mAdapter;

    private ApplyPopup(Builder builder) {
        List<Item> items = getApplyItems(builder.mContext);
        mPopupWindow = new ListPopupWindow(builder.mContext);
        mAdapter = new ApplyPopupAdapter(builder.mContext, items);

        mPopupWindow.setWidth(getMeasuredWidth(builder.mContext));
        Drawable drawable = mPopupWindow.getBackground();
        if (drawable != null) {
            drawable.setColorFilter(ColorHelper.getAttributeColor(
                    builder.mContext, R.attr.card_background), PorterDuff.Mode.SRC_IN);
        }

        mPopupWindow.setAnchorView(builder.mTo);
        mPopupWindow.setAdapter(mAdapter);
        mPopupWindow.setOnItemClickListener((adapterView, view, i, l) -> {
            if (builder.mCallback != null) {
                builder.mCallback.onClick(this, i);
                return;
            }

            mPopupWindow.dismiss();
        });
    }

    public void show() {
        mPopupWindow.show();
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    public List<Item> getItems() {
        return mAdapter.getItems();
    }

    public void updateItem(int position, Item item) {
        mAdapter.updateItem(position, item);
    }

    public void removeItem(int position) {
        mAdapter.removeItem(position);
    }

    public static Builder Builder(@NonNull Context context) {
        return new Builder(context);
    }

    private List<Item> getApplyItems(@NonNull Context context) {
        List<Item> items = new ArrayList<>();
        items.add(new Item(context.getResources().getString(R.string.menu_wallpaper_crop))
                .setType(Type.WALLPAPER_CROP)
                .setCheckboxValue(Preferences.get(context).isCropWallpaper())
                .setShowCheckbox(true));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            items.add(new Item(context.getResources().getString(R.string.menu_apply_lockscreen))
                    .setType(Type.LOCKSCREEN)
                    .setIcon(R.drawable.ic_toolbar_lockscreen));
        }

        items.add(new Item(context.getResources().getString(R.string.menu_apply_homescreen))
                .setType(Type.HOMESCREEN)
                .setIcon(R.drawable.ic_toolbar_homescreen));

        if (context.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
            items.add(new Item(context.getResources().getString(R.string.menu_save))
                    .setType(Type.DOWNLOAD)
                    .setIcon(R.drawable.ic_toolbar_download));
        }
        return items;
    }

    private int getMeasuredWidth(@NonNull Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int maxWidth = context.getResources().getDimensionPixelSize(R.dimen.popup_max_width);
        int minWidth = context.getResources().getDimensionPixelSize(R.dimen.popup_min_width);
        String longestText = "";
        for (Item item : mAdapter.getItems()) {
            if (item.getTitle().length() > longestText.length())
                longestText = item.getTitle();
        }

        int padding = context.getResources().getDimensionPixelSize(R.dimen.content_margin);
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.icon_size_small);
        TextView textView = new TextView(context);
        textView.setPadding(padding + iconSize + padding, 0, padding, 0);
        textView.setTypeface(TypefaceHelper.getRegular(context));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources()
                .getDimension(R.dimen.text_content_subtitle));
        textView.setText(longestText);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);

        if (textView.getMeasuredWidth() <= minWidth) {
            return minWidth;
        }

        if (textView.getMeasuredWidth() >= minWidth && textView.getMeasuredWidth() <= maxWidth) {
            return textView.getMeasuredWidth();
        }
        return maxWidth;
    }

    public static class Builder {

        private final Context mContext;
        private Callback mCallback;
        private View mTo;

        private Builder(Context context) {
            mContext = context;
        }

        public Builder to(@Nullable View to) {
            mTo = to;
            return this;
        }

        public Builder callback(@Nullable Callback callback) {
            mCallback = callback;
            return this;
        }

        public ApplyPopup build() {
            return new ApplyPopup(this);
        }

        public void show() {
            build().show();
        }
    }

    public class Item {

        private final String mTitle;
        private int mIcon;
        private boolean mShowCheckbox;
        private boolean mCheckboxValue;
        private Type mType;

        public Item(String title) {
            mTitle = title;
            mShowCheckbox = false;
            mCheckboxValue = false;
        }

        public Item setIcon(@DrawableRes int icon) {
            mIcon = icon;
            return this;
        }

        public Item setShowCheckbox(boolean showCheckbox) {
            mShowCheckbox = showCheckbox;
            return this;
        }

        public Item setCheckboxValue(boolean checkboxValue) {
            mCheckboxValue = checkboxValue;
            return this;
        }

        public Item setType(Type type) {
            mType = type;
            return this;
        }

        public String getTitle() {
            return mTitle;
        }

        @DrawableRes
        public int getIcon() {
            return mIcon;
        }

        public boolean isShowCheckbox() {
            return mShowCheckbox;
        }

        public boolean getCheckboxValue() {
            return mCheckboxValue;
        }

        public Type getType() {
            return mType;
        }
    }

    public enum Type {
        WALLPAPER_CROP,
        HOMESCREEN,
        LOCKSCREEN,
        DOWNLOAD
    }

    class ApplyPopupAdapter extends BaseAdapter {

        private List<Item> mItems;
        private final Context mContext;

        ApplyPopupAdapter(@NonNull Context context, @NonNull List<Item> items) {
            mContext = context;
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Item getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = View.inflate(mContext, R.layout.apply_popup_item_list, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Item item = mItems.get(position);
            holder.checkBox.setVisibility(View.GONE);
            if (item.isShowCheckbox()) {
                holder.checkBox.setChecked(item.getCheckboxValue());
                holder.checkBox.setVisibility(View.VISIBLE);
            }

            if (item.getIcon() != 0) {
                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                Drawable drawable = DrawableHelper.getTintedDrawable(mContext, item.getIcon(), color);
                holder.title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            holder.title.setText(item.getTitle());
            return view;
        }

        class ViewHolder {

            @BindView(R2.id.checkbox)
            AppCompatCheckBox checkBox;
            @BindView(R2.id.title)
            TextView title;

            ViewHolder(@NonNull View view) {
                ButterKnife.bind(this, view);
            }
        }

        List<Item> getItems() {
            return mItems;
        }

        void updateItem(int position, Item item) {
            mItems.set(position, item);
            notifyDataSetChanged();
        }

        void removeItem(int position) {
            mItems.remove(position);
            notifyDataSetChanged();
        }
    }

    public interface Callback {
        void onClick(ApplyPopup applyPopup, int position);
    }
}
