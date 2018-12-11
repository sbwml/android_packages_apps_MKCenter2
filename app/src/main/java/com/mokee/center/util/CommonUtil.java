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

package com.mokee.center.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.SystemProperties;
import androidx.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.lzy.okgo.db.DownloadManager;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;
import com.mokee.center.MKCenterApplication;
import com.mokee.center.R;
import com.mokee.center.controller.UpdaterService;
import com.mokee.center.misc.Constants;
import com.mokee.center.misc.State;
import com.mokee.center.model.DonationInfo;
import com.mokee.center.model.UpdateInfo;
import com.mokee.os.Build;
import com.mokee.security.License;
import com.mokee.security.LicenseInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mokee.center.misc.Constants.ACTION_PAYMENT_REQUEST;

public class CommonUtil {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return !(info == null || !info.isConnected() || !info.isAvailable());
    }

    public static boolean isOnWifiOrEthernet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && (info.getType() == ConnectivityManager.TYPE_ETHERNET
                || info.getType() == ConnectivityManager.TYPE_WIFI));
    }

    public static void cleanupDownloadsDir(Context context) {
        SharedPreferences mMainPrefs = getMainPrefs(context);
        boolean deleteUpdates = mMainPrefs.getBoolean(Constants.PREF_AUTO_DELETE_UPDATES, false);
        if (deleteUpdates) {
            Map<String, DownloadTask> downloadTaskMap = CommonUtil.getDownloadTaskMap();
            for (String version : downloadTaskMap.keySet()) {
                if (!BuildInfoUtil.isCompatible(version)) {
                    downloadTaskMap.get(version).remove(true);
                }
            }
        }
    }

    public static void openLink(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static void updateDonationInfo(Context context) {
        DonationInfo donationInfo = MKCenterApplication.getInstance().getDonationInfo();
        donationInfo.setPaid(getAmountPaid(context).intValue());
        donationInfo.setBasic(donationInfo.getPaid() >= Constants.DONATION_BASIC);
        donationInfo.setAdvanced(donationInfo.getPaid() >= Constants.DONATION_ADVANCED);
    }

    public static Float getAmountPaid(Context context) {
        if (new File(Constants.LICENSE_FILE).exists()) {
            try {
                LicenseInfo licenseInfo = License.readLicense(Constants.LICENSE_FILE, Constants.LICENSE_PUB_KEY);
                String unique_ids = Build.getUniqueIDS(context);
                if (Arrays.asList(unique_ids.split(",")).contains(licenseInfo.getUniqueID())
                        && licenseInfo.getPackageName().equals(context.getPackageName())) {
                    return licenseInfo.getPrice();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return 0f;
        }
        return 0f;
    }

    public static void sendPaymentRequest(Activity context, String channel, String description, String price, String type) {
        Intent intent = new Intent(ACTION_PAYMENT_REQUEST);
        intent.putExtra("packagename", context.getPackageName());
        intent.putExtra("channel", channel);
        intent.putExtra("type", type);
        intent.putExtra("description", description);
        intent.putExtra("price", price);
        context.startActivityForResult(intent, 0);
    }

    public static void restoreLicenseRequest(Activity context) {
        Intent intent = new Intent(Constants.ACTION_RESTORE_REQUEST);
        context.startActivityForResult(intent, 0);
    }

    public static boolean checkForNewUpdates(File oldJson, File newJson) {
        List<UpdateInfo> oldList = State.loadState(oldJson);
        List<UpdateInfo> newList = State.loadState(newJson);
        Set<String> oldUpdates = new HashSet<>();
        for (UpdateInfo update : oldList) {
            oldUpdates.add(update.getName());
        }
        // In case of no new updates, the old list should
        // have all (if not more) the updates
        for (UpdateInfo update : newList) {
            if (!oldUpdates.contains(update.getName())) {
                return true;
            }
        }
        return false;
    }

    public static LinkedList<UpdateInfo> getSortedUpdates(LinkedList<UpdateInfo> updates) {
        Collections.sort(updates, (o1, o2) -> {
            float codeo1 = BuildInfoUtil.getReleaseCode(o1.getName());
            float codeo2 = BuildInfoUtil.getReleaseCode(o2.getName());
            if (codeo2 - codeo1 == 0) {
                return Long.compare(BuildInfoUtil.getBuildDate(o2.getName()), BuildInfoUtil.getBuildDate(o1.getName()));
            } else {
                return Float.compare(codeo1, codeo2);
            }
        });
        return updates;
    }

    public static LinkedList<UpdateInfo> parseJson(Context context, String json, String tag)
            throws JSONException {
        LinkedList<UpdateInfo> updates = new LinkedList<>();
        JSONArray updatesList = new JSONArray(json);
        for (int i = 0; i < updatesList.length(); i++) {
            if (updatesList.isNull(i)) {
                continue;
            }
            try {
                UpdateInfo updateInfo = parseJsonUpdate(context, updatesList.getJSONObject(i));
                if (updateInfo != null) {
                    updates.add(updateInfo);
                }
            } catch (JSONException e) {
                Log.e(tag, "Could not parse update object, index=" + i, e);
            }
        }
        return updates;
    }

    private static UpdateInfo parseJsonUpdate(Context context, JSONObject object) throws JSONException {
        UpdateInfo updateInfo = new UpdateInfo.Builder()
                .setName(object.getString("name"))
                .setDisplayVersion(BuildInfoUtil.getDisplayVersion(context, object.getString("name")))
                .setMD5Sum(object.getString("md5"))
                .setDiffSize(object.getLong("diff"))
                .setFileSize(object.getLong("length"))
                .setTimestamp(object.getLong("timestamp"))
                .setDownloadUrl(object.getString("rom"))
                .setChangelogUrl(object.getString("log")).build();
        return updateInfo;
    }

    public static CharSequence calculateEta(Context context, long speed, long totalBytes, long totalBytesRead) {
        return context.getString(R.string.download_remaining, DateUtils.formatDuration((totalBytes - totalBytesRead) / speed * 1000));
    }

    public static boolean isABDevice() {
        return SystemProperties.getBoolean(Constants.PROP_AB_DEVICE, false);
    }

    public static void triggerUpdate(Context context, String downloadId) {
        final Intent intent = new Intent(context, UpdaterService.class);
        intent.setAction(UpdaterService.ACTION_INSTALL_UPDATE);
        intent.putExtra(UpdaterService.EXTRA_DOWNLOAD_ID, downloadId);
        context.startService(intent);
    }

    public static Map<String, DownloadTask> getDownloadTaskMap() {
        Map<String, DownloadTask> downloadTaskMap = new HashMap<>();
        List<DownloadTask> downloadTasks = OkDownload.restore(DownloadManager.getInstance().getAll());
        for (Iterator iterator = downloadTasks.iterator(); iterator.hasNext(); ) {
            DownloadTask task = (DownloadTask) iterator.next();
            downloadTaskMap.put(task.progress.tag, task);
        }
        return downloadTaskMap;
    }

    public static SharedPreferences getDonationPrefs(Context context) {
        return context.getSharedPreferences(Constants.DONATION_PREF, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getMainPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
