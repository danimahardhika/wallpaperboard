package com.dm.wallpaper.board.activities.configurations;

import android.support.annotation.NonNull;

/**
 * Author: Dani Mahardhika
 * Created on: 10/27/2017
 * https://github.com/danimahardhika
 */

public class ActivityConfiguration {

    private boolean mIsLicenseCheckerEnabled;
    private byte[] mRandomString;
    private String mLicenseKey;
    private String[] mDonationProductsId;

    public ActivityConfiguration setLicenseCheckerEnabled(boolean enabled) {
        mIsLicenseCheckerEnabled = enabled;
        return this;
    }

    public ActivityConfiguration setRandomString(@NonNull byte[] randomString) {
        mRandomString = randomString;
        return this;
    }

    public ActivityConfiguration setLicenseKey(@NonNull String licenseKey) {
        mLicenseKey = licenseKey;
        return this;
    }

    public ActivityConfiguration setDonationProductsId(@NonNull String[] productsId) {
        mDonationProductsId = productsId;
        return this;
    }

    public boolean isLicenseCheckerEnabled() {
        return mIsLicenseCheckerEnabled;
    }

    public byte[] getRandomString() {
        return mRandomString;
    }

    public String getLicenseKey() {
        return mLicenseKey;
    }

    public String[] getDonationProductsId() {
        return mDonationProductsId;
    }
}
