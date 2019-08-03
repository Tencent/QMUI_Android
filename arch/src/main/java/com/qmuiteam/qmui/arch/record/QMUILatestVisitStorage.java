package com.qmuiteam.qmui.arch.record;

import android.support.annotation.NonNull;

public interface QMUILatestVisitStorage {

    int NOT_EXIST = -1;

    // Fragment stuff
    void saveFragmentRecordInfo(@NonNull RecordInfo recordInfo);

    int getFragmentRecordId();

    String getFragmentStringArgument(String key, String defValue);

    int getFragmentIntArgument(String key, int defValue);

    long getFragmentLongArgument(String key, long defValue);

    float getFragmentFloatArgument(String key, float defValue);

    boolean getFragmentBoolArgument(String key, boolean defValue);


    // Activity Stuff
    void saveActivityRecordInfo(@NonNull RecordInfo recordInfo);

    int getActivityRecordId();

    String getActivityStringArgument(String key, String defValue);

    int getActivityIntArgument(String key, int defValue);

    long getActivityLongArgument(String key, long defValue);

    float getActivityFloatArgument(String key, float defValue);

    boolean getActivityBoolArgument(String key, boolean defValue);
}
