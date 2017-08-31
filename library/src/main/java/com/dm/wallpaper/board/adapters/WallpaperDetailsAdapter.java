package com.dm.wallpaper.board.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarDuration;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.ColorPalette;
import com.dm.wallpaper.board.items.WallpaperProperty;
import com.dm.wallpaper.board.utils.LogUtil;

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

public class WallpaperDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<WallpaperProperty> mProperties;
    private ColorPalette mPalette;
    private List<Category> mCategories;
    private final Context mContext;

    private static final int TYPE_DETAILS = 0;
    private static final int TYPE_PALETTE_HEADER = 1;
    private static final int TYPE_PALETTE = 2;
    private static final int TYPE_CATEGORY = 3;

    public WallpaperDetailsAdapter(@NonNull Context context,
                                   @NonNull List<WallpaperProperty> properties,
                                   @NonNull ColorPalette palette,
                                   @NonNull List<Category> categories) {
        mContext = context;
        mProperties = properties;
        mPalette = palette;
        mCategories = categories;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_DETAILS) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_wallpaper_preview_details, parent, false);
            return new PropertyViewHolder(view);
        } else if (viewType == TYPE_PALETTE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_wallpaper_preview_palette_header, parent, false);
            return new PaletteHeaderViewHolder(view);
        } else if (viewType == TYPE_PALETTE) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_wallpaper_preview_palette, parent, false);
            return new PaletteViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.activity_wallpaper_preview_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder.itemView != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(getItemViewType(position) == TYPE_PALETTE_HEADER ||
                        getItemViewType(position) == TYPE_CATEGORY);
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }

        if (holder.getItemViewType() == TYPE_DETAILS) {
            PropertyViewHolder propertyViewHolder = (PropertyViewHolder) holder;

            WallpaperProperty property = mProperties.get(position);
            propertyViewHolder.title.setText(property.getTitle());
            if (property.getIcon() != 0) {
                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
                Drawable drawable = DrawableHelper.getTintedDrawable(mContext, property.getIcon(), color);
                propertyViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }
        } else if (holder.getItemViewType() == TYPE_PALETTE_HEADER) {
            PaletteHeaderViewHolder paletteHeaderViewHolder = (PaletteHeaderViewHolder) holder;

            paletteHeaderViewHolder.container.setVisibility(View.VISIBLE);
            if (mPalette.size() == 0) {
                paletteHeaderViewHolder.container.setVisibility(View.GONE);
            }
        } else if (holder.getItemViewType() == TYPE_PALETTE) {
            PaletteViewHolder paletteViewHolder = (PaletteViewHolder) holder;

            int finalPosition = position - mProperties.size() - 1;
            paletteViewHolder.title.setText(mPalette.getHex(finalPosition));

            Drawable drawable = DrawableHelper.getTintedDrawable(mContext,
                    R.drawable.ic_toolbar_details_palette_color, mPalette.get(finalPosition));
            paletteViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        } else if (holder.getItemViewType() == TYPE_CATEGORY) {
            CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;

            categoryViewHolder.container.setVisibility(View.VISIBLE);
            if (mCategories.size() == 0) {
                categoryViewHolder.container.setVisibility(View.GONE);
                return;
            }

            categoryViewHolder.recyclerView.setAdapter(new WallpaperDetailsCategoryAdapter(mContext, mCategories));
            int spanCount = mContext.getResources().getInteger(R.integer.wallpaper_details_column_count);
            ViewHelper.resetSpanCount(categoryViewHolder.recyclerView, spanCount);
        }
    }

    @Override
    public int getItemCount() {
        return mProperties.size() + mPalette.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mProperties.size()) {
            return TYPE_DETAILS;
        } else if (position == mProperties.size()) {
            return TYPE_PALETTE_HEADER;
        } else if (position > mProperties.size() && position < (getItemCount() - 1)) {
            return TYPE_PALETTE;
        }
        return TYPE_CATEGORY;
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R2.id.title)
        TextView title;

        PropertyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            title.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position < 0 || position > mProperties.size()) return;

            showCafeBar(mProperties.get(position).getDesc(), "", false);
        }
    }

    class PaletteHeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.container)
        LinearLayout container;

        PaletteHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            TextView title = ButterKnife.findById(itemView, R.id.title);
            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            Drawable drawable = DrawableHelper.getTintedDrawable(mContext,
                    R.drawable.ic_toolbar_details_palette, color);
            title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }

    class PaletteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R2.id.title)
        TextView title;

        PaletteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            title.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition() - mProperties.size() - 1;
            if (position < 0 || position > mPalette.size()) return;

            String content = mContext.getResources().getString(R.string.wallpaper_property_color,
                    mPalette.getHex(position));
            showCafeBar(content, mPalette.getHex(position), true);
        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.title)
        TextView title;
        @BindView(R2.id.recyclerview)
        RecyclerView recyclerView;
        @BindView(R2.id.container)
        LinearLayout container;

        CategoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            Drawable drawable = DrawableHelper.getTintedDrawable(mContext,
                    R.drawable.ic_toolbar_details_category, color);
            title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new GridLayoutManager(mContext,
                    mContext.getResources().getInteger(R.integer.wallpaper_details_column_count)));
            recyclerView.setNestedScrollingEnabled(false);
        }
    }

    private void showCafeBar(String title, String content, boolean showCopy) {
        CafeBar.Builder builder = CafeBar.builder(mContext)
                .theme(new CafeBarTheme.Custom(ColorHelper.getAttributeColor(
                        mContext, R.attr.card_background)))
                .typeface(TypefaceHelper.getRegular(mContext), TypefaceHelper.getBold(mContext))
                .content(title)
                .duration(CafeBarDuration.SHORT.getDuration())
                .floating(true);

        if (showCopy) {
            builder.neutralText(R.string.copy)
                    .neutralColor(ColorHelper.getAttributeColor(mContext, R.attr.colorAccent))
                    .onNeutral(cafeBar -> {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("content", content);
                        clipboard.setPrimaryClip(clip);
                    });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = mContext.getResources().getBoolean(R.bool.android_helpers_tablet_mode);
            if (!tabletMode && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
               builder.fitSystemWindow();
            }
        }

        CafeBar cafeBar = builder.build();
        cafeBar.show();
    }

    public void setWallpaperProperties(@NonNull List<WallpaperProperty> properties) {
        mProperties = properties;
        notifyDataSetChanged();
    }

    public void setColorPalette(@NonNull ColorPalette palette) {
        mPalette = palette;
        notifyDataSetChanged();
    }

    public void setCategories(@NonNull List<Category> categories) {
        mCategories = categories;
        notifyDataSetChanged();
    }
}
