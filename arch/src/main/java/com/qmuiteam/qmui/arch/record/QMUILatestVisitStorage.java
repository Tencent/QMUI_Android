package com.qmuiteam.qmui.arch.record;

import android.support.annotation.NonNull;

public interface QMUILatestVisitStorage {

    int NOT_EXIST = -1;

    // Fragment stuff
    void saveFragmentRecordInfo(@NonNull RecordInfo recordInfo);

    int getFragmentRecordId();

    String getFragmentStringArgument(String key);

    Integer getFragmentIntArgument(String key);

    Long getFragmentLongArgument(String key);

    Float getFragmentFloatArgument(String key);

    Boolean getFragmentBoolArgument(String key);

    void clearFragmentStorage();


    // Activity Stuff
    void saveActivityRecordInfo(@NonNull RecordInfo recordInfo);

    int getActivityRecordId();

    String getActivityStringArgument(String key);

    Integer getActivityIntArgument(String key);

    Long getActivityLongArgument(String key);

    Float getActivityFloatArgument(String key);

    Boolean getActivityBoolArgument(String key);

    void clearActivityStorage();
}
