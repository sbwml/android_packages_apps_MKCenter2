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

package com.mokee.center;

import android.app.Application;

import com.mokee.center.model.DonationInfo;
import com.mokee.center.util.CommonUtils;

public class MKCenterApplication extends Application {

    private static MKCenterApplication mApp;

    private DonationInfo mDonationInfo = new DonationInfo();

    public static MKCenterApplication getInstance() {
        return mApp;
    }

    public DonationInfo getDonationInfo() {
        return mDonationInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        CommonUtils.updateDonationInfo(this);
    }

}
