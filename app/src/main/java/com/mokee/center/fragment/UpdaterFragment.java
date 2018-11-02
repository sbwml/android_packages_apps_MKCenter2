/*
 * Copyright (C) 2018 The MoKee Open Source Project
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


package com.mokee.center.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import com.mokee.center.MKCenterApplication;
import com.mokee.center.R;
import com.mokee.center.preference.DonationRecordPreference;
import com.mokee.center.preference.IncrementalUpdatesPreference;
import com.mokee.center.preference.VerifiedUpdatesPreference;
import com.mokee.center.util.CommonUtils;

import static com.mokee.center.misc.Constants.DONATION_RESULT_OK;
import static com.mokee.center.misc.Constants.DONATION_RESULT_SUCCESS;
import static com.mokee.center.misc.Constants.KEY_DONATION_AMOUNT;
import static com.mokee.center.misc.Constants.PREF_DONATION_RECORD;
import static com.mokee.center.misc.Constants.PREF_INCREMENTAL_UPDATES;
import static com.mokee.center.misc.Constants.PREF_VERIFIED_UPDATES;

public class UpdaterFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mDonationPrefs;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.updater);
        setHasOptionsMenu(true);
        mDonationPrefs = CommonUtils.getDonationPrefs(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        mDonationPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case DONATION_RESULT_OK:
            case DONATION_RESULT_SUCCESS:
                CommonUtils.updateDonationInfo(getContext());
                ((DonationRecordPreference) findPreference(PREF_DONATION_RECORD)).updateRankInfo();
                ((IncrementalUpdatesPreference)findPreference(PREF_INCREMENTAL_UPDATES)).updateStatus();
                ((VerifiedUpdatesPreference)findPreference(PREF_VERIFIED_UPDATES)).updateStatus();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_restore:
                CommonUtils.restoreLicenseRequest(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_DONATION_AMOUNT)) {
            if (mDonationPrefs.getInt(KEY_DONATION_AMOUNT, 0)
                    > MKCenterApplication.getInstance().getDonationInfo().getPaid()) {
                CommonUtils.restoreLicenseRequest(getActivity());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mDonationPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}
