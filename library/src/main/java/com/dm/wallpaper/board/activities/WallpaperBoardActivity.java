package com.dm.wallpaper.board.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.fragments.AboutFragment;
import com.dm.wallpaper.board.fragments.FavoritesFragment;
import com.dm.wallpaper.board.fragments.SettingsFragment;
import com.dm.wallpaper.board.fragments.WallpapersFragment;
import com.dm.wallpaper.board.fragments.dialogs.InAppBillingFragment;
import com.dm.wallpaper.board.helpers.ColorHelper;
import com.dm.wallpaper.board.helpers.DrawableHelper;
import com.dm.wallpaper.board.helpers.InAppBillingHelper;
import com.dm.wallpaper.board.helpers.LicenseHelper;
import com.dm.wallpaper.board.helpers.PermissionHelper;
import com.dm.wallpaper.board.helpers.SoftKeyboardHelper;
import com.dm.wallpaper.board.helpers.ViewHelper;
import com.dm.wallpaper.board.items.InAppBilling;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.receivers.WallpaperBoardReceiver;
import com.dm.wallpaper.board.services.WallpaperBoardService;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.ImageConfig;
import com.dm.wallpaper.board.utils.listeners.InAppBillingListener;
import com.dm.wallpaper.board.utils.listeners.SearchListener;
import com.dm.wallpaper.board.utils.listeners.WallpaperBoardListener;
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
        WallpaperBoardListener, InAppBillingListener, SearchListener {

    @BindView(R2.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R2.id.toolbar_logo)
    TextView mToolbarLogo;
    @BindView(R2.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R2.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private BillingProcessor mBillingProcessor;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragManager;
    private WallpaperBoardReceiver mReceiver;

    private String mFragmentTag;
    private int mPosition, mLastPosition;

    private String mLicenseKey;
    private String[] mDonationProductsId;

    public void initMainActivity(@Nullable Bundle savedInstanceState, boolean isLicenseCheckerEnabled,
                                 @NonNull byte[] salt, @NonNull String licenseKey,
                                 @NonNull String[] donationProductsId) {
        super.setTheme(Preferences.getPreferences(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_board);
        ButterKnife.bind(this);
        ViewHelper.setApplicationWindowColor(this);
        ViewHelper.resetNavigationBarTranslucent(this,
                getResources().getConfiguration().orientation);
        registerBroadcastReceiver();

        SoftKeyboardHelper softKeyboardHelper = new SoftKeyboardHelper(this,
                findViewById(R.id.container));
        softKeyboardHelper.enable();

        mFragManager = getSupportFragmentManager();
        mLicenseKey = licenseKey;
        mDonationProductsId = donationProductsId;

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        AppBarLayout appBar = ButterKnife.findById(this, R.id.appbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        appBar.setExpanded(false);

        initNavigationView(toolbar);
        initNavigationViewHeader();
        initTheme();
        initInAppBilling();

        mPosition = mLastPosition = 0;
        if (savedInstanceState != null) {
            mPosition = mLastPosition = savedInstanceState.getInt("position", 0);
        }

        setFragment(getFragment(mPosition));
        checkWallpapers();

        if (Preferences.getPreferences(this).isFirstRun()) {
            if (isLicenseCheckerEnabled) {
                LicenseHelper.getLicenseChecker(this).checkLicense(mLicenseKey, salt);
                return;
            }
        }

        if (isLicenseCheckerEnabled) {
            if (!Preferences.getPreferences(this).isLicensed())
                finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
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
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (mBillingProcessor != null) {
            mBillingProcessor.release();
            mBillingProcessor = null;
        }
        if (mReceiver != null) unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ViewHelper.disableAppBarDrag(ButterKnife.findById(this, R.id.appbar));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetNavigationView(newConfig.orientation);
        ViewHelper.resetNavigationBarTranslucent(this, newConfig.orientation);
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
        if (requestCode == PermissionHelper.PERMISSION_STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                WallpapersFragment fragment = (WallpapersFragment) mFragManager
                        .findFragmentByTag(Extras.TAG_WALLPAPERS);
                if (fragment != null) {
                    fragment.downloadWallpaper();
                }
            } else {
                PermissionHelper.showPermissionStorageDenied(this);
            }
        }
    }

    @Override
    public void OnWallpapersChecked(@Nullable Intent intent) {
        if (intent != null) {
            int size = intent.getIntExtra(Extras.EXTRA_SIZE, 0);
            Database database = new Database(this);
            int offlineSize = database.getWallpapersCount();
            Preferences.getPreferences(this).setAvailableWallpapersCount(size);

            if (size > offlineSize) {
                int accent = ColorHelper.getAttributeColor(this, R.attr.colorAccent);
                LinearLayout container = (LinearLayout) mNavigationView.getMenu().getItem(0).getActionView();
                if (container != null) {
                    TextView counter = (TextView) container.findViewById(R.id.counter);
                    if (counter == null) return;

                    ViewCompat.setBackground(counter, DrawableHelper.getTintedDrawable(this,
                            R.drawable.ic_toolbar_circle, accent));
                    counter.setTextColor(ColorHelper.getTitleTextColor(accent));
                    int newItem = (size - offlineSize);
                    counter.setText(String.valueOf(newItem > 99 ? "99+" : newItem));
                    container.setVisibility(View.VISIBLE);

                    if (mFragmentTag.equals(Extras.TAG_WALLPAPERS)) {
                        WallpapersFragment fragment = (WallpapersFragment)
                                mFragManager.findFragmentByTag(Extras.TAG_WALLPAPERS);
                        if (fragment != null) fragment.initPopupBubble();
                    }
                    return;
                }
            }
        }

        LinearLayout container = (LinearLayout) mNavigationView.getMenu().getItem(0).getActionView();
        if (container != null) container.setVisibility(View.GONE);
    }

    @Override
    public void OnInAppBillingInitialized(boolean success) {
        if (!success) mBillingProcessor = null;
    }

    @Override
    public void OnInAppBillingSelected(InAppBilling product) {
        if (mBillingProcessor == null) return;
        mBillingProcessor.purchase(this, product.getProductId());
    }

    @Override
    public void OnInAppBillingConsume(String productId) {
        if (mBillingProcessor == null) return;
        if (mBillingProcessor.consumePurchase(productId)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.navigation_view_donate)
                    .content(R.string.donation_success)
                    .positiveText(R.string.close)
                    .show();
        }
    }

    @Override
    public void OnSearchExpanded(boolean expand) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);

        if (expand) {
            int color = ColorHelper.getAttributeColor(this, R.attr.search_toolbar_color);
            ViewHelper.changeSearchViewActionModeColor(this,
                    collapsingToolbar, R.attr.toolbar_color, R.attr.search_toolbar_color);
            ColorHelper.setStatusBarColor(this, color);

            int iconColor = ColorHelper.getAttributeColor(this, R.attr.search_toolbar_icon);
            toolbar.setNavigationIcon(DrawableHelper.getTintedDrawable(
                    this, R.drawable.ic_toolbar_back, iconColor));
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        } else {
            SoftKeyboardHelper.closeKeyboard(this);
            ColorHelper.setTransparentStatusBar(this, Color.TRANSPARENT);
            collapsingToolbar.setContentScrim(new ColorDrawable(
                    ColorHelper.getAttributeColor(this, R.attr.toolbar_color)));
            mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(this));
            toolbar.setNavigationOnClickListener(view ->
                    mDrawerLayout.openDrawer(GravityCompat.START));
        }

        mDrawerLayout.setDrawerLockMode(expand ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                DrawerLayout.LOCK_MODE_UNLOCKED);
        supportInvalidateOptionsMenu();
    }

    private void initNavigationView(Toolbar toolbar) {
        resetNavigationView(getResources().getConfiguration().orientation);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.txt_open, R.string.txt_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
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

        mDrawerLayout.setDrawerShadow(R.drawable.navigation_view_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        ColorStateList colorStateList = ContextCompat.getColorStateList(this,
                Preferences.getPreferences(this).isDarkTheme() ?
                        R.color.navigation_view_item_highlight_dark :
                        R.color.navigation_view_item_highlight);
        mNavigationView.getMenu().getItem(mNavigationView.getMenu().size() - 2).setVisible(
                getResources().getBoolean(R.bool.enable_donation));
        mNavigationView.setItemTextColor(colorStateList);
        mNavigationView.setItemIconTintList(colorStateList);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_view_wallpapers) mPosition = 0;
            else if (id == R.id.navigation_view_favorites) mPosition = 1;
            else if (id == R.id.navigation_view_settings) mPosition = 2;
            else if (id == R.id.navigation_view_about) mPosition = 3;
            else if (id == R.id.navigation_view_donate) mPosition = 4;

            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void initTheme() {
        getWindow().getDecorView().setBackgroundColor(
                ColorHelper.getAttributeColor(this, R.attr.main_background));
        ColorHelper.setStatusBarIconColor(this);
    }

    private void initNavigationViewHeader() {
        String imageUrl = getResources().getString(R.string.navigation_view_header);
        String titleText = getResources().getString(R.string.navigation_view_header_title);
        View header = mNavigationView.getHeaderView(0);
        ImageView image = (ImageView) header.findViewById(R.id.header_image);
        LinearLayout container = (LinearLayout) header.findViewById(R.id.header_title_container);
        TextView title = (TextView )header.findViewById(R.id.header_title);
        TextView version = (TextView) header.findViewById(R.id.header_version);

        if (titleText.length() == 0) {
            container.setVisibility(View.GONE);
            return;
        }
        title.setText(titleText);
        try {
            String versionText = "v" + getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            version.setText(versionText);
        } catch (Exception ignored) {}

        if (ColorHelper.isValidColor(imageUrl)) {
            image.setBackgroundColor(Color.parseColor(imageUrl));
            return;
        }

        if (!URLUtil.isValidUrl(imageUrl)) {
            imageUrl = "drawable://" + DrawableHelper.getResourceId(this, imageUrl);
        }

        ImageLoader.getInstance().displayImage(imageUrl, new ImageViewAware(image),
                ImageConfig.getDefaultImageOptions(true), new ImageSize(720, 720), null, null);
    }

    private void initInAppBilling() {
        if (!getResources().getBoolean(R.bool.enable_donation)) return;
        if (mBillingProcessor != null) return;

        if (BillingProcessor.isIabServiceAvailable(this)) {
            mBillingProcessor = new BillingProcessor(this,
                    mLicenseKey, new InAppBillingHelper(this));
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(WallpaperBoardReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mReceiver = new WallpaperBoardReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void checkWallpapers() {
        int wallpapersCount = new Database(this).getWallpapersCount();

        if (Preferences.getPreferences(this).isConnectedToNetwork() && (wallpapersCount > 0)) {
            Intent intent = new Intent(this, WallpaperBoardService.class);
            startService(intent);
            return;
        }

        int size = Preferences.getPreferences(this).getAvailableWallpapersCount();
        if (size > 0) {
            OnWallpapersChecked(new Intent().putExtra(Extras.EXTRA_SIZE, size));
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
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }

        mNavigationView.getMenu().getItem(mPosition).setChecked(true);
        resetToolbarLogo();
        mToolbarTitle.setText(mNavigationView.getMenu().getItem(mPosition).getTitle());
    }

    @Nullable
    private Fragment getFragment(int position) {
        if (position == 0) {
            mFragmentTag = Extras.TAG_WALLPAPERS;
            return new WallpapersFragment();
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

    private void resetToolbarLogo() {
        mToolbarTitle.setVisibility(mPosition == 0 ? View.GONE : View.VISIBLE);
        mToolbarLogo.setVisibility(mPosition == 0 ? View.VISIBLE : View.GONE);
    }

    private void clearBackStack() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            OnSearchExpanded(false);
        }
    }

}
