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

package com.mokee.center.preference;

import android.content.Context;
import android.support.v7.preference.Preference;

import com.mokee.center.R;

public class EmptyListPreference extends Preference {
    public EmptyListPreference(Context context) {
        super(context);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        setEnabled(false);
        setLayoutResource(R.layout.preference_empty_list);
        setSummary(R.string.no_available_updates_intro);
    }

}
