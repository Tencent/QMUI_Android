package com.qmuiteam.qmuidemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.qmuiteam.qmuidemo.QDMainActivity;

/**
 * @author cginechen
 * @date 2016-12-08
 */

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        Intent intent = new Intent(this, QDMainActivity.class);
        startActivity(intent);
        finish();
    }
}
