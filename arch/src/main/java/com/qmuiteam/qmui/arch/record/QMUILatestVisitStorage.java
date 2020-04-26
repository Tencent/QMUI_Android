package com.qmuiteam.qmui.arch.record;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface QMUILatestVisitStorage {

    int NOT_EXIST = -1;

    // Fragment stuff
    void saveFragmentRecordInfo(int id, @Nullable Map<String, RecordArgumentEditor.Argument> arguments);

    int getFragmentRecordId();

    @Nullable
    Map<String, RecordArgumentEditor.Argument> getFragmentArguments();


    void clearFragmentStorage();


    // Activity Stuff
    void saveActivityRecordInfo(int id, @Nullable Map<String, RecordArgumentEditor.Argument> arguments);

    int getActivityRecordId();

    void getAndWriteActivityArgumentsToIntent(@NonNull Intent intent);

    void clearActivityStorage();

    void clearAll();
}
