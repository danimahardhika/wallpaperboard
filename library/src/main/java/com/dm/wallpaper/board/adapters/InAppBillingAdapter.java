package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.items.InAppBilling;

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

public class InAppBillingAdapter extends BaseAdapter {

    private final Context mContext;
    private final InAppBilling[] mInAppBillings;

    private int mSelectedPosition = 0;

    public InAppBillingAdapter(@NonNull Context context, @NonNull InAppBilling[] inAppBillings) {
        mContext = context;
        mInAppBillings = inAppBillings;
    }

    @Override
    public int getCount() {
        return mInAppBillings.length;
    }

    @Override
    public InAppBilling getItem(int position) {
        return mInAppBillings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_inappbilling_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.radio.setChecked(mSelectedPosition == position);

        String product = mInAppBillings[position].getPrice() +" - "+
                mInAppBillings[position].getProductName();
        holder.name.setText(product);

        holder.container.setOnClickListener(v -> {
            mSelectedPosition = position;
            notifyDataSetChanged();
        });
        return view;
    }

    class ViewHolder {

        @BindView(R2.id.radio)
        AppCompatRadioButton radio;
        @BindView(R2.id.name)
        TextView name;
        @BindView(R2.id.container)
        LinearLayout container;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public InAppBilling getSelectedProduct() {
        return mInAppBillings[mSelectedPosition];
    }

}
