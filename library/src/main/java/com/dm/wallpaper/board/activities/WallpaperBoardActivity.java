package com.dm.wallpaper.board.activities;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.danimahardhika.android.helpers.license.LicenseHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.applications.WallpaperBoardApplication;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.fragments.AboutFragment;
import com.dm.wallpaper.board.fragments.CollectionFragment;
import com.dm.wallpaper.board.fragments.FavoritesFragment;
import com.dm.wallpaper.board.fragments.SettingsFragment;
import com.dm.wallpaper.board.fragments.dialogs.InAppBillingFragment;
import com.dm.wallpaper.board.helpers.ConfigurationHelper;
import com.dm.wallpaper.board.helpers.InAppBillingHelper;

import com.dm.wallpaper.board.helpers.LicenseCallbackHelper;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.items.InAppBilling;
import com.dm.wallpaper.board.items.PopupItem;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.tasks.WallpapersLoaderTask;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.Popup;
import com.dm.wallpaper.board.utils.listeners.AppBarListener;
import com.dm.wallpaper.board.utils.listeners.InAppBillingListener;
import com.dm.wallpaper.board.utils.listeners.NavigationListener;
import com.dm.wallpaper.board.utils.listeners.TabListener;
import com.dm.wallpaper.board.utils.views.HeaderView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

public class WallpaperBoardActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        InAppBillingListener, AppBarListener, NavigationListener, TabListener {

    @BindView(R2.id.search_bar)
    CardView mSearchBar;
    @BindView(R2.id.navigation)
    ImageView mNavigation;
    @BindView(R2.id.sort)
    ImageView mMenuSort;
    @BindView(R2.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R2.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private BillingProcessor mBillingProcessor;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragManager;
    private LicenseHelper mLicenseHelper;
    private AsyncTask mAsyncTask;

    private String mFragmentTag;
    private int mPosition, mLastPosition;

    private String mLicenseKey;
    private String[] mDonationProductsId;

    public void initMainActivity(@Nullable Bundle savedInstanceState, boolean isLicenseCheckerEnabled,
                                 @NonNull byte[] salt, @NonNull String licenseKey,
                                 @NonNull String[] donationProductsId) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_board);
        ButterKnife.bind(this);
        Database.get(this.getApplicationContext());

        WindowHelper.resetNavigationBarTranslucent(this,
                WindowHelper.NavigationBarTranslucent.PORTRAIT_ONLY);

        SoftKeyboardHelper softKeyboardHelper = new SoftKeyboardHelper(this,
                findViewById(R.id.container));
        softKeyboardHelper.enable();

        mFragManager = getSupportFragmentManager();
        mLicenseKey = licenseKey;
        mDonationProductsId = donationProductsId;

        initSearchBar();
        initNavigationView();
        initNavigationViewHeader();
        initInAppBilling();

        mPosition = mLastPosition = 0;
        if (savedInstanceState != null) {
            mPosition = mLastPosition = savedInstanceState.getInt(Extras.EXTRA_POSITION, 0);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int position = bundle.getInt(Extras.EXTRA_POSITION, -1);
            if (position >= 0 && position < 5) {
                mPosition = mLastPosition = position;
            }
        }

        setFragment(getFragment(mPosition));
        mAsyncTask = WallpapersLoaderTask.start(this);

        if (Preferences.get(this).isFirstRun() && isLicenseCheckerEnabled) {
            mLicenseHelper = new LicenseHelper(this);
            mLicenseHelper.run(mLicenseKey, salt, new LicenseCallbackHelper(this));
            return;
        }

