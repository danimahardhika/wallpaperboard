package com.dm.wallpaper.board.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.dm.wallpaper.board.utils.listeners.InAppBillingListener;

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

public class InAppBillingHelper implements BillingProcessor.IBillingHandler {

    private final Context mContext;

    public InAppBillingHelper(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        InAppBillingListener listener = (InAppBillingListener) mContext;
        listener.OnInAppBillingConsume(productId);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (errorCode == Constants.BILLING_ERROR_FAILED_TO_INITIALIZE_PURCHASE) {
            InAppBillingListener listener = (InAppBillingListener) mContext;
            listener.OnInAppBillingInitialized(false);
        }
    }

    @Override
    public void onBillingInitialized() {
        InAppBillingListener listener = (InAppBillingListener) mContext;
        listener.OnInAppBillingInitialized(true);
    }
}
