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
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.R2;
import com.dm.wallpaper.board.adapters.FilterAdapter;
import com.dm.wallpaper.board.databases.Database;
import com.dm.wallpaper.board.helpers.TypefaceHelper;
import com.dm.wallpaper.board.items.Category;
import com.dm.wallpaper.board.utils.LogUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

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

public class FilterFragment extends DialogFragment implements View.OnClickListener {

    @BindView(R2.id.title)
    TextView mTitle;
    @BindView(R2.id.menu_select)
    ImageView mMenuSelect;
    @BindView(R2.id.listview)
    ListView mListView;
    @BindView(R2.id.progress)
    MaterialProgressBar mProgress;

    private FilterAdapter mAdapter;
    private AsyncTask mAsyncTask;
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
        builder.typeface(TypefaceHelper.getMedium(getActivity()), TypefaceHelper.getRegular(getActivity()));
        builder.customView(R.layout.fragment_filter, false);
        MaterialDialog dialog = builder.build();
        dialog.show();

        ButterKnife.bind(this, dialog);
        mTitle.setText(mIsMuzei ? R.string.muzei_category : R.string.wallpaper_filter);
        mMenuSelect.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsMuzei = getArguments().getBoolean(MUZEI);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mIsMuzei = savedInstanceState.getBoolean(MUZEI);
        }

        mAsyncTask = new CategoriesLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MUZEI, mIsMuzei);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.menu_select) {
            if (mAsyncTask != null) return;
            if (mAdapter != null) {
                mAdapter.setEnabled(false);
            }
            mAsyncTask = new SelectAllLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void initMenuSelect() {
        int color = ColorHelper.getAttributeColor(getActivity(), android.R.attr.textColorPrimary);
        boolean isAllSelected = mAdapter.getCount() == mAdapter.getSelectedCount();

        mMenuSelect.setImageDrawable(DrawableHelper.getTintedDrawable(getActivity(),
                isAllSelected ? R.drawable.ic_toolbar_select_all_selected : R.drawable.ic_toolbar_select_all,
                color));
        AnimationHelper.show(mMenuSelect).start();
    }

    private class SelectAllLoader extends AsyncTask<Void, Void, Boolean> {

        boolean isAllSelected;

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    isAllSelected = mAdapter.selectAll();
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
                int color = ColorHelper.getAttributeColor(getActivity(), android.R.attr.textColorPrimary);
                mMenuSelect.setImageDrawable(DrawableHelper.getTintedDrawable(getActivity(),
                        isAllSelected ? R.drawable.ic_toolbar_select_all_selected : R.drawable.ic_toolbar_select_all,
                        color));
                if (mAdapter != null) {
                    mAdapter.setEnabled(true);
                    mAdapter.notifyDataSetChanged();
                }

            }
        }
    }

    private class CategoriesLoader extends AsyncTask<Void, Void, Boolean> {

        List<Category> categories;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    categories = Database.get(getActivity()).getCategories();
                    for (Category category : categories) {
                        int count = Database.get(getActivity()).getCategoryCount(category.getName());
                        category.setCount(count);
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
            mProgress.setVisibility(View.GONE);
            if (aBoolean) {
                mAdapter = new FilterAdapter(getActivity(), categories, mIsMuzei);
                mListView.setAdapter(mAdapter);

                initMenuSelect();
            } else {
                dismiss();
            }
        }
    }
}
