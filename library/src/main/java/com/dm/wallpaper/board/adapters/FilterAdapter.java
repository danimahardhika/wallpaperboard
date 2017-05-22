package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.items.Category;

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

public class FilterAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Category> mCategories;
    private final boolean mIsMuzei;

    public FilterAdapter(@NonNull Context context, @NonNull List<Category> categories, boolean isMuzei) {
        mContext = context;
        mCategories = categories;
        mIsMuzei = isMuzei;
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public Category getItem(int position) {
        return mCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_filter_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Category category = mCategories.get(position);
        holder.title.setText(category.getName());
        holder.checkBox.setChecked(mIsMuzei ?
                category.isMuzeiSelected() :
                category.isSelected());
        String count = category.getCount() > 99 ? "99+" : category.getCount() +"";
        holder.counter.setText(count);
        holder.container.setOnClickListener(v -> {
            Database database = Database.get(mContext);
            if (mIsMuzei) {
                database.selectCategoryForMuzei(category.getId(),
                        !category.isMuzeiSelected());
                category.setMuzeiSelected(
                        !category.isMuzeiSelected());
            } else {
                database.selectCategory(category.getId(),
                        !category.isSelected());
                mCategories.get(position).setSelected(
                        !category.isSelected());
            }

            notifyDataSetChanged();
        });
        return view;
    }

    class ViewHolder {

        @BindView(R2.id.container)
        LinearLayout container;
        @BindView(R2.id.checkbox)
        AppCompatCheckBox checkBox;
        @BindView(R2.id.title)
        TextView title;
        @BindView(R2.id.counter)
        TextView counter;

        ViewHolder(@NonNull View view) {
            ButterKnife.bind(this, view);
            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            ViewCompat.setBackground(counter, DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_circle, color));
            counter.setTextColor(ColorHelper.getTitleTextColor(color));
        }
    }
}
