package com.qmuiteam.qmui.arch;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by haoyaun on 2018/1/18.
 */

public class RequestInfo implements Parcelable {
    private final int mResultWho;
    private final int mRequestCode;
    private final Bundle mBundle;

    public RequestInfo(int resultWho, int requestCode, Bundle bundle) {
        this.mResultWho = resultWho;
        this.mRequestCode = requestCode;
        this.mBundle = bundle;
    }

    public int getmResultWho() {
        return mResultWho;
    }

    public int getmRequestCode() {
        return mRequestCode;
    }

    public Bundle getmBundle() {
        return mBundle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResultWho);
        dest.writeInt(this.mRequestCode);
        dest.writeBundle(this.mBundle);
    }

    protected RequestInfo(Parcel in) {
        this.mResultWho = in.readInt();
        this.mRequestCode = in.readInt();
        this.mBundle = in.readBundle();
    }

    public static final Creator<RequestInfo> CREATOR = new Creator<RequestInfo>() {
        @Override
        public RequestInfo createFromParcel(Parcel source) {
            return new RequestInfo(source);
        }

        @Override
        public RequestInfo[] newArray(int size) {
            return new RequestInfo[size];
        }
    };
}
