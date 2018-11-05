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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.mokee.center.MKCenterApplication;
import com.mokee.center.R;
import com.mokee.center.preference.DonationRecordPreference;
import com.mokee.center.preference.IncrementalUpdatesPreference;
import com.mokee.center.preference.VerifiedUpdatesPreference;
import com.mokee.center.util.CommonUtils;
import com.mokee.center.util.RequestUtils;

import static com.mokee.center.misc.Constants.DONATION_RESULT_OK;
import static com.mokee.center.misc.Constants.DONATION_RESULT_SUCCESS;
import static com.mokee.center.misc.Constants.KEY_DONATION_AMOUNT;
import static com.mokee.center.misc.Constants.PREF_DONATION_RECORD;
import static com.mokee.center.misc.Constants.PREF_INCREMENTAL_UPDATES;
import static com.mokee.center.misc.Constants.PREF_VERIFIED_UPDATES;

public class UpdaterFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mDonationPrefs;

    private InterstitialAd mWelcomeInterstitialAd;

    private View mRefreshIconView;
    private RotateAnimation mRefreshAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!MKCenterApplication.getInstance().getDonationInfo().isAdvanced()) {
            MobileAds.initialize(getContext(), getString(R.string.app_id));
            mWelcomeInterstitialAd = new InterstitialAd(getContext());
            mWelcomeInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
            AdRequest adRequest = new AdRequest.Builder().build();
            mWelcomeInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mWelcomeInterstitialAd.show();
                }
            });
            mWelcomeInterstitialAd.loadAd(adRequest);
        }

        mRefreshAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRefreshAnimation.setInterpolator(new LinearInterpolator());
        mRefreshAnimation.setDuration(1000);
    }

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
                ((IncrementalUpdatesPreference) findPreference(PREF_INCREMENTAL_UPDATES)).updateStatus();
                ((VerifiedUpdatesPreference) findPreference(PREF_VERIFIED_UPDATES)).updateStatus();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                downloadUpdatesList();
                return true;
            case R.id.action_restore:
                CommonUtils.restoreLicenseRequest(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadUpdatesList() {
        RequestUtils.fetchAvailableUpdates(getContext(), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
            }

            @Override
            public void onFinish() {
                super.onFinish();
                refreshAnimationStop();
            }
        });
        refreshAnimationStart();
    }

    private void refreshAnimationStart() {
        if (mRefreshIconView == null) {
            mRefreshIconView = getActivity().findViewById(R.id.action_refresh);
        }
        if (mRefreshIconView != null) {
            mRefreshAnimation.setRepeatCount(Animation.INFINITE);
            mRefreshIconView.startAnimation(mRefreshAnimation);
            mRefreshIconView.setEnabled(false);
        }
    }

    private void refreshAnimationStop() {
        if (mRefreshIconView != null) {
            mRefreshAnimation.setRepeatCount(0);
            mRefreshIconView.setEnabled(true);
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
