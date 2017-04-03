package com.dm.wallpaper.board.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.wallpaper.board.R;
import com.dm.wallpaper.board.helpers.ColorHelper;
import com.dm.wallpaper.board.helpers.DrawableHelper;
import com.dm.wallpaper.board.helpers.FileHelper;
import com.dm.wallpaper.board.items.Setting;
import com.dm.wallpaper.board.preferences.Preferences;
import com.dm.wallpaper.board.utils.LogUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

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

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Setting> mSettings;

    private static final int TYPE_CONTENT = 0;
    private static final int TYPE_FOOTER = 1;

    public SettingsAdapter(@NonNull Context context, @NonNull List<Setting> settings) {
        mContext = context;
        mSettings = settings;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_settings_item_list, parent, false);
        } else if (viewType == TYPE_FOOTER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_settings_item_footer, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == TYPE_CONTENT) {
            Setting setting = mSettings.get(position);

            if (setting.getTitle().length() == 0) {
                holder.title.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
                holder.container.setVisibility(View.VISIBLE);

                holder.subtitle.setText(setting.getSubtitle());

                if (setting.getContent().length() == 0) {
                    holder.content.setVisibility(View.GONE);
                } else {
                    holder.content.setText(setting.getContent());
                    holder.content.setVisibility(View.VISIBLE);
                }

                if (setting.getFooter().length() == 0) {
                    holder.footer.setVisibility(View.GONE);
                } else {
                    holder.footer.setText(setting.getFooter());
                }

                if (setting.getCheckState() >= 0) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    holder.checkBox.setChecked(setting.getCheckState() == 1);
                } else {
                    holder.checkBox.setVisibility(View.GONE);
                }
            } else {
                holder.container.setVisibility(View.GONE);
                holder.title.setVisibility(View.VISIBLE);
                holder.title.setText(setting.getTitle());

                if (position > 0) {
                    holder.divider.setVisibility(View.VISIBLE);
                } else {
                    holder.divider.setVisibility(View.GONE);
                }

                if (setting.getIcon() != -1) {
                    int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                    holder.title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                            mContext, setting.getIcon(), color), null, null, null);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSettings.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (getItemCount() - 1)) return TYPE_FOOTER;
        return TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;
        private TextView subtitle;
        private TextView content;
        private TextView footer;
        private LinearLayout container;
        private AppCompatCheckBox checkBox;
        private View divider;

        private int holderId;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_CONTENT) {
                title = ButterKnife.findById(itemView, R.id.title);
                subtitle = ButterKnife.findById(itemView, R.id.subtitle);
                content = ButterKnife.findById(itemView, R.id.content);
                footer = ButterKnife.findById(itemView, R.id.footer);
                checkBox = ButterKnife.findById(itemView, R.id.checkbox);
                divider = ButterKnife.findById(itemView, R.id.divider);
                container = ButterKnife.findById(itemView, R.id.container);
                holderId = TYPE_CONTENT;

                container.setOnClickListener(this);
            } else if (viewType == TYPE_FOOTER) {
                holderId = TYPE_FOOTER;
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = getAdapterPosition();

                if (position < 0 || position > mSettings.size()) return;

                Setting setting = mSettings.get(position);
                switch (setting.getType()) {
                    case CACHE:
                        new MaterialDialog.Builder(mContext)
                                .content(R.string.pref_data_cache_clear_dialog)
                                .positiveText(R.string.clear)
                                .negativeText(android.R.string.cancel)
                                .onPositive((dialog, which) -> {
                                    try {
                                        File cache = mContext.getCacheDir();
                                        FileHelper.clearCache(cache);

                                        double size = (double) FileHelper.getCacheSize(
                                                mContext.getCacheDir()) / FileHelper.MB;
                                        NumberFormat formatter = new DecimalFormat("#0.00");

                                        setting.setFooter(String.format(mContext.getResources().getString(
                                                R.string.pref_data_cache_size),
                                                formatter.format(size) + " MB"));
                                        notifyItemChanged(position);

                                        Toast.makeText(mContext, R.string.pref_data_cache_cleared,
                                                Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        LogUtil.e(Log.getStackTraceString(e));
                                    }
                                })
                                .show();
                        break;
                    case THEME:
                        Preferences.getPreferences(mContext).setDarkTheme(!checkBox.isChecked());
                        ((AppCompatActivity) mContext).recreate();
                        break;
                }
            }
        }
    }
}
