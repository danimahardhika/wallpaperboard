package com.dm.wallpaper.board.adapters;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.activities.WallpaperBoardPreviewActivity;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.items.PopupItem;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.tasks.WallpaperApplyTask;
import com.dm.wallpaper.board.utils.Popup;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.WallpaperDownloader;
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

import static com.dm.wallpaper.board.helpers.ViewHelper.setCardViewToFlat;

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

    private final boolean mIsFavoriteMode;

    public WallpapersAdapter(@NonNull Context context, @NonNull List<Wallpaper> wallpapers,
                             boolean isFavoriteMode, boolean isSearchMode) {
        mContext = context;
        mWallpapers = wallpapers;
        mIsFavoriteMode = isFavoriteMode;
        WallpaperBoardApplication.sIsClickable = true;

        if (isSearchMode) {
            mWallpapersAll = new ArrayList<>();
            mWallpapersAll.addAll(mWallpapers);
        }

        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(true);
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
        Wallpaper wallpaper = mWallpapers.get(position);
        holder.name.setText(wallpaper.getName());

        if (wallpaper.getAuthor() == null) {
            holder.author.setVisibility(View.GONE);
        } else {
            holder.author.setText(wallpaper.getAuthor());
            holder.author.setVisibility(View.VISIBLE);
        }

        int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
        if (wallpaper.getColor() != 0) {
            color = ColorHelper.getTitleTextColor(wallpaper.getColor());
        }

        setFavorite(holder.favorite, color, position, false);

        ImageLoader.getInstance().displayImage(wallpaper.getThumbUrl(), new ImageViewAware(holder.image),
                mOptions.build(), ImageConfig.getThumbnailSize(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        int color;
                        if (wallpaper.getColor() == 0) {
                            color = ColorHelper.getAttributeColor(
                                    mContext, R.attr.card_background);
                        } else {
                            color = wallpaper.getColor();
                        }

                        int text = ColorHelper.getTitleTextColor(color);
                        holder.name.setTextColor(text);
                        holder.author.setTextColor(ColorHelper.setColorAlpha(text, 0.7f));
                        holder.card.setCardBackgroundColor(color);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (loadedImage != null && wallpaper.getColor() == 0) {
                            Palette.from(loadedImage).generate(palette -> {
                                if (mContext == null) return;
                                if (((Activity) mContext).isFinishing()) return;
                                
                                int vibrant = ColorHelper.getAttributeColor(
                                        mContext, R.attr.card_background);
                                int color = palette.getVibrantColor(vibrant);
                                if (color == vibrant)
                                    color = palette.getMutedColor(vibrant);
                                holder.card.setCardBackgroundColor(color);

                                int text = ColorHelper.getTitleTextColor(color);
                                holder.name.setTextColor(text);
                                holder.author.setTextColor(ColorHelper.setColorAlpha(text, 0.7f));

                                wallpaper.setColor(color);
                                setFavorite(holder.favorite, text, holder.getAdapterPosition(), false);

                                Database.get(mContext).updateWallpaper(wallpaper);
                            });
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
            setCardViewToFlat(card);

            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0f);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StateListAnimator stateListAnimator = AnimatorInflater
                        .loadStateListAnimator(mContext, R.animator.card_lift_long);
                card.setStateListAnimator(stateListAnimator);
            }

            card.setOnClickListener(this);
            card.setOnLongClickListener(this);
            favorite.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.card) {
                if (WallpaperBoardApplication.sIsClickable) {
                    WallpaperBoardApplication.sIsClickable = false;
                    try {
                        Bitmap bitmap = null;
                        if (image.getDrawable() != null) {
                            bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                        }

                        final Intent intent = new Intent(mContext, WallpaperBoardPreviewActivity.class);
                        intent.putExtra(Extras.EXTRA_URL, mWallpapers.get(position).getUrl());
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        ActivityTransitionLauncher.with((AppCompatActivity) mContext)
                                .from(image, Extras.EXTRA_IMAGE)
                                .image(bitmap)
                                .launch(intent);
                    } catch (Exception e) {
                        WallpaperBoardApplication.sIsClickable = true;
                    }
                }
            } else if (id == R.id.favorite) {
                if (position < 0 || position > mWallpapers.size()) return;

                boolean isFavorite = mWallpapers.get(position).isFavorite();
                Database.get(mContext).favoriteWallpaper(
                        mWallpapers.get(position).getUrl(), !isFavorite);

                if (mIsFavoriteMode) {
                    mWallpapers.remove(position);
                    notifyItemRemoved(position);
                    return;
                }

                mWallpapers.get(position).setFavorite(!isFavorite);
                setFavorite(favorite, name.getCurrentTextColor(), position, true);

                CafeBar.builder(mContext)
                        .theme(Preferences.get(mContext).isDarkTheme() ? CafeBarTheme.LIGHT : CafeBarTheme.DARK)
                        .fitSystemWindow()
                        .floating(true)
                        .typeface(TypefaceHelper.getRegular(mContext), TypefaceHelper.getBold(mContext))
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
            if (id == R.id.card) {
                if (position < 0 || position > mWallpapers.size()) {
                    return false;
                }

                Popup popup = Popup.Builder(mContext)
                        .to(favorite)
                        .list(PopupItem.getApplyItems(mContext))
                        .callback((applyPopup, i) -> {
                            PopupItem item = applyPopup.getItems().get(i);
                            if (item.getType() == PopupItem.Type.WALLPAPER_CROP) {
                                Preferences.get(mContext).setCropWallpaper(!item.getCheckboxValue());
                                item.setCheckboxValue(Preferences.get(mContext).isCropWallpaper());

                                applyPopup.updateItem(i, item);
                                return;
                            } else if (item.getType() == PopupItem.Type.LOCKSCREEN) {
                                WallpaperApplyTask.prepare(mContext)
                                        .wallpaper(mWallpapers.get(position))
                                        .to(WallpaperApplyTask.Apply.LOCKSCREEN)
                                        .start(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else if (item.getType() == PopupItem.Type.HOMESCREEN) {
                                WallpaperApplyTask.prepare(mContext)
                                        .wallpaper(mWallpapers.get(position))
                                        .to(WallpaperApplyTask.Apply.HOMESCREEN)
                                        .start(AsyncTask.THREAD_POOL_EXECUTOR);

                            } else if (item.getType() == PopupItem.Type.HOMESCREEN_LOCKSCREEN) {
                                WallpaperApplyTask.prepare(mContext)
                                        .wallpaper(mWallpapers.get(position))
                                        .to(WallpaperApplyTask.Apply.HOMESCREEN_LOCKSCREEN)
                                        .start(AsyncTask.THREAD_POOL_EXECUTOR);

                            } else if (item.getType() == PopupItem.Type.DOWNLOAD) {
                                if (PermissionHelper.isStorageGranted(mContext)) {
                                    WallpaperDownloader.prepare(mContext)
                                            .wallpaper(mWallpapers.get(position))
                                            .start();
                                } else {
                                    PermissionHelper.requestStorage(mContext);
                                }
                            }
                            applyPopup.dismiss();
                        })
                        .build();

                popup.show();
                return true;
            }
            return false;
        }
    }

    private void setFavorite(@NonNull ImageView imageView, @ColorInt int color, int position, boolean animate) {
        if (position < 0 || position > mWallpapers.size()) return;

        if (mIsFavoriteMode) {
            imageView.setImageDrawable(DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_toolbar_love, color));
            return;
        }

        boolean isFavorite = mWallpapers.get(position).isFavorite();

        if (animate) {
            AnimationHelper.show(imageView)
                    .interpolator(new LinearOutSlowInInterpolator())
                    .callback(new AnimationHelper.Callback() {
                        @Override
                        public void onAnimationStart() {
                            imageView.setImageDrawable(DrawableHelper.getTintedDrawable(mContext,
                                    isFavorite ? R.drawable.ic_toolbar_love : R.drawable.ic_toolbar_unlove, color));
                        }

                        @Override
                        public void onAnimationEnd() {

                        }
                    })
                    .start();
            return;
        }

        imageView.setImageDrawable(DrawableHelper.getTintedDrawable(mContext,
                isFavorite ? R.drawable.ic_toolbar_love : R.drawable.ic_toolbar_unlove, color));
    }

    public void setWallpapers(@NonNull List<Wallpaper> wallpapers) {
        mWallpapers = wallpapers;
        notifyDataSetChanged();
    }

    public List<Wallpaper> getWallpapers() {
        return mWallpapers;
    }

    public void clearItems() {
        int size = mWallpapers.size();
        mWallpapers.clear();
        notifyItemRangeRemoved(0, size);
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
