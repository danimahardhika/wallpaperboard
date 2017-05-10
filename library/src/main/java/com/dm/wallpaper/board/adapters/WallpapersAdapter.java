package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.activities.WallpaperBoardPreviewActivity;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.fragments.FavoritesFragment;
import com.dm.wallpaper.board.fragments.WallpaperSearchFragment;
import com.dm.wallpaper.board.fragments.WallpapersFragment;
import com.dm.wallpaper.board.fragments.dialogs.WallpaperOptionsFragment;
import com.dm.wallpaper.board.helpers.ColorHelper;
import com.dm.wallpaper.board.helpers.DrawableHelper;
import com.dm.wallpaper.board.helpers.WallpaperHelper;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.listeners.WallpaperListener;
import com.dm.wallpaper.board.utils.views.HeaderView;

import com.kogitune.activitytransition.ActivityTransitionLauncher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.ViewHolder> {

    private final Context mContext;
    private final DisplayImageOptions.Builder mOptions;
    private List<Wallpaper> mWallpapers;
    private List<Wallpaper> mWallpapersAll;

    private int mLastSelectedPosition = -1;
    private final boolean mIsFavoriteMode;
    public static boolean sIsClickable = true;

    public WallpapersAdapter(@NonNull Context context, @NonNull List<Wallpaper> wallpapers,
                             boolean isFavoriteMode, boolean isSearchMode) {
        mContext = context;
        mWallpapers = wallpapers;
        mIsFavoriteMode = isFavoriteMode;

        if (isSearchMode) {
            mWallpapersAll = new ArrayList<>();
            mWallpapersAll.addAll(mWallpapers);
        }

        int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
        Drawable loading = DrawableHelper.getDefaultImage(
                mContext, R.drawable.ic_default_image_loading, color,
                mContext.getResources().getDimensionPixelSize(R.dimen.default_image_padding));
        Drawable failed = DrawableHelper.getDefaultImage(
                mContext, R.drawable.ic_default_image_failed, color,
                mContext.getResources().getDimensionPixelSize(R.dimen.default_image_padding));
        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(true);
        mOptions.showImageForEmptyUri(failed);
        mOptions.showImageOnFail(failed);
        mOptions.showImageOnLoading(loading);
        mOptions.displayer(new FadeInBitmapDisplayer(700));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_wallpapers_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mWallpapers.get(position).getName());
        holder.author.setText(mWallpapers.get(position).getAuthor());

        setFavorite(holder.favorite, ColorHelper.getAttributeColor(
                mContext, android.R.attr.textColorPrimary), position);

        String url = WallpaperHelper.getThumbnailUrl(mContext,
                mWallpapers.get(position).getUrl(),
                mWallpapers.get(position).getThumbUrl());

        ImageLoader.getInstance().displayImage(url, new ImageViewAware(holder.image),
                mOptions.build(), ImageConfig.getThumbnailSize(mContext), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        if (Preferences.get(mContext).isColoredWallpapersCard()) {
                            int vibrant = ColorHelper.getAttributeColor(
                                    mContext, R.attr.card_background);
                            holder.card.setCardBackgroundColor(vibrant);
                            int primary = ColorHelper.getAttributeColor(
                                    mContext, android.R.attr.textColorPrimary);
                            holder.name.setTextColor(primary);
                            holder.author.setTextColor(primary);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (Preferences.get(mContext).isColoredWallpapersCard()) {
                            if (loadedImage != null) {
                                Palette.from(loadedImage).generate(palette -> {
                                    int vibrant = ColorHelper.getAttributeColor(
                                            mContext, R.attr.card_background);
                                    int color = palette.getVibrantColor(vibrant);
                                    if (color == vibrant)
                                        color = palette.getMutedColor(vibrant);
                                    holder.card.setCardBackgroundColor(color);
                                    int text = ColorHelper.getTitleTextColor(color);
                                    holder.name.setTextColor(text);
                                    holder.author.setTextColor(text);
                                    setFavorite(holder.favorite, text, holder.getAdapterPosition());
                                });
                            }
                        }
                    }
                }, null);
    }

    @Override
    public int getItemCount() {
        return mWallpapers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R2.id.card)
        CardView card;
        @BindView(R2.id.container)
        LinearLayout container;
        @BindView(R2.id.image)
        HeaderView image;
        @BindView(R2.id.name)
        TextView name;
        @BindView(R2.id.author)
        TextView author;
        @BindView(R2.id.favorite)
        ImageView favorite;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0);
            }

            if (mContext.getResources().getBoolean(R.bool.enable_wallpaper_card_rounded_corner)) {
                card.setRadius(mContext.getResources().getDimensionPixelSize(R.dimen.card_corner_radius));
            }

            container.setOnClickListener(this);
            container.setOnLongClickListener(this);
            favorite.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.container) {
                if (sIsClickable) {
                    sIsClickable = false;
                    try {
                        final Intent intent = new Intent(mContext, WallpaperBoardPreviewActivity.class);
                        intent.putExtra(Extras.EXTRA_URL, mWallpapers.get(position).getUrl());
                        intent.putExtra(Extras.EXTRA_AUTHOR, mWallpapers.get(position).getAuthor());
                        intent.putExtra(Extras.EXTRA_NAME, mWallpapers.get(position).getName());

                        ActivityTransitionLauncher.with((AppCompatActivity) mContext)
                                .from(image, Extras.EXTRA_IMAGE)
                                .image(((BitmapDrawable) image.getDrawable()).getBitmap())
                                .launch(intent);
                    } catch (Exception e) {
                        sIsClickable = true;
                    }

                    FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                    if (fm != null) {
                        Fragment fragment = fm.findFragmentById(R.id.container);
                        if (fragment != null) {
                            if (fragment instanceof WallpapersFragment ||
                                    fragment instanceof FavoritesFragment ||
                                    fragment instanceof WallpaperSearchFragment) {
                                WallpaperListener listener = (WallpaperListener) fragment;
                                listener.onWallpaperSelected(position);
                            }
                        }
                    }
                }
            } else if (id == R.id.favorite) {
                if (position < 0 || position > mWallpapers.size()) return;

                if (mIsFavoriteMode) {
                    Database database = new Database(mContext);
                    database.favoriteWallpaper(mWallpapers.get(position).getId(),
                            !mWallpapers.get(position).isFavorite());
                    mWallpapers.remove(position);
                    notifyItemRemoved(position);
                    return;
                }

                mWallpapers.get(position).setFavorite(!mWallpapers.get(position).isFavorite());
                setFavorite(favorite, name.getCurrentTextColor(), position);

                CafeBar.builder(mContext)
                        .theme(new CafeBarTheme.Custom(ColorHelper.getAttributeColor(
                                mContext, R.attr.card_background)))
                        .fitSystemWindow()
                        .typeface("Font-Regular.ttf", "Font-Bold.ttf")
                        .content(String.format(
                                mContext.getResources().getString(mWallpapers.get(position).isFavorite() ?
                                        R.string.wallpaper_favorite_added : R.string.wallpaper_favorite_removed),
                                mWallpapers.get(position).getName()))
                        .icon(mWallpapers.get(position).isFavorite() ?
                                R.drawable.ic_toolbar_love : R.drawable.ic_toolbar_unlove)
                        .show();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.container) {
                if (position < 0 || position > mWallpapers.size()) {
                    mLastSelectedPosition = -1;
                    return false;
                }

                mLastSelectedPosition = position;
                WallpaperOptionsFragment.showWallpaperOptionsDialog(
                        ((AppCompatActivity) mContext).getSupportFragmentManager(),
                        mWallpapers.get(position).getUrl(),
                        mWallpapers.get(position).getName());
                return true;
            }
            return false;
        }
    }

    private void setFavorite(@NonNull ImageView imageView, @ColorInt int color, int position) {
        if (position < 0 || position > mWallpapers.size()) return;

        if (mIsFavoriteMode) {
            if (!Preferences.get(mContext).isColoredWallpapersCard())
                color = ContextCompat.getColor(mContext, R.color.favoriteColor);
            imageView.setImageDrawable(DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_toolbar_love, color));
            return;
        }

        boolean isFavorite = mWallpapers.get(position).isFavorite();
        if (!Preferences.get(mContext).isColoredWallpapersCard() && isFavorite)
            color = ContextCompat.getColor(mContext, R.color.favoriteColor);
        imageView.setImageDrawable(DrawableHelper.getTintedDrawable(mContext,
                isFavorite ? R.drawable.ic_toolbar_love : R.drawable.ic_toolbar_unlove, color));
        Database database = new Database(mContext);
        database.favoriteWallpaper(mWallpapers.get(position).getId(), isFavorite);
    }

    public void filter() {
        Database database = new Database(mContext);
        mWallpapers = database.getFilteredWallpapers();
        notifyDataSetChanged();
    }

    public void downloadLastSelectedWallpaper() {
        if (mLastSelectedPosition < 0 || mLastSelectedPosition > mWallpapers.size()) return;

        WallpaperHelper.downloadWallpaper(mContext,
                ColorHelper.getAttributeColor(mContext, R.attr.colorAccent),
                mWallpapers.get(mLastSelectedPosition).getUrl(),
                mWallpapers.get(mLastSelectedPosition).getName());
    }

    public void search(String string) {
        String query = string.toLowerCase(Locale.getDefault()).trim();
        mWallpapers.clear();
        if (query.length() == 0) mWallpapers.addAll(mWallpapersAll);
        else {
            for (int i = 0; i < mWallpapersAll.size(); i++) {
                Wallpaper wallpaper = mWallpapersAll.get(i);
                String name = wallpaper.getName().toLowerCase(Locale.getDefault());
                String author = wallpaper.getAuthor().toLowerCase(Locale.getDefault());
                if (name.contains(query) || author.contains(query)) {
                    mWallpapers.add(wallpaper);
                }
            }
        }
        notifyDataSetChanged();
    }
}
