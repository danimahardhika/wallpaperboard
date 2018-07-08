package com.dm.wallpaper.board.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.items.Credit;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

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

public class CreditsAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Credit> mCredits;
    private final DisplayImageOptions.Builder mOptions;

    public CreditsAdapter(@NonNull Context context, @NonNull List<Credit> credits) {
        mContext = context;
        mCredits = credits;

        int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
        Drawable drawable = DrawableHelper.getTintedDrawable(
                mContext, R.drawable.ic_toolbar_default_profile, color);

        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(true);
        mOptions.showImageForEmptyUri(drawable);
        mOptions.showImageOnFail(drawable);
        mOptions.showImageOnLoading(drawable);
        mOptions.displayer(new CircleBitmapDisplayer());
    }

    @Override
    public int getCount() {
        return mCredits.size();
    }

    @Override
    public Credit getItem(int position) {
        return mCredits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_credits_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Credit credit = mCredits.get(position);
        holder.title.setText(credit.getName());
        holder.subtitle.setText(credit.getContribution());
        holder.container.setOnClickListener(view1 -> {
            String link = credit.getLink();
            if (URLUtil.isValidUrl(link)) {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                } catch (ActivityNotFoundException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }
        });

        if (credit.getContribution().length() == 0) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            holder.subtitle.setVisibility(View.VISIBLE);
        }

        ImageLoader.getInstance().displayImage(credit.getImage(),
                new ImageViewAware(holder.image), mOptions.build(),
                new ImageSize(144, 144), null, null);
        return view;
    }

    class ViewHolder {

        @BindView(R2.id.container)
        LinearLayout container;
        @BindView(R2.id.image)
        ImageView image;
        @BindView(R2.id.title)
        TextView title;
        @BindView(R2.id.subtitle)
        TextView subtitle;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
            ViewCompat.setBackground(image, DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_circle, ColorHelper.setColorAlpha(color, 0.4f)));
        }
    }
}
