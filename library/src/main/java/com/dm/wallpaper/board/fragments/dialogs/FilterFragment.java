package com.dm.wallpaper.board.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.FilterAdapter;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.fragments.WallpapersFragment;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.utils.Extras;

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

public class FilterFragment extends DialogFragment {

    @BindView(R2.id.listview)
    ListView listView;

    private boolean mIsMuzei;

    private static final String MUZEI = "muzei";
    private static final String TAG = "com.dm.wallpaper.board.dialog.filter";

    private static FilterFragment newInstance(boolean isMuzei) {
        FilterFragment fragment = new FilterFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(MUZEI, isMuzei);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showFilterDialog(FragmentManager fm, boolean isMuzei) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = FilterFragment.newInstance(isMuzei);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.typeface("Font-Medium.ttf", "Font-Regular.ttf");
        builder.title(mIsMuzei ? R.string.muzei_category : R.string.wallpaper_filter);
        builder.customView(R.layout.fragment_filter, false);
        MaterialDialog dialog = builder.build();
        dialog.show();

        ButterKnife.bind(this, dialog);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsMuzei = getArguments().getBoolean(MUZEI);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<Category> categories = new Database(getActivity()).getCategories();
        listView.setAdapter(new FilterAdapter(getActivity(), categories, mIsMuzei));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (getActivity() == null) return;

        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm == null) return;

        WallpapersFragment fragment = (WallpapersFragment) fm.findFragmentByTag(Extras.TAG_WALLPAPERS);
        if (fragment != null) {
            fragment.filterWallpapers();
        }
        super.onDismiss(dialog);
    }
}
