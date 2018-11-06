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

package com.mokee.center.controller;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

public class UpdaterController {

    public static final String ACTION_CHECK_FINISHED = "action_check_finished";
    public static final String ACTION_CHECK_START = "action_check_start";

    private static UpdaterController sUpdaterController;

    private final Context mContext;
    private final LocalBroadcastManager mBroadcastManager;

    public static synchronized UpdaterController getInstance() {
        return sUpdaterController;
    }

    private UpdaterController(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mContext = context.getApplicationContext();
    }

    protected static synchronized UpdaterController getInstance(Context context) {
        if (sUpdaterController == null) {
            sUpdaterController = new UpdaterController(context);
        }
        return sUpdaterController;
    }

    void notifyCheckStart() {
        Intent intent = new Intent(ACTION_CHECK_START);
        mBroadcastManager.sendBroadcast(intent);
    }

}
