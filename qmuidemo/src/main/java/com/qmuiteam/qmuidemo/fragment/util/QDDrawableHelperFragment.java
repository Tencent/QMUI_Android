package com.qmuiteam.qmuidemo.fragment.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIDrawableHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kayo on 2016/12/5.
 */

@Widget(group = Group.Helper, widgetClass = QMUIDrawableHelper.class, iconRes = R.mipmap.icon_grid_drawable_helper)
public class QDDrawableHelperFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.createFromView) Button mCreateFromViewButton;
    @BindView(R.id.solidImage) ImageView mSolidImageView;
    @BindView(R.id.circleGradient) ImageView mCircleGradientView;
    @BindView(R.id.tintColor) ImageView mTintColorImageView;
    @BindView(R.id.tintColorOrigin) ImageView mTintColorOriginImageView;
    @BindView(R.id.separator) View mSeparatorView;

    private View mRootView;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        mRootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_drawablehelper, null);
        ButterKnife.bind(this, mRootView);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        initContent();

        return mRootView;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());
    }

    private void initContent() {
        int commonShapeSize = getResources().getDimensionPixelSize(R.dimen.drawableHelper_common_shape_size);
        int commonShapeRadius = QMUIDisplayHelper.dp2px(getContext(), 10);

        // 创建一张指定大小的纯色图片，支持圆角
        BitmapDrawable solidImageBitmapDrawable = QMUIDrawableHelper.createDrawableWithSize(getResources(), commonShapeSize, commonShapeSize, commonShapeRadius, ContextCompat.getColor(getContext(), R.color.app_color_theme_3));
        mSolidImageView.setImageDrawable(solidImageBitmapDrawable);

        // 创建一张圆形渐变图片，支持圆角
        GradientDrawable gradientCircleGradientDrawable = QMUIDrawableHelper.createCircleGradientDrawable(ContextCompat.getColor(getContext(), R.color.app_color_theme_4),
                ContextCompat.getColor(getContext(), R.color.qmui_config_color_transparent), commonShapeRadius, 0.5f, 0.5f);
        mCircleGradientView.setImageDrawable(gradientCircleGradientDrawable);

        // 设置 Drawable 的颜色
        // 创建两张表现相同的图片
        BitmapDrawable tintColorBitmapDrawble = QMUIDrawableHelper.createDrawableWithSize(getResources(), commonShapeSize, commonShapeSize, commonShapeRadius, ContextCompat.getColor(getContext(), R.color.app_color_theme_1));
        BitmapDrawable tintColorOriginBitmapDrawble = QMUIDrawableHelper.createDrawableWithSize(getResources(), commonShapeSize, commonShapeSize, commonShapeRadius, ContextCompat.getColor(getContext(), R.color.app_color_theme_1));
        // 其中一张重新设置颜色
        QMUIDrawableHelper.setDrawableTintColor(tintColorBitmapDrawble, ContextCompat.getColor(getContext(), R.color.app_color_theme_7));
        mTintColorImageView.setImageDrawable(tintColorBitmapDrawble);
        mTintColorOriginImageView.setImageDrawable(tintColorOriginBitmapDrawble);

        // 创建带上分隔线或下分隔线的 Drawable
        LayerDrawable separatorLayerDrawable = QMUIDrawableHelper.createItemSeparatorBg(ContextCompat.getColor(getContext(), R.color.app_color_theme_7),
                ContextCompat.getColor(getContext(), R.color.app_color_theme_6), QMUIDisplayHelper.dp2px(getContext(), 2), true);
        QMUIViewHelper.setBackgroundKeepingPadding(mSeparatorView, separatorLayerDrawable);

        // 从一个 View 创建 Bitmap
        mCreateFromViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QMUIDialog.CustomDialogBuilder dialogBuilder = new QMUIDialog.CustomDialogBuilder(getContext());
                dialogBuilder.setLayout(R.layout.drawablehelper_createfromview);
                final QMUIDialog dialog = dialogBuilder.setTitle("示例效果（点击下图关闭本浮层）").create();
                ImageView displayImageView = (ImageView) dialog.findViewById(R.id.createFromViewDisplay);
                Bitmap createFromViewBitmap = QMUIDrawableHelper.createBitmapFromView(mRootView);
                displayImageView.setImageBitmap(createFromViewBitmap);

                displayImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }
}
