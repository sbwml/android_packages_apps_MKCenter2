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
import android.net.Uri;
import android.support.v7.preference.PreferenceManager;

import com.mokee.center.MKCenterApplication;
import com.mokee.center.misc.Constants;
import com.mokee.center.model.DonationInfo;
import com.mokee.os.Build;
import com.mokee.security.License;
import com.mokee.security.LicenseInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

import static com.mokee.center.misc.Constants.ACTION_PAYMENT_REQUEST;

public class CommonUtils {

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

    public static int getCurrentVersionType() {
        switch (Build.RELEASE_TYPE.toLowerCase(Locale.ENGLISH)) {
            case "nightly":
                return 1;
            case "experimental":
                return 2;
            case "unofficial":
                return 3;
            default:
                return 0;
        }
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

    public static SharedPreferences getDonationPrefs(Context context) {
        return context.getSharedPreferences(Constants.DONATION_PREF, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getMainPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