        if (isLicenseCheckerEnabled && !Preferences.get(this).isLicensed()) {
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extras.EXTRA_POSITION, mPosition);
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        Database.get(this.getApplicationContext()).openDatabase();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mBillingProcessor != null) {
            mBillingProcessor.release();
            mBillingProcessor = null;
        }

        if (mLicenseHelper != null) {
            mLicenseHelper.destroy();
        }

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetNavigationView(newConfig.orientation);
        WindowHelper.resetNavigationBarTranslucent(this, WindowHelper.NavigationBarTranslucent.PORTRAIT_ONLY);
        LocaleHelper.setLocale(this);
    }

    @Override
    public void onBackPressed() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            clearBackStack();
            return;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (!mFragmentTag.equals(Extras.TAG_COLLECTION)) {
            mPosition = mLastPosition = 0;
            setFragment(getFragment(mPosition));
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onNavigationIconClick() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onAppBarScroll(float percentage) {
        if (percentage == 1f) {
            if (mSearchBar.getVisibility() == View.VISIBLE) {
                AnimationHelper.slideUpOut(mSearchBar)
                        .duration(400)
                        .callback(new AnimationHelper.Callback() {
                            @Override
                            public void onAnimationStart() {
                                mSearchBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationEnd() {

                            }
                        })
                        .start();
            }
        } else if (percentage < 0.5f) {
            if (mSearchBar.getVisibility() == View.GONE) {
                AnimationHelper.slideDownIn(mSearchBar)
                        .interpolator(new LinearOutSlowInInterpolator())
                        .duration(400)
                        .callback(new AnimationHelper.Callback() {
                            @Override
                            public void onAnimationStart() {
                                mSearchBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd() {

                            }
                        })
                        .start();
            }
        }
    }

    @Override
    public void onTabScroll(String tag) {
        if (tag.equals(Extras.TAG_LATEST) || tag.equals(Extras.TAG_CATEGORIES)) {
            if (mMenuSort.getVisibility() == View.VISIBLE) {
                AnimationHelper.hide(mMenuSort).start();
            }
        } else if (tag.equals(Extras.TAG_WALLPAPERS)) {
            if (mMenuSort.getVisibility() == View.GONE) {
                AnimationHelper.show(mMenuSort).start();
            }
        }
    }

    @Override
    public void onInAppBillingInitialized(boolean success) {
        if (!success) mBillingProcessor = null;
    }

    @Override
    public void onInAppBillingSelected(InAppBilling product) {
        if (mBillingProcessor == null) return;
        mBillingProcessor.purchase(this, product.getProductId());
    }

    @Override
    public void onInAppBillingConsume(String productId) {
        if (mBillingProcessor == null) return;
        if (mBillingProcessor.consumePurchase(productId)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.navigation_view_donate)
                    .content(R.string.donation_success)
                    .positiveText(R.string.close)
                    .show();
        }
    }

    private void initSearchBar() {
        int color = ColorHelper.getAttributeColor(this, R.attr.search_bar_icon);

        ImageView searchIcon = ButterKnife.findById(this, R.id.search);
        if (searchIcon != null) {
            searchIcon.setImageDrawable(DrawableHelper.getTintedDrawable(
                    this, R.drawable.ic_toolbar_search, color));
        }

        TextView searchBarTitle = ButterKnife.findById(this, R.id.search_bar_title);
        if (searchBarTitle != null) {
            searchBarTitle.setTextColor(ColorHelper.setColorAlpha(color, 0.7f));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mSearchBar.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mSearchBar.getLayoutParams();
                params.setMargins(params.leftMargin,
                        params.topMargin + WindowHelper.getStatusBarHeight(this),
                        params.leftMargin,
                        params.bottomMargin);
            }

            StateListAnimator stateListAnimator = AnimatorInflater
                    .loadStateListAnimator(this, R.animator.card_lift);
            mSearchBar.setStateListAnimator(stateListAnimator);
        }

        mSearchBar.setOnClickListener(view -> {
            Intent intent = new Intent(this, WallpaperBoardBrowserActivity.class);
            intent.putExtra(Extras.EXTRA_FRAGMENT_ID, Extras.ID_WALLPAPER_SEARCH);

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        mMenuSort.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_sort, color));
        mMenuSort.setOnClickListener(view -> {
            Popup.Builder(this)
                    .to(mMenuSort)
                    .list(PopupItem.getSortItems(this, true))
                    .callback((popup, position) -> {
                        Preferences.get(WallpaperBoardActivity.this)
                                .setSortBy(popup.getItems().get(position).getType());

                        if (mFragmentTag.equals(Extras.TAG_COLLECTION)) {
                            Fragment fragment = mFragManager.findFragmentByTag(Extras.TAG_COLLECTION);
                            if (fragment != null && fragment instanceof CollectionFragment) {
                                CollectionFragment f = (CollectionFragment) fragment;
                                f.refreshWallpapers();
                            }
                        }

                        popup.dismiss();
                    })
                    .show();
        });
    }

    private void initNavigationView() {
        Drawable drawable = ConfigurationHelper.getNavigationIcon(this,
                WallpaperBoardApplication.getConfiguration().getNavigationIcon());
        int color = ColorHelper.getAttributeColor(this, R.attr.search_bar_icon);
        mNavigation.setImageDrawable(DrawableHelper.getTintedDrawable(drawable, color));
        mNavigation.setOnClickListener(view -> {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
                return;
            }

            mDrawerLayout.openDrawer(GravityCompat.START);
        });

        resetNavigationView(getResources().getConfiguration().orientation);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.txt_open, R.string.txt_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View view = getWindow().getDecorView();
                    if (view != null) {
                        view.setSystemUiVisibility(0);
                    }
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                ColorHelper.setupStatusBarIconColor(WallpaperBoardActivity.this);

                if (mPosition == 4) {
                    mPosition = mLastPosition;
                    mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                    InAppBillingFragment.showInAppBillingDialog(mFragManager,
                            mBillingProcessor,
                            mLicenseKey,
                            mDonationProductsId);
                    return;
                }

                if (mPosition != mLastPosition) {
                    mLastPosition = mPosition;
                    setFragment(getFragment(mPosition));
                }
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(false);

        mDrawerLayout.setDrawerShadow(R.drawable.navigation_view_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        ColorStateList colorStateList = ContextCompat.getColorStateList(this,
                Preferences.get(this).isDarkTheme() ?
                        R.color.navigation_view_item_highlight_dark :
                        R.color.navigation_view_item_highlight);

        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.navigation_view_donate);
        if (menuItem != null) {
            menuItem.setVisible(getResources().getBoolean(R.bool.enable_donation));
        }

        mNavigationView.setItemTextColor(colorStateList);
        mNavigationView.setItemIconTintList(colorStateList);
        Drawable background = ContextCompat.getDrawable(this,
                Preferences.get(this).isDarkTheme() ?
                        R.drawable.navigation_view_item_background_dark :
                        R.drawable.navigation_view_item_background);
        mNavigationView.setItemBackground(background);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_view_wallpapers) mPosition = 0;
            else if (id == R.id.navigation_view_favorites) mPosition = 1;
            else if (id == R.id.navigation_view_settings) mPosition = 2;
            else if (id == R.id.navigation_view_about) mPosition = 3;
            else if (id == R.id.navigation_view_donate) mPosition = 4;
            else if (id == R.id.navigation_view_share) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_app_subject,
                        getResources().getString(R.string.app_name)));
                intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_app_body,
                        getResources().getString(R.string.app_name),
                        "https://play.google.com/store/apps/details?id=" +getPackageName()));
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.email_client)));
                return false;

            } else if (id == R.id.navigation_view_rate) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=" +getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
                return false;
            }

            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
        ViewHelper.hideNavigationViewScrollBar(mNavigationView);
    }

    private void initNavigationViewHeader() {
        if (WallpaperBoardApplication.getConfiguration().getNavigationViewHeader() == WallpaperBoardApplication.NavigationViewHeader.NONE) {
            mNavigationView.removeHeaderView(mNavigationView.getHeaderView(0));
            return;
        }

        String imageUrl = getResources().getString(R.string.navigation_view_header);
        String titleText = getResources().getString(R.string.navigation_view_header_title);
        View header = mNavigationView.getHeaderView(0);

        HeaderView image = ButterKnife.findById(header, R.id.header_image);
        LinearLayout container = ButterKnife.findById(header, R.id.header_title_container);
        TextView title = ButterKnife.findById(header, R.id.header_title);
        TextView version = ButterKnife.findById(header, R.id.header_version);

        if (WallpaperBoardApplication.getConfiguration().getNavigationViewHeader() == WallpaperBoardApplication.NavigationViewHeader.MINI) {
            image.setRatio(16, 9);
        }

        if (titleText.length() == 0) {
            container.setVisibility(View.GONE);
        } else {
            title.setText(titleText);
            try {
                String versionText = "v" + getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
                version.setText(versionText);
            } catch (Exception ignored) {}
        }

        if (ColorHelper.isValidColor(imageUrl)) {
            image.setBackgroundColor(Color.parseColor(imageUrl));
            return;
        }

        if (!URLUtil.isValidUrl(imageUrl)) {
            imageUrl = "drawable://" + DrawableHelper.getResourceId(this, imageUrl);
        }

        ImageLoader.getInstance().displayImage(imageUrl, new ImageViewAware(image),
                ImageConfig.getDefaultImageOptions(), new ImageSize(720, 720), null, null);
    }

    private void initInAppBilling() {
        if (!getResources().getBoolean(R.bool.enable_donation)) return;
        if (mBillingProcessor != null) return;

        if (BillingProcessor.isIabServiceAvailable(this)) {
            mBillingProcessor = new BillingProcessor(this,
                    mLicenseKey, new InAppBillingHelper(this));
        }
    }

    private void resetNavigationView(int orientation) {
        int index = mNavigationView.getMenu().size() - 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mNavigationView.getMenu().getItem(index).setVisible(true);
                mNavigationView.getMenu().getItem(index).setEnabled(false);
                return;
            }
        }
        mNavigationView.getMenu().getItem(index).setVisible(false);
    }

    private void setFragment(Fragment fragment) {
        if (fragment == null) return;
        clearBackStack();

        FragmentTransaction ft = mFragManager.beginTransaction().replace(
                R.id.container, fragment, mFragmentTag);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }

        mNavigationView.getMenu().getItem(mPosition).setChecked(true);

        float percentage = 1.0f;
        if (mPosition == 0) percentage = 0f;
        onAppBarScroll(percentage);
    }

    @Nullable
    private Fragment getFragment(int position) {
        if (position == 0) {
            mFragmentTag = Extras.TAG_COLLECTION;
            return new CollectionFragment();
        } else if (position == 1) {
            mFragmentTag = Extras.TAG_FAVORITES;
            return new FavoritesFragment();
        } else if (position == 2) {
            mFragmentTag = Extras.TAG_SETTINGS;
            return new SettingsFragment();
        } else if (position == 3) {
            mFragmentTag = Extras.TAG_ABOUT;
            return new AboutFragment();
        }
        return null;
    }

    private void clearBackStack() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
