/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qmuiteam.qmuidemo.fragment.components.qqface

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.LineHeightSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.qmuiteam.qmui.span.QMUITouchableSpan
import com.qmuiteam.qmui.type.parser.EmojiTextParser
import com.qmuiteam.qmui.type.parser.TextParser
import com.qmuiteam.qmui.type.view.LineTypeView
import com.qmuiteam.qmui.type.view.MarqueeTypeView
import com.qmuiteam.qmui.util.QMUIColorHelper
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmuidemo.QDQQFaceManager
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.Group
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import java.util.regex.Pattern

/**
 * @author cginechen
 * @date 2016-12-24
 */

class B(val mHeight: Int): LineHeightSpan {
    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, lineHeight: Int, fm: Paint.FontMetricsInt) {

        // 参考官方 API 29 提供的 Standard 而进行修改
        if (fm.descent <= fm.bottom && fm.ascent >= fm.top) {
            if (fm.descent > mHeight) {
                // Show as much descent as possible
                fm.descent = Math.min(mHeight, fm.descent)
                fm.bottom = fm.descent
                fm.ascent = 0
                fm.top = fm.ascent
            } else if (-fm.ascent + fm.descent > mHeight) {
                // Show all descent, and as much ascent as possible
                fm.bottom = fm.descent
                fm.ascent = -mHeight + fm.descent
                fm.top = fm.ascent
            } else {
                // Show proportionally additional ascent / top & descent / bottom
                val additional: Int = mHeight - (-fm.top + fm.bottom)

                // Round up for the negative values and down for the positive values  (arbitrary choice)
                // So that bottom - top equals additional even if it's an odd number.
                fm.top -= Math.ceil((additional / 2.0f).toDouble()).toInt()
                fm.bottom += Math.floor((additional / 2.0f).toDouble()).toInt()
                fm.ascent = fm.top
                fm.descent = fm.bottom
            }
        } else {
            val originHeight = fm.descent - fm.ascent
            // If original height is not positive, do nothing.
            if (originHeight <= 0) {
                return
            }
            if (originHeight < mHeight) {
                // Show proportionally additional ascent / top & descent / bottom
                val additional: Int = mHeight - originHeight

                // Round up for the negative values and down for the positive values  (arbitrary choice)
                // So that bottom - top equals additional even if it's an odd number.
                fm.ascent -= Math.ceil((additional / 2.0f).toDouble()).toInt()
                fm.top = fm.ascent
                fm.descent += Math.floor((additional / 2.0f).toDouble()).toInt()
                fm.bottom = fm.descent
            } else {
                var ratio: Float = mHeight * 1.0f / originHeight
                fm.descent = Math.round(fm.descent * ratio)
                fm.ascent = fm.descent - mHeight
                ratio = mHeight * 1.0f / (fm.bottom - fm.top)
                fm.bottom = Math.round(fm.bottom * ratio)
                fm.top = fm.bottom - mHeight
            }
        }
    }

}

class Test(context: Context, attrs: AttributeSet): TextView(context, attrs){

