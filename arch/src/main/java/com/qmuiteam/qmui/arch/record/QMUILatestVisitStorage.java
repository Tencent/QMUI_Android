package com.qmuiteam.qmui.arch.record;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

public interface QMUILatestVisitStorage {

    int NOT_EXIST = -1;

    // Fragment stuff
    void saveFragmentRecordInfo(int id, @Nullable Map<String, RecordArgumentEditor.Argument> arguments);

    int getFragmentRecordId();

    void getAndWriteFragmentArgumentsToBundle(@NonNull Bundle bundle);

    void clearFragmentStorage();


    // Activity Stuff
    void saveActivityRecordInfo(int id, @Nullable Map<String, RecordArgumentEditor.Argument> arguments);

    int getActivityRecordId();

    void getAndWriteActivityArgumentsToIntent(@NonNull Intent intent);

    void clearActivityStorage();

    void clearAll();
}
