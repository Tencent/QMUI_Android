package com.qmuiteam.qmuidemo.fragment.lab;

import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.opengl.GLES10.glClearColor;
import static android.opengl.GLES20.glViewport;

//TODO xiaomi 8 surfaceView can not move when swipe back. It's ok in pixel
public class QDArchSurfaceTestFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.container) FrameLayout mContainer;
    private GLSurfaceView mSurfaceView;

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_surface_test, null);
        ButterKnife.bind(this, view);
        mSurfaceView = new GLSurfaceView(getContext());
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                glClearColor(0, 0, 0, 0);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {

            }
        });
        mContainer.addView(mSurfaceView);
        initTopBar();
        return view;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });
        mTopBar.setTitle("Test SurfaceView");
        QDArchTestFragment.injectEntrance(mTopBar);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }
}
