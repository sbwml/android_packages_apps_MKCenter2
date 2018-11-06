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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.mokee.center.activity.MainActivity;
import com.mokee.center.controller.UpdaterController;
import com.mokee.center.misc.Constants;
import com.mokee.center.misc.State;
import com.mokee.center.model.UpdateInfo;
import com.mokee.center.preference.DonationRecordPreference;
import com.mokee.center.preference.IncrementalUpdatesPreference;
import com.mokee.center.preference.LastUpdateCheckPreference;
import com.mokee.center.preference.VerifiedUpdatesPreference;
import com.mokee.center.receiver.UpdatesCheckReceiver;
import com.mokee.center.util.CommonUtils;
import com.mokee.center.util.RequestUtils;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.mokee.center.misc.Constants.DONATION_RESULT_OK;
import static com.mokee.center.misc.Constants.DONATION_RESULT_SUCCESS;
import static com.mokee.center.misc.Constants.KEY_DONATION_AMOUNT;
import static com.mokee.center.misc.Constants.PREF_DONATION_RECORD;
import static com.mokee.center.misc.Constants.PREF_INCREMENTAL_UPDATES;
import static com.mokee.center.misc.Constants.PREF_LAST_UPDATE_CHECK;
import static com.mokee.center.misc.Constants.PREF_VERIFIED_UPDATES;

public class UpdaterFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = UpdaterFragment.class.getName();

    private BroadcastReceiver mBroadcastReceiver;

    private MainActivity mMainActivity;

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

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                if (UpdaterController.ACTION_CHECK_FINISHED.equals(intent.getAction())) {
//
//                } else if (UpdaterController.ACTION_CHECK_START.equals(intent.getAction())) {
//                    downloadUpdatesList();
//                }
            }
        };
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.updater);
        setHasOptionsMenu(true);
        mDonationPrefs = CommonUtils.getDonationPrefs(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDonationPrefs.unregisterOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDonationPrefs.registerOnSharedPreferenceChangeListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdaterController.ACTION_CHECK_START);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver, intentFilter);
        return super.onCreateView(inflater, container, savedInstanceState);
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
                downloadUpdatesList(true);
                return true;
            case R.id.action_restore:
                CommonUtils.restoreLicenseRequest(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    private void loadUpdatesList(LinkedList<UpdateInfo> updates, boolean manualRefresh)
//            throws IOException, JSONException {
//        Log.d(TAG, "Adding remote updates");
//        UpdaterController controller = mUpdaterService.getUpdaterController();
//        boolean newUpdates = false;
//
//        List<String> updatesOnline = new ArrayList<>();
//        for (UpdateInfo update : updates) {
//            newUpdates |= controller.addUpdate(update);
//            updatesOnline.add(update.getDownloadId());
//        }
//        controller.setUpdatesAvailableOnline(updatesOnline, true);
//
//        if (manualRefresh) {
//            showSnackbar(
//                    newUpdates ? R.string.snack_updates_found : R.string.snack_no_updates_found,
//                    Snackbar.LENGTH_SHORT);
//        }
//
//        List<String> updateIds = new ArrayList<>();
//        List<UpdateInfo> sortedUpdates = controller.getUpdates();
//        if (sortedUpdates.isEmpty()) {
//            findViewById(R.id.no_new_updates_view).setVisibility(View.VISIBLE);
//            findViewById(R.id.recycler_view).setVisibility(View.GONE);
//        } else {
//            findViewById(R.id.no_new_updates_view).setVisibility(View.GONE);
//            findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);
//            sortedUpdates.sort((u1, u2) -> Long.compare(u2.getTimestamp(), u1.getTimestamp()));
//            for (UpdateInfo update : sortedUpdates) {
//                updateIds.add(update.getDownloadId());
//            }
//            mAdapter.setData(updateIds);
//            mAdapter.notifyDataSetChanged();
//        }
//    }

    private void processNewJson(Response<String> response, File json, File jsonNew, boolean manualRefresh) {
        try {
            final LinkedList<UpdateInfo> updates = CommonUtils.parseJson(response.body(), TAG);
            State.saveState(updates, jsonNew);
//            loadUpdatesList(updates, manualRefresh);
            SharedPreferences preferences = CommonUtils.getMainPrefs(getContext());
            preferences.edit().putLong(Constants.PREF_LAST_UPDATE_CHECK, System.currentTimeMillis()).apply();
            ((LastUpdateCheckPreference)findPreference(PREF_LAST_UPDATE_CHECK)).updateSummary();
            if (json.exists() && preferences.getBoolean(Constants.PREF_AUTO_UPDATES_CHECK, true)
                    && CommonUtils.checkForNewUpdates(json, jsonNew)) {
                UpdatesCheckReceiver.updateRepeatingUpdatesCheck(getContext());
            }
            // In case we set a one-shot check because of a previous failure
            UpdatesCheckReceiver.cancelUpdatesCheck(getContext());
            jsonNew.renameTo(json);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Could not read json", e);
            mMainActivity.makeSnackbar(R.string.updates_check_failed).show();
        }
    }

    private void downloadUpdatesList(final boolean manualRefresh) {
        final File json = CommonUtils.getCachedUpdateList(getContext());
        final File jsonNew = new File(json.getAbsolutePath() + UUID.randomUUID());
        RequestUtils.fetchAvailableUpdates(getContext(), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                processNewJson(response, json, jsonNew, manualRefresh);
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                mMainActivity.makeSnackbar(R.string.updates_check_failed).show();
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
            findPreference(PREF_INCREMENTAL_UPDATES).setEnabled(false);
            findPreference(PREF_VERIFIED_UPDATES).setEnabled(false);
        }
    }

    private void refreshAnimationStop() {
        if (mRefreshIconView != null) {
            mRefreshAnimation.setRepeatCount(0);
            mRefreshIconView.setEnabled(true);
            findPreference(PREF_INCREMENTAL_UPDATES).setEnabled(true);
            findPreference(PREF_VERIFIED_UPDATES).setEnabled(true);
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
}
