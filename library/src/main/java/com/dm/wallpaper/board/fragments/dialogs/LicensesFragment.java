package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

public class LicensesFragment extends DialogFragment {

    @BindView(R2.id.webview)
    WebView mWebView;

    private AsyncTask mAsyncTask;

    private static final String TAG = "com.dm.wallpaper.board.dialog.licenses";

    private static LicensesFragment newInstance() {
        return new LicensesFragment();
    }

    public static void showLicensesDialog(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.add(newInstance(), TAG)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        try {
            ft.commit();
        } catch (IllegalStateException e) {
            ft.commitAllowingStateLoss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_licenses, false);
        builder.typeface(TypefaceHelper.getMedium(getActivity()), TypefaceHelper.getRegular(getActivity()));
        builder.title(R.string.about_open_source_licenses);
        MaterialDialog dialog = builder.build();
        dialog.show();

        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAsyncTask = new LicensesLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDismiss(dialog);
    }

    private class LicensesLoader extends AsyncTask<Void, Void, Boolean> {

        private StringBuilder sb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sb = new StringBuilder();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    InputStream rawResource = getActivity().getResources()
                            .openRawResource(R.raw.licenses);
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(rawResource));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    bufferedReader.close();
                    rawResource.close();
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
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            if (aBoolean) {
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadDataWithBaseURL(null,
                        sb.toString(), "text/html", "utf-8", null);
                LocaleHelper.setLocale(getActivity());
            }
        }
    }
}


