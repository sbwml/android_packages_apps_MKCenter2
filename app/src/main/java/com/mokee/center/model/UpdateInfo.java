/*
 * Copyright (C) 2014-2018 The MoKee Open Source Project
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

package com.mokee.center.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UpdateInfo implements Parcelable, Serializable {

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };

    private static final long serialVersionUID = 5499890003569313403L;
    private String mName;
    private String mMD5Sum;
    private long mDiffSize;
    private long mFileSize;
    private long mTimestamp;
    private String mDownloadUrl;
    private String mChangelogUrl;

    private UpdateInfo() {
    }

    private UpdateInfo(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        return mName;
    }

    public String getMD5Sum() {
        return mMD5Sum;
    }

    public long getDiffSize() {
        return mDiffSize;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public String getChangelogUrl() {
        return mChangelogUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mMD5Sum);
        dest.writeLong(mDiffSize);
        dest.writeLong(mFileSize);
        dest.writeLong(mTimestamp);
        dest.writeString(mDownloadUrl);
        dest.writeString(mChangelogUrl);
    }

    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mMD5Sum = in.readString();
        mDiffSize = in.readLong();
        mFileSize = in.readLong();
        mTimestamp = in.readLong();
        mDownloadUrl = in.readString();
        mChangelogUrl = in.readString();
    }

    public static class Builder {
        private String mName;
        private String mMD5Sum;
        private long mDiffSize;
        private long mFileSize;
        private long mTimestamp;
        private String mDownloadUrl;
        private String mChangelogUrl;

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setMD5Sum(String md5Sum) {
            mMD5Sum = md5Sum;
            return this;
        }

        public Builder setDiffSize(long diffSize) {
            mDiffSize = diffSize;
            return this;
        }

        public Builder setFileSize(long fileSize) {
            mFileSize = fileSize;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public Builder setChangelogUrl(String changelogUrl) {
            mChangelogUrl = changelogUrl;
            return this;
        }

        public Builder setDownloadUrl(String downloadUrl) {
            mDownloadUrl = downloadUrl;
            return this;
        }

        public UpdateInfo build() {
            UpdateInfo info = new UpdateInfo();
            info.mName = mName;
            info.mMD5Sum = mMD5Sum;
            info.mDiffSize = mDiffSize;
            info.mFileSize = mFileSize;
            info.mTimestamp = mTimestamp;
            info.mChangelogUrl = mChangelogUrl;
            info.mDownloadUrl = mDownloadUrl;
            return info;
        }
    }
}
