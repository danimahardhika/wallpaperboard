package com.dm.wallpaper.board.adapters;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.activities.WallpaperBoardBrowserActivity;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
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

public class WallpaperDetailsCategoryAdapter extends RecyclerView.Adapter<WallpaperDetailsCategoryAdapter.ViewHolder> {

    private final Context mContext;
    private List<Category> mCategories;
    private final DisplayImageOptions.Builder mOptions;

    public WallpaperDetailsCategoryAdapter(@NonNull Context context, @NonNull List<Category> categories) {
        mContext = context;
        mCategories = categories;

        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(true);
        mOptions.displayer(new FadeInBitmapDisplayer(700));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.activity_wallpaper_preview_category_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Category category = mCategories.get(position);
        holder.title.setText(category.getName());

        ImageLoader.getInstance().displayImage(
                category.getThumbUrl(),
                new ImageViewAware(holder.image),
                mOptions.build(),
                ImageConfig.getBigThumbnailSize(),
                null,
                null);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R2.id.image)
        ImageView image;
        @BindView(R2.id.title)
        TextView title;
        @BindView(R2.id.card)
        CardView card;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StateListAnimator stateListAnimator = AnimatorInflater
                        .loadStateListAnimator(mContext, R.animator.card_lift);
                card.setStateListAnimator(stateListAnimator);
            }
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position < 0 || position > mCategories.size()) return;

            Intent intent = new Intent(mContext, WallpaperBoardBrowserActivity.class);
            intent.putExtra(Extras.EXTRA_FRAGMENT_ID, Extras.ID_CATEGORY_WALLPAPERS);
            intent.putExtra(Extras.EXTRA_CATEGORY, mCategories.get(position).getName());
            intent.putExtra(Extras.EXTRA_COUNT, mCategories.get(position).getCount());

            mContext.startActivity(intent);
        }
    }
}