    init {
        setBackgroundColor(Color.RED)
        text = SpannableString("呵呵བོད་སྐད").apply {
            setSpan(B(80), 0, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(80, MeasureSpec.EXACTLY))
    }
}

@Widget(group = Group.Other, name = "QQ表情使用展示")
@LatestVisitRecord
class QDQQFaceUsageFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.topbar)
    var mTopBar: QMUITopBarLayout? = null

    @JvmField
    @BindView(R.id.marquee1)
    var mMarqueeTypeView1: MarqueeTypeView? = null

    @JvmField
    @BindView(R.id.marquee2)
    var mMarqueeTypeView2: MarqueeTypeView? = null

    @JvmField
    @BindView(R.id.line_type_1)
    var mLineType1: LineTypeView? = null


    @JvmField
    @BindView(R.id.qqface1)
    var mQQFace1: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface2)
    var mQQFace2: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface3)
    var mQQFace3: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface4)
    var mQQFace4: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface5)
    var mQQFace5: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface6)
    var mQQFace6: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface7)
    var mQQFace7: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface8)
    var mQQFace8: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface9)
    var mQQFace9: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface10)
    var mQQFace10: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface11)
    var mQQFace11: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface12)
    var mQQFace12: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface13)
    var mQQFace13: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface14)
    var mQQFace14: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface15)
    var mQQFace15: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface16)
    var mQQFace16: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface17)
    var mQQFace17: QMUIQQFaceView? = null

    @JvmField
    @BindView(R.id.qqface18)
    var mQQFace18: QMUIQQFaceView? = null
    override fun onCreateView(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_qqface_layout, null)
        ButterKnife.bind(this, view)
        initTopBar()
        initData()
        return view
    }

    private fun initTopBar() {
        mTopBar!!.addLeftBackImageButton().setOnClickListener { popBackStack() }
        mTopBar!!.setTitle(QDDataManager.getInstance().getName(this.javaClass))
    }

    private fun initData() {
        val textParser: TextParser = EmojiTextParser(QDQQFaceManager.getInstance())
        mMarqueeTypeView1!!.fadeWidth = QMUIDisplayHelper.dp2px(context, 40).toFloat()
        mMarqueeTypeView1!!.textParser = textParser
        mMarqueeTypeView1!!.text = "🙃🙃🙃🙃飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀飘呀这是一行很长很长[微笑][微笑][微笑][微笑]的文本，但是[微笑][微笑][微笑][微笑]只能单行显示"
        mMarqueeTypeView2!!.fadeWidth = QMUIDisplayHelper.dp2px(context, 40).toFloat()
        mMarqueeTypeView2!!.textParser = textParser
        mMarqueeTypeView2!!.text = "[大哭]我太短了，实在是飘不动了"
        mLineType1!!.textParser = textParser
        val lineLayout = mLineType1!!.lineLayout
        lineLayout.maxLines = 6
        lineLayout.ellipsize = TextUtils.TruncateAt.END
        lineLayout.moreText = "更多"
        lineLayout.moreUnderlineHeight = QMUIDisplayHelper.dp2px(context, 2)
        lineLayout.moreTextColor = Color.RED
        lineLayout.moreUnderlineColor = Color.BLUE
        mLineType1!!.lineHeight = QMUIDisplayHelper.dp2px(context, 36)
        mLineType1!!.textColor = Color.BLACK
        mLineType1!!.textSize = QMUIDisplayHelper.sp2px(context, 15).toFloat()
        mLineType1!!.text = "QMUI Android 的设计[微笑]目的🙃🙃🙃🙃是用于辅助快速搭建一个具备基本设计还原[微笑]效果的 Android 项目，" +
                "同时利用自身[微笑]提供的丰富控件及兼容处理，让开[微笑]发者能专注于业务需求而无需耗费[微笑]精力在基础代[微笑]码的设计上。" +
                "不管是新项目的创建，或是已有项[微笑]目的维护，均可使开[微笑]发效率和项目[微笑]质量得到大幅度提升。"
        mLineType1!!.addBgEffect(10, 16, QMUIColorHelper.setColorAlpha(Color.RED, 0.5f))

        mLineType1!!.onClick {
            Toast.makeText(context, "你点整个 LineTypeView 干嘛", Toast.LENGTH_SHORT).show()
        }

        mQQFace1!!.text = "这是一行很长很长[微笑][微笑][微笑][微笑]的文本，但是[微笑][微笑][微笑][微笑]只能单行显示"
        mQQFace2!!.text = "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行。"
        mQQFace3!!.text = "这是一行很长很长[微笑][微笑][微笑][微笑]的文本，但是[微笑][微笑][微笑][微笑]只能单行显示"
        mQQFace4!!.text = "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行。"
        mQQFace5!!.text = "这是一行很长很长[微笑][微笑][微笑][微笑]的文本，但是[微笑][微笑][微笑][微笑]只能单行显示"
        mQQFace6!!.text = "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行。"
        mQQFace7!!.text = "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]"
        mQQFace8!!.text = "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]"
        mQQFace9!!.text = "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]"
        mQQFace10!!.text = "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]"
        mQQFace11!!.text = "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]"
        mQQFace12!!.text = "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]"
        mQQFace13!!.text = "表情可以和字体一起变大[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]"
        val topic = "#[发呆][微笑]话题"
        val text = "这是一段文本，为了测量 span 的点击在不同 Gravity 下能否正常工作。$topic"
        val sb = SpannableString(text)
        val span: QMUITouchableSpan = object : QMUITouchableSpan(
            mQQFace14,
            R.attr.app_skin_span_normal_text_color,
            R.attr.app_skin_span_pressed_text_color,
            R.attr.app_skin_span_normal_bg_color,
            R.attr.app_skin_span_pressed_bg_color
        ) {
            override fun onSpanClick(widget: View) {
                Toast.makeText(widget.context, "点击了话题", Toast.LENGTH_SHORT).show()
            }
        }
        span.setIsNeedUnderline(true)
        sb.setSpan(span, text.indexOf(topic), text.indexOf(topic) + topic.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        mQQFace14!!.text = sb
        mQQFace15!!.text = sb
        mQQFace15!!.setLinkUnderLineColor(Color.RED)
        mQQFace16!!.text = sb
        mQQFace16!!.setLinkUnderLineHeight(QMUIDisplayHelper.dp2px(context, 4))
        mQQFace16!!.setLinkUnderLineColor(ContextCompat.getColorStateList(requireContext(), R.color.s_app_color_blue_to_red))
        mQQFace15!!.gravity = Gravity.CENTER
        mQQFace16!!.gravity = Gravity.RIGHT
        mQQFace17!!.setLinkUnderLineColor(Color.RED)
        mQQFace17!!.setNeedUnderlineForMoreText(true)
        mQQFace17!!.text = "这是一段文本，为了测量更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多" +
                "更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多" +
                "更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多的显示情况"
        mQQFace18!!.setParagraphSpace(QMUIDisplayHelper.dp2px(context, 20))
        mQQFace18!!.text = """
            这是一段文本，为[微笑]了测量多段落[微笑]
            这是一段文本，为[微笑]了测量多段落[微笑]
            这是一段文本，为[微笑]了测量多段落[微笑]
            """.trimIndent()
    }
}