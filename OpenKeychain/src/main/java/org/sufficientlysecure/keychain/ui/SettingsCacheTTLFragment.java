/*
 * Copyright (C) 2012-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2015 Adithya Abraham Philip <adithyaphilip@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui;


import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.ui.util.Notify;
import org.sufficientlysecure.keychain.ui.util.Notify.Style;
import org.sufficientlysecure.keychain.ui.util.recyclerview.DividerItemDecoration;
import org.sufficientlysecure.keychain.util.Preferences.CacheTTLPrefs;


public class SettingsCacheTTLFragment extends Fragment {

    public static final String ARG_TTL_PREFS = "ttl_prefs";

    private CacheTTLListAdapter mAdapter;

    public static SettingsCacheTTLFragment newInstance(CacheTTLPrefs ttlPrefs) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TTL_PREFS, ttlPrefs);

        SettingsCacheTTLFragment fragment = new SettingsCacheTTLFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        return inflater.inflate(R.layout.settings_cache_ttl_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CacheTTLPrefs prefs = (CacheTTLPrefs) getArguments().getSerializable(ARG_TTL_PREFS);

        mAdapter = new CacheTTLListAdapter(prefs);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.cache_ttl_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));


    }

    private void saveKeyserverList() {
        // Preferences.getPreferences(getActivity()).setKeyServers(servers);
    }

    public class CacheTTLListAdapter extends RecyclerView.Adapter<CacheTTLListAdapter.ViewHolder> {

        private final ArrayList<Boolean> mPositionIsChecked;
        private int mDefaultPosition;

        public CacheTTLListAdapter(CacheTTLPrefs prefs) {
            this.mPositionIsChecked = new ArrayList<>();
            for (int ttlTime : CacheTTLPrefs.CACHE_TTLS) {
                mPositionIsChecked.add(prefs.ttlTimes.contains(ttlTime));
                if (ttlTime == prefs.defaultTtl) {
                    mDefaultPosition = mPositionIsChecked.size() -1;
                }
            }

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.settings_cache_ttl_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return mPositionIsChecked.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            CheckBox mChecked;
            TextView mTitle;
            RadioButton mIsDefault;

            public ViewHolder(View itemView) {
                super(itemView);
                mChecked = (CheckBox) itemView.findViewById(R.id.ttl_selected);
                mTitle = (TextView) itemView.findViewById(R.id.ttl_title);
                mIsDefault = (RadioButton) itemView.findViewById(R.id.ttl_default);

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mChecked.performClick();
                    }
                });
            }

            public void bind(final int position) {

                int ttl = CacheTTLPrefs.CACHE_TTLS.get(position);
                boolean isChecked = mPositionIsChecked.get(position);
                boolean isDefault = position == mDefaultPosition;

                mTitle.setText(CacheTTLPrefs.CACHE_TTL_NAMES.get(ttl));
                mChecked.setChecked(isChecked);
                mIsDefault.setEnabled(isChecked);
                mIsDefault.setChecked(isDefault);

                mChecked.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTtlChecked(position);
                    }
                });

                mIsDefault.setOnClickListener(!isChecked ? null : new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setDefault(position);
                    }
                });

            }

            private void setTtlChecked(int position) {
                boolean isChecked = mPositionIsChecked.get(position);
                int checkedItems = countCheckedItems();

                boolean isLastChecked = isChecked && checkedItems == 1;
                boolean isOneTooMany = !isChecked && checkedItems >= 3;
                if (isLastChecked) {
                    Notify.create(getActivity(), R.string.settings_cache_ttl_at_least_one, Style.ERROR).show();
                } else if (isOneTooMany) {
                    Notify.create(getActivity(), R.string.settings_cache_ttl_max_three, Style.ERROR).show();
                } else {
                    mPositionIsChecked.set(position, !isChecked);
                    repositionDefault();
                }
                notifyItemChanged(position);
            }

            private void repositionDefault() {
                boolean defaultPositionIsChecked = mPositionIsChecked.get(mDefaultPosition);
                if (defaultPositionIsChecked) {
                    return;
                }

                // prefer moving default up
                int i = mDefaultPosition;
                while (--i >= 0) {
                    if (mPositionIsChecked.get(i)) {
                        setDefault(i);
                        return;
                    }
                }

                // if that didn't work, move it down
                i = mDefaultPosition;
                while (++i < mPositionIsChecked.size()) {
                    if (mPositionIsChecked.get(i)) {
                        setDefault(i);
                        return;
                    }
                }

                // we should never get here - if we do, leave default as is (there is a sanity check in the
                // set preference method, so no biggie)

            }

            private void setDefault(int position) {
                int previousDefaultPosition = mDefaultPosition;
                mDefaultPosition = position;
                notifyItemChanged(previousDefaultPosition);
                notifyItemChanged(mDefaultPosition);
            }

            private int countCheckedItems() {
                int result = 0;
                for (boolean isChecked : mPositionIsChecked) {
                    if (isChecked) {
                        result += 1;
                    }
                }
                return result;
            }

        }

    }
}
