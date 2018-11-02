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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.Toast;

import com.mokee.center.R;
import com.mokee.os.Build;

public class UniqueIDPreference extends Preference {

    private final ClipboardManager clipboardManager;

    public UniqueIDPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        return Build.getUniqueID(getContext());
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(v -> {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, getSummary()));
            Toast.makeText(getContext(), R.string.text_copied, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

}
