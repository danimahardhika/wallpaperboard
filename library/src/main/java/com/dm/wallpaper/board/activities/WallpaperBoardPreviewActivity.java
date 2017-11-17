package com.dm.wallpaper.board.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.Transition;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.WallpaperDetailsAdapter;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.helpers.TapIntroHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.items.ColorPalette;
import com.dm.wallpaper.board.items.PopupItem;
import com.dm.wallpaper.board.items.Wallpaper;
import com.dm.wallpaper.board.items.WallpaperProperty;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.tasks.LocalFavoritesRestoreTask;
import com.dm.wallpaper.board.tasks.WallpaperApplyTask;
import com.dm.wallpaper.board.tasks.WallpaperPropertiesLoaderTask;
import com.dm.wallpaper.board.tasks.WallpaperPaletteLoaderTask;
import com.dm.wallpaper.board.utils.Popup;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.Tooltip;
import com.dm.wallpaper.board.utils.WallpaperDownloader;
import com.kogitune.activitytransition.ActivityTransition;
import com.kogitune.activitytransition.ExitActivityTransition;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.senab.photoview.PhotoViewAttacher;

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

public class WallpaperBoardPreviewActivity extends AppCompatActivity implements View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, SlidingUpPanelLayout.PanelSlideListener,
        WallpaperPropertiesLoaderTask.Callback, WallpaperPaletteLoaderTask.Callback, View.OnLongClickListener {

    @BindView(R2.id.wallpaper)
    ImageView mImageView;
    @BindView(R2.id.progress)
    ProgressBar mProgress;
    @BindView(R2.id.back)
    ImageView mBack;
    @BindView(R2.id.bottom_panel)
    RelativeLayout mBottomPanel;
    @BindView(R2.id.name)
    TextView mName;
    @BindView(R2.id.author)
    TextView mAuthor;
    @BindView(R2.id.menu_preview)
    ImageView mMenuPreview;
    @BindView(R2.id.menu_save)
    ImageView mMenuSave;
    @BindView(R2.id.menu_apply)
    ImageView mMenuApply;
    @BindView(R2.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R2.id.sliding_layout)
    SlidingUpPanelLayout mSlidingLayout;

    private Runnable mRunnable;
    private Handler mHandler;
    private PhotoViewAttacher mAttacher;
    private ExitActivityTransition mExitTransition;
    private Tooltip mTooltip;

    private Wallpaper mWallpaper;
    private List<WallpaperProperty> mProperties;
    private ColorPalette mPalette;
    private List<Category> mCategories;

    private boolean mIsEnter = true;
    private boolean mIsResumed = false;

    private boolean mIsBottomPanelDragged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.WallpaperThemeDark : R.style.WallpaperTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);
        ButterKnife.bind(this);

        mProgress.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("#CCFFFFFF"), PorterDuff.Mode.SRC_IN);

        String url = "";
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(Extras.EXTRA_URL);
            mIsResumed = savedInstanceState.getBoolean(Extras.EXTRA_RESUMED);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString(Extras.EXTRA_URL);
        }

        mWallpaper = Database.get(this).getWallpaper(url);
        if (mWallpaper == null) {
            finish();
            return;
        }

        mProperties = WallpaperProperty.getWallpaperProperties(this, mWallpaper);
        mPalette = new ColorPalette();
        mCategories = Database.get(this).getWallpaperCategories(mWallpaper.getCategory());

        mBack.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_back, Color.WHITE));
        mBack.setOnClickListener(this);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.wallpaper_details_column_count),
                StaggeredGridLayoutManager.VERTICAL));

        mSlidingLayout.setDragView(mBottomPanel);
        mSlidingLayout.setScrollableView(mRecyclerView);
        mSlidingLayout.setCoveredFadeColor(Color.TRANSPARENT);
        mSlidingLayout.addPanelSlideListener(this);
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        resetBottomPadding();
        initBottomPanel(Color.WHITE);

        mRecyclerView.setAdapter(new WallpaperDetailsAdapter(this,
                new ArrayList<>(), new ColorPalette(), new ArrayList<>()));

        if (!mIsResumed) {
            mExitTransition = ActivityTransition
                    .with(getIntent())
                    .to(this, mImageView, Extras.EXTRA_IMAGE)
                    .duration(300)
                    .start(savedInstanceState);
        }

        if (mImageView.getDrawable() == null) {
            int color = mWallpaper.getColor();
            if (color == 0) {
                color = ColorHelper.getAttributeColor(this, R.attr.card_background);
            }

            AnimationHelper.setBackgroundColor(mSlidingLayout, Color.TRANSPARENT, color).start();
            mProgress.getIndeterminateDrawable().setColorFilter(
                    ColorHelper.setColorAlpha(ColorHelper.getTitleTextColor(color), 0.7f),
                    PorterDuff.Mode.SRC_IN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && savedInstanceState == null
                && mImageView.getDrawable() != null) {
            Transition transition = getWindow().getSharedElementEnterTransition();

            if (transition != null) {
                transition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        if (mIsEnter) {
                            mIsEnter = false;
                            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            mIsBottomPanelDragged = false;
                            loadWallpaper(mWallpaper.getThumbUrl());
                        }
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
                return;
            }
        }

        mRunnable = () -> {
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            mIsBottomPanelDragged = false;
            loadWallpaper(mWallpaper.getThumbUrl());
            mRunnable = null;
            mHandler = null;
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 700);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView, getResources().getInteger(
                R.integer.wallpaper_details_column_count));
        if (mRecyclerView.getAdapter() != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
        if (mTooltip != null) {
            mTooltip.dismiss();
        }
        new Handler().postDelayed(this::resetBottomPadding, 200);
        LocaleHelper.setLocale(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getExtras() != null) {
            this.setIntent(intent);
            String url = "";
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                url = bundle.getString(Extras.EXTRA_URL);
            }

            Wallpaper wallpaper = Database.get(this).getWallpaper(url);
            if (wallpaper == null) {
                return;
            }

            mWallpaper = wallpaper;
            loadWallpaper(mWallpaper.getThumbUrl());
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mWallpaper != null) {
            outState.putString(Extras.EXTRA_URL, mWallpaper.getUrl());
        }

        outState.putBoolean(Extras.EXTRA_RESUMED, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (Preferences.get(this).isCropWallpaper()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

        ImageLoader.getInstance().cancelDisplayTask(mImageView);
        WallpaperBoardApplication.sIsClickable = true;
        if (mAttacher != null) mAttacher.cleanup();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        if (mTooltip != null) {
            mTooltip.dismiss();
            mTooltip = null;
        }

        if (mHandler != null && mRunnable != null)
            mHandler.removeCallbacks(mRunnable);

        if (mExitTransition != null) {
            mExitTransition.exit(this);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back) {
            onBackPressed();
        } else if (id == R.id.menu_preview) {
            if (mTooltip != null) {
                mTooltip.dismiss();
            }

            if (mProgress.getVisibility() == View.GONE) {
                loadWallpaper(mWallpaper.getUrl());
            }
        } else if (id == R.id.menu_save) {
            if (PermissionHelper.isStorageGranted(this)) {
                WallpaperDownloader.prepare(this)
                        .wallpaper(mWallpaper)
                        .start();
                return;
            }

            PermissionHelper.requestStorage(this);
        } else if (id == R.id.menu_apply) {
            Popup popup = Popup.Builder(this)
                    .to(view)
                    .list(PopupItem.getApplyItems(this))
                    .callback((applyPopup, position) -> {
                        PopupItem item = applyPopup.getItems().get(position);
                        if (item.getType() == PopupItem.Type.WALLPAPER_CROP) {
                            Preferences.get(this).setCropWallpaper(!item.getCheckboxValue());
                            item.setCheckboxValue(Preferences.get(this).isCropWallpaper());

                            applyPopup.updateItem(position, item);
                            if (Preferences.get(WallpaperBoardPreviewActivity.this).isCropWallpaper()) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                return;
                            }

                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                            return;
                        } else if (item.getType() == PopupItem.Type.LOCKSCREEN) {
                            RectF rectF = null;
                            if (Preferences.get(WallpaperBoardPreviewActivity.this).isCropWallpaper()) {
                                if (mAttacher != null)
                                    rectF = mAttacher.getDisplayRect();
                            }

                            WallpaperApplyTask.prepare(this)
                                    .wallpaper(mWallpaper)
                                    .to(WallpaperApplyTask.Apply.LOCKSCREEN)
                                    .crop(rectF)
                                    .start(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else if (item.getType() == PopupItem.Type.HOMESCREEN) {
                            RectF rectF = null;
                            if (Preferences.get(WallpaperBoardPreviewActivity.this).isCropWallpaper()) {
                                if (mAttacher != null)
                                    rectF = mAttacher.getDisplayRect();
                            }

                            WallpaperApplyTask.prepare(this)
                                    .wallpaper(mWallpaper)
                                    .to(WallpaperApplyTask.Apply.HOMESCREEN)
                                    .crop(rectF)
                                    .start(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else if (item.getType() == PopupItem.Type.HOMESCREEN_LOCKSCREEN) {
                            RectF rectF = null;
                            if (Preferences.get(WallpaperBoardPreviewActivity.this).isCropWallpaper()) {
                                if (mAttacher != null)
                                    rectF = mAttacher.getDisplayRect();
                            }

                            WallpaperApplyTask.prepare(this)
                                    .wallpaper(mWallpaper)
                                    .to(WallpaperApplyTask.Apply.HOMESCREEN_LOCKSCREEN)
                                    .crop(rectF)
                                    .start(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        applyPopup.dismiss();
                    })
                    .build();

            if (getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                popup.getItems().remove(popup.getItems().size() - 1);
            }
            popup.show();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        int res = 0;
        if (id == R.id.menu_apply) {
            res = R.string.wallpaper_apply;
        } else if (id == R.id.menu_save) {
            res = R.string.wallpaper_save_to_device;
        } else if (id == R.id.menu_preview) {
            res = R.string.wallpaper_preview_full;
        }

        if (res == 0) return false;

        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                WallpaperDownloader.prepare(this)
                        .wallpaper(mWallpaper)
                        .start();

                if (!Preferences.get(this).isBackupRestored()) {
                    LocalFavoritesRestoreTask.with(this).start(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else {
                Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (slideOffset > 0.1f) {
            if (mIsBottomPanelDragged) return;

            mIsBottomPanelDragged = true;
            int fromColor = ColorHelper.get(this, R.color.bottomPanelCollapsed);
            int toColor = ColorHelper.getAttributeColor(this, R.attr.main_background);

            if (mRecyclerView.getAdapter() != null) {
                WallpaperDetailsAdapter adapter = (WallpaperDetailsAdapter) mRecyclerView.getAdapter();
                adapter.setWallpaperProperties(mProperties);
                adapter.setColorPalette(mPalette);
                adapter.setCategories(mCategories);
            }
            AnimationHelper.setBackgroundColor(mBottomPanel, fromColor, toColor)
                    .interpolator(new LinearOutSlowInInterpolator())
                    .duration(400)
                    .start();
            mName.setVisibility(View.GONE);
            mAuthor.setVisibility(View.GONE);
            initBottomPanel(ColorHelper.getTitleTextColor(toColor));
        } else if (slideOffset == 0f) {
            if (!mIsBottomPanelDragged) return;

            mIsBottomPanelDragged = false;
            int fromColor = ColorHelper.getAttributeColor(this, R.attr.main_background);
            int toColor = ColorHelper.get(this, R.color.bottomPanelCollapsed);

            if (mRecyclerView.getAdapter() != null) {
                WallpaperDetailsAdapter adapter = (WallpaperDetailsAdapter) mRecyclerView.getAdapter();
                adapter.setWallpaperProperties(new ArrayList<>());
                adapter.setColorPalette(new ColorPalette());
                adapter.setCategories(new ArrayList<>());
            }
            AnimationHelper.setBackgroundColor(mBottomPanel, fromColor, toColor)
                    .interpolator(new LinearOutSlowInInterpolator())
                    .duration(400)
                    .start();
            mName.setVisibility(View.VISIBLE);
            mAuthor.setVisibility(View.VISIBLE);
            initBottomPanel(Color.WHITE);
        }
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                    SlidingUpPanelLayout.PanelState newState) {
        File file = ImageLoader.getInstance().getDiskCache().get(mWallpaper.getUrl());
        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED &&
                mTooltip == null
                && Preferences.get(this).isShowWallpaperTooltip() &&
                !file.exists() &&
                !Preferences.get(this).isTimeToShowWallpaperPreviewIntro() &&
                !Preferences.get(this).isHighQualityPreviewEnabled()) {
            mTooltip = Tooltip.Builder(this)
                    .to(mMenuPreview)
                    .content(R.string.wallpaper_tooltip_preview)
                    .desc(R.string.wallpaper_tooltip_preview_icon_tap)
                    .descIcon(R.drawable.ic_toolbar_preview_full)
                    .visibleDontShowAgain(true)
                    .cancelable(false)
                    .buttonCallback(tooltip -> {
                        Preferences.get(this).setShowWallpaperTooltip(!tooltip.getCheckboxState());
                        tooltip.dismiss();
                    })
                    .build();
            mTooltip.show();
        }
    }

    @Override
    public void onPropertiesReceived(Wallpaper wallpaper) {
        if (wallpaper == null) return;

        mWallpaper.setDimensions(wallpaper.getDimensions());
        mWallpaper.setSize(wallpaper.getSize());
        mWallpaper.setMimeType(wallpaper.getMimeType());

        mProperties = WallpaperProperty.getWallpaperProperties(this, mWallpaper);
        if (mRecyclerView.getAdapter() != null && mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            WallpaperDetailsAdapter adapter = (WallpaperDetailsAdapter) mRecyclerView.getAdapter();
            adapter.setWallpaperProperties(mProperties);
        }
    }

    @Override
    public void onPaletteGenerated(ColorPalette palette) {
        mPalette = palette;
        if (mRecyclerView.getAdapter() != null && mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            WallpaperDetailsAdapter adapter = (WallpaperDetailsAdapter) mRecyclerView.getAdapter();
            adapter.setColorPalette(mPalette);
        }
    }

    private void initBottomPanel(int color) {
        mName.setText(mWallpaper.getName());
        mName.setTextColor(color);
        mAuthor.setText(mWallpaper.getAuthor());
        mAuthor.setTextColor(ColorHelper.setColorAlpha(color, 0.7f));

        mMenuPreview.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_preview_full, color));
        mMenuSave.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_download, color));
        mMenuApply.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_apply_options, color));

        if (getResources().getBoolean(R.bool.enable_wallpaper_download)) {
            mMenuSave.setVisibility(View.VISIBLE);
        }

        mMenuPreview.setOnClickListener(this);
        mMenuSave.setOnClickListener(this);
        mMenuApply.setOnClickListener(this);
        mMenuPreview.setOnLongClickListener(this);
        mMenuSave.setOnLongClickListener(this);
        mMenuApply.setOnLongClickListener(this);
    }

    private void resetBottomPadding() {
        int navBar = WindowHelper.getNavigationBarHeight(this);
        int bottomPanelHeight = getResources().getDimensionPixelSize(R.dimen.sliding_panel_height);
        int marginTop = getResources().getDimensionPixelSize(R.dimen.icon_size_small) +
                (getResources().getDimensionPixelSize(R.dimen.content_margin) * 2);
        int paddingRight = navBar;
        int paddingBottom = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            marginTop += WindowHelper.getStatusBarHeight(this);

            if (mBack.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mBack.getLayoutParams();
                params.topMargin = WindowHelper.getStatusBarHeight(this);
            }

            boolean tabletMode = getResources().getBoolean(R.bool.android_helpers_tablet_mode);
            if (tabletMode && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                int halfScreen = WindowHelper.getScreenSize(this).y / 2;
                marginTop = halfScreen - marginTop;
            }

            if (tabletMode || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                bottomPanelHeight += navBar;
                paddingRight = 0;
                paddingBottom = navBar;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (isInMultiWindowMode()) {
                    bottomPanelHeight = getResources().getDimensionPixelSize(R.dimen.sliding_panel_height);
                    paddingBottom = 0;
                }
            }
        }

        mSlidingLayout.setPanelHeight(bottomPanelHeight);
        mBottomPanel.setPadding(0, 0, paddingRight, 0);
        mRecyclerView.setPadding(0, 0, 0, paddingBottom);

        if (mBottomPanel.getLayoutParams() instanceof SlidingUpPanelLayout.LayoutParams) {
            SlidingUpPanelLayout.LayoutParams params = (SlidingUpPanelLayout.LayoutParams)
                    mBottomPanel.getLayoutParams();
            params.topMargin = marginTop;
        }
    }

    private void loadWallpaper(String url) {
        if (mAttacher != null) {
            mAttacher.cleanup();
            mAttacher = null;
        }

        boolean highQualityPreview = Preferences.get(this).isHighQualityPreviewEnabled();
        File file = ImageLoader.getInstance().getDiskCache().get(mWallpaper.getUrl());
        if (file.exists() || highQualityPreview) {
            if (file.exists()) {
                LogUtil.d("full size wallpaper available in cache: " +file.getName());
            }
            url = mWallpaper.getUrl();
        }

        if (!file.exists() && highQualityPreview) {
            if (mImageView.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                WallpaperPaletteLoaderTask.with(bitmap)
                        .callback(WallpaperBoardPreviewActivity.this)
                        .start();
            }
        }

        WallpaperPropertiesLoaderTask.prepare(this)
                .callback(this)
                .wallpaper(mWallpaper)
                .start(AsyncTask.THREAD_POOL_EXECUTOR);

        DisplayImageOptions.Builder options = ImageConfig.getRawDefaultImageOptions();
        options.cacheInMemory(false);
        options.cacheOnDisk(true);

        ImageLoader.getInstance().handleSlowNetwork(true);
        ImageLoader.getInstance().displayImage(url, mImageView, options.build(), new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                if (Preferences.get(WallpaperBoardPreviewActivity.this).isCropWallpaper()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                AnimationHelper.fade(mProgress).start();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                if (mWallpaper.getColor() == 0) {
                    mWallpaper.setColor(ColorHelper.getAttributeColor(
                            WallpaperBoardPreviewActivity.this, R.attr.colorAccent));
                }

                onWallpaperLoaded();
                if (mImageView.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
                    WallpaperPaletteLoaderTask.with(bitmap)
                            .callback(WallpaperBoardPreviewActivity.this)
                            .start();
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                WallpaperPaletteLoaderTask.with(loadedImage)
                        .callback(WallpaperBoardPreviewActivity.this)
                        .start();

                if (loadedImage != null && mWallpaper.getColor() == 0) {
                    try {
                        Palette.from(loadedImage).generate(palette -> {
                            int accent = ColorHelper.getAttributeColor(
                                    WallpaperBoardPreviewActivity.this, R.attr.colorAccent);
                            int color = palette.getVibrantColor(accent);
                            if (color == accent)
                                color = palette.getMutedColor(accent);

                            mWallpaper.setColor(color);
                            Database.get(WallpaperBoardPreviewActivity.this).updateWallpaper(mWallpaper);

                            onWallpaperLoaded();
                        });
                    } catch (Exception ignored) {
                        onWallpaperLoaded();
                    }
                    return;
                }

                onWallpaperLoaded();
            }
        }, null);
    }

    private void onWallpaperLoaded() {
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
        AnimationHelper.fade(mProgress).start();
        mRunnable = null;
        mHandler = null;
        mIsResumed = false;

        TapIntroHelper.showWallpaperPreviewIntro(this, mWallpaper.getColor());
    }
}
