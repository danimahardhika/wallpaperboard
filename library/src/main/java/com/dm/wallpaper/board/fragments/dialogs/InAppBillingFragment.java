package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.InAppBillingAdapter;
import com.dm.wallpaper.board.items.InAppBilling;
import com.dm.wallpaper.board.utils.Extras;
import com.dm.wallpaper.board.utils.LogUtil;
import com.dm.wallpaper.board.utils.listeners.InAppBillingListener;

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

public class InAppBillingFragment extends DialogFragment {

    @BindView(R2.id.inapp_list)
    ListView mListView;
    @BindView(R2.id.progress)
    ProgressBar mProgress;

    private String mKey;
    private String[] mProductsId;

    private InAppBillingAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mLoadInAppProducts;

    private static BillingProcessor mBillingProcessor;

    private static final String TAG = "com.dm.wallpaper.board.dialog.inappbilling";

    private static InAppBillingFragment newInstance(String key, String[] productId) {
        InAppBillingFragment fragment = new InAppBillingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Extras.EXTRA_KEY, key);
        bundle.putStringArray(Extras.EXTRA_PRODUCT_ID, productId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showInAppBillingDialog(@NonNull FragmentManager fm, BillingProcessor billingProcessor,
                                              @NonNull String key, @NonNull String[] productId) {
        mBillingProcessor = billingProcessor;
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = InAppBillingFragment.newInstance(key, productId);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKey = getArguments().getString(Extras.EXTRA_KEY);
        mProductsId = getArguments().getStringArray(Extras.EXTRA_PRODUCT_ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_inappbilling, false)
                .typeface("Font-Medium.ttf", "Font-Regular.ttf")
                .title(R.string.navigation_view_donate)
                .positiveText(R.string.donate)
                .negativeText(R.string.close)
                .onPositive((dialog, which) -> {
                    if (mLoadInAppProducts == null) {
                        try {
                            InAppBillingListener listener = (InAppBillingListener) getActivity();
                            listener.onInAppBillingSelected(mAdapter.getSelectedProduct());
                        } catch (Exception ignored) {}
                        dismiss();
                    }
                });

        MaterialDialog dialog = builder.build();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        setCancelable(false);

        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mKey = savedInstanceState.getString(Extras.EXTRA_KEY);
            mProductsId = savedInstanceState.getStringArray(Extras.EXTRA_PRODUCT_ID);
        }
        loadInAppProducts();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Extras.EXTRA_KEY, mKey);
        outState.putStringArray(Extras.EXTRA_PRODUCT_ID, mProductsId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mBillingProcessor = null;
        if (mLoadInAppProducts != null) mLoadInAppProducts.cancel(true);
        super.onDismiss(dialog);
    }

    private void loadInAppProducts() {
        mLoadInAppProducts = new AsyncTask<Void, Void, Boolean>() {

            InAppBilling[] inAppBillings;
            boolean isBillingNotReady = false;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress.setVisibility(View.VISIBLE);
                inAppBillings = new InAppBilling[mProductsId.length];
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (mBillingProcessor == null) {
                            isBillingNotReady = true;
                            return false;
                        }

                        for (int i = 0; i < mProductsId.length; i++) {
                            SkuDetails product = mBillingProcessor
                                    .getPurchaseListingDetails(mProductsId[i]);
                            if (product != null) {
                                InAppBilling inAppBilling;
                                String title = product.title.substring(0, product.title.lastIndexOf("("));
                                inAppBilling = new InAppBilling(product.priceText, mProductsId[i], title);
                                inAppBillings[i] = inAppBilling;
                            } else {
                                if (i == mProductsId.length - 1)
                                    return false;
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                mProgress.setVisibility(View.GONE);
                if (aBoolean) {
                    mAdapter = new InAppBillingAdapter(getActivity(), inAppBillings);
                    mListView.setAdapter(mAdapter);
                } else {
                    dismiss();
                    if (!isBillingNotReady)
                        Toast.makeText(getActivity(), R.string.billing_load_product_failed,
                                Toast.LENGTH_LONG).show();
                }
                mLoadInAppProducts = null;
            }

        }.execute();
    }
}
