package com.dm.wallpaper.board.adapters;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.activities.WallpaperBoardPreviewActivity;
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
import com.kogitune.activitytransition.ActivityTransitionLauncher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

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

public class LatestAdapter extends RecyclerView.Adapter<LatestAdapter.ViewHolder> {

    private final Context mContext;
    private List<Wallpaper> mWallpapers;
    private final DisplayImageOptions.Builder mOptions;

    public LatestAdapter(@NonNull Context context, @NonNull List<Wallpaper> wallpapers) {
        mContext = context;
        mWallpapers = wallpapers;

        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(true);
        mOptions.displayer(new FadeInBitmapDisplayer(700));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_latest_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wallpaper wallpaper = mWallpapers.get(position);
        holder.name.setText(wallpaper.getName());
        holder.author.setText(wallpaper.getAuthor());

        if (mContext.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
            holder.download.setVisibility(View.VISIBLE);
        } else {
            holder.download.setVisibility(View.GONE);
        }

        setFavorite(holder.favorite, Color.WHITE, position, false);
        resetImageViewHeight(holder.image, wallpaper.getDimensions());

        ImageLoader.getInstance().displayImage(
                wallpaper.getThumbUrl(),
                new ImageViewAware(holder.image),
                mOptions.build(),
                ImageConfig.getBigThumbnailSize(),
                new SimpleImageLoadingListener() {

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

                        holder.card.setCardBackgroundColor(color);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (loadedImage != null && wallpaper.getColor() == 0) {
                            Palette.from(loadedImage).generate(palette -> {
                                int vibrant = ColorHelper.getAttributeColor(
                                        mContext, R.attr.card_background);
                                int color = palette.getVibrantColor(vibrant);
                                if (color == vibrant)
                                    color = palette.getMutedColor(vibrant);
                                holder.card.setCardBackgroundColor(color);

                                wallpaper.setColor(color);
                                Database.get(mContext).updateWallpaper(wallpaper);
                            });
                        }
                    }
                },
                null);
    }

    @Override
    public int getItemCount() {
        return mWallpapers.size();
    }

    private void resetImageViewHeight(@NonNull ImageView imageView, ImageSize imageSize) {
        if (imageSize == null) imageSize = new ImageSize(400, 300);

        int width = WindowHelper.getScreenSize(mContext).x;
        int spanCount = mContext.getResources().getInteger(R.integer.latest_wallpapers_column_count);
        if (spanCount > 1) {
            width = width/spanCount;
        }
        double scaleFactor = (double) width / (double) imageSize.getWidth();
        double measuredHeight = (double) imageSize.getHeight() * scaleFactor;
        imageView.getLayoutParams().height = Double.valueOf(measuredHeight).intValue();
        imageView.requestLayout();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R2.id.image)
        ImageView image;
        @BindView(R2.id.name)
        TextView name;
        @BindView(R2.id.author)
        TextView author;
        @BindView(R2.id.favorite)
        ImageView favorite;
        @BindView(R2.id.download)
        ImageView download;
        @BindView(R2.id.apply)
        ImageView apply;
        @BindView(R2.id.card)
        CardView card;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mContext.getResources().getInteger(R.integer.latest_wallpapers_column_count) == 1) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams params =
                            (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.leftMargin = 0;
                    params.rightMargin = 0;
                    params.topMargin = 0;
                    params.bottomMargin = 0;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(0);
                    }
                }
            } else {
                setCardViewToFlat(card);
            }

            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0f);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StateListAnimator stateListAnimator = AnimatorInflater
                        .loadStateListAnimator(mContext, R.animator.card_lift);
                card.setStateListAnimator(stateListAnimator);
            }

            if (mContext.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                download.setImageDrawable(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_toolbar_download, Color.WHITE));
                download.setOnClickListener(this);
            }

            apply.setImageDrawable(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_apply_options, Color.WHITE));

            card.setOnClickListener(this);
            favorite.setOnClickListener(this);
            apply.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.favorite) {
                if (position < 0 || position > mWallpapers.size()) return;

                boolean isFavorite = mWallpapers.get(position).isFavorite();
                Database.get(mContext).favoriteWallpaper(
                        mWallpapers.get(position).getId(), !isFavorite);

                mWallpapers.get(position).setFavorite(!isFavorite);
                setFavorite(favorite, name.getCurrentTextColor(), position, true);

                CafeBar.builder(mContext)
                        .theme(new CafeBarTheme.Custom(ColorHelper.getAttributeColor(
                                mContext, R.attr.card_background)))
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
            } else if (id == R.id.download) {
                if (PermissionHelper.isStorageGranted(mContext)) {
                    WallpaperDownloader.prepare(mContext)
                            .wallpaper(mWallpapers.get(position))
                            .start();
                    return;
                }

                PermissionHelper.requestStorage(mContext);
            } else if (id == R.id.apply) {
                Popup popup = Popup.Builder(mContext)
                        .to(apply)
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
                            }
                            applyPopup.dismiss();
                        })
                        .build();

                if (mContext.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                    popup.removeItem(popup.getItems().size() - 1);
                }

                popup.show();
            } else if (id == R.id.card) {
                if (WallpapersAdapter.sIsClickable) {
                    WallpapersAdapter.sIsClickable = false;
                    try {
                        Bitmap bitmap = null;
                        if (image.getDrawable() != null) {
                            bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                        }

                        final Intent intent = new Intent(mContext, WallpaperBoardPreviewActivity.class);
                        intent.putExtra(Extras.EXTRA_ID, mWallpapers.get(position).getId());

                        ActivityTransitionLauncher.with((AppCompatActivity) mContext)
                                .from(image, Extras.EXTRA_IMAGE)
                                .image(bitmap)
                                .launch(intent);
                    } catch (Exception e) {
                        WallpapersAdapter.sIsClickable = true;
                    }
                }
            }
        }
    }
    public void setWallpapers(@NonNull List<Wallpaper> wallpapers) {
        mWallpapers = wallpapers;
        notifyDataSetChanged();
    }


    private void setFavorite(@NonNull ImageView imageView, @ColorInt int color, int position, boolean animate) {
        if (position < 0 || position > mWallpapers.size()) return;

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
}
