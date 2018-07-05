package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.CreditsAdapter;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.items.Credit;
import com.dm.wallpaper.board.utils.Extras;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
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

public class CreditsFragment extends DialogFragment {

    @BindView(R2.id.listview)
    ListView mListView;

    private int mType;
    private AsyncTask mAsyncTask;

    private static final String TAG = "com.field.guide.dialog.credits";

    private static CreditsFragment newInstance(int type) {
        CreditsFragment fragment = new CreditsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Extras.EXTRA_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showCreditsDialog(FragmentManager fm, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.add(newInstance(type), TAG)
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        try {
            ft.commit();
        } catch (IllegalStateException e) {
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(Extras.EXTRA_TYPE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_credits, false);
        builder.typeface(TypefaceHelper.getMedium(getActivity()), TypefaceHelper.getRegular(getActivity()));
        builder.title(getTitle(mType));
        builder.positiveText(R.string.close);

        MaterialDialog dialog = builder.build();
        dialog.show();
        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(Extras.EXTRA_TYPE);
        }

        mAsyncTask = new CreditsLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extras.EXTRA_TYPE, mType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @NonNull
    private String getTitle(int type) {
        switch (type) {
            case Extras.TYPE_CONTRIBUTORS:
                return getActivity().getResources().getString(R.string.about_contributors_title);
            default:
                return "";
        }
    }

    private int getResource(int type) {
        switch (type) {
            case Extras.TYPE_CONTRIBUTORS:
                return R.xml.contributors;
            default:
                return -1;
        }
    }

    private class CreditsLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Credit> credits;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            credits = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    int res = getResource(mType);
                    if (res == -1) return false;
                    XmlPullParser xpp = getActivity().getResources().getXml(res);

                    while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("contributor")) {
                                Credit credit = new Credit(
                                        xpp.getAttributeValue(null, "name"),
                                        xpp.getAttributeValue(null, "contribution"),
                                        xpp.getAttributeValue(null, "image"),
                                        xpp.getAttributeValue(null, "link"));
                                credits.add(credit);
                            }
                        }
                        xpp.next();
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
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            if (aBoolean) {
                mListView.setAdapter(new CreditsAdapter(getActivity(), credits));
            } else {
                dismiss();
            }
        }
    }
}
