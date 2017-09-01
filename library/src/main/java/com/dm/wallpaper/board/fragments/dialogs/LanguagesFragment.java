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
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.LanguagesAdapter;
import com.dm.wallpaper.board.helpers.LocaleHelper;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.items.Language;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.LogUtil;

import java.util.List;
import java.util.Locale;

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

public class LanguagesFragment extends DialogFragment {

    @BindView(R2.id.listview)
    ListView mListView;

    private Locale mLocale;

    private AsyncTask<Void, Void, Boolean> mGetLanguages;

    public static final String TAG = "com.dm.wallpaper.board.dialog.languages";

    private static LanguagesFragment newInstance() {
        return new LanguagesFragment();
    }

    public static void showLanguageChooser(@NonNull FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = LanguagesFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_languages, false);
        builder.typeface(TypefaceHelper.getMedium(getActivity()), TypefaceHelper.getRegular(getActivity()));
        builder.title(R.string.pref_language_header);
        MaterialDialog dialog = builder.build();
        dialog.show();

        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLanguages();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mLocale != null) {
            Preferences.get(getActivity()).setCurrentLocale(mLocale.toString());
            LocaleHelper.setLocale(getActivity());
            getActivity().recreate();
        }
        if (mGetLanguages != null) mGetLanguages.cancel(true);
        super.onDismiss(dialog);
    }

    public void setLanguage(@NonNull Locale locale) {
        mLocale = locale;
        dismiss();
    }

    private void getLanguages() {
        mGetLanguages = new AsyncTask<Void, Void, Boolean>() {

            List<Language> languages;
            int index = 0;

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        languages = LocaleHelper.getAvailableLanguages(getActivity());
                        Locale locale = Preferences.get(getActivity()).getCurrentLocale();
                        for (int i = 0; i < languages.size(); i++) {
                            Locale l = languages.get(i).getLocale();
                            if (l.toString().equals(locale.toString())) {
                                index = i;
                                break;
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
                if (aBoolean) {
                    mListView.setAdapter(new LanguagesAdapter(getActivity(), languages, index));
                } else {
                    dismiss();
                }
                mGetLanguages = null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
