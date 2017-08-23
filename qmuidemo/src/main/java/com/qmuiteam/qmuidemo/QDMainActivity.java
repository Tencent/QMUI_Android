package com.qmuiteam.qmuidemo;

import android.os.Bundle;

import com.qmuiteam.qmuidemo.base.BaseFragmentActivity;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.home.HomeFragment;

public class QDMainActivity extends BaseFragmentActivity {

	@Override
	protected int getContextViewId() {
		return R.id.qmuidemo;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			BaseFragment fragment = new HomeFragment();

			getSupportFragmentManager()
					.beginTransaction()
					.add(getContextViewId(), fragment, fragment.getClass().getSimpleName())
					.addToBackStack(fragment.getClass().getSimpleName())
					.commit();
		}
	}
}
