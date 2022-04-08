package com.qmuiteam.qmuidemo.fragment.lab

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmui.kotlin.*
import com.qmuiteam.qmui.type.parser.EmojiTextParser
import com.qmuiteam.qmui.type.view.EmojiEditText
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmuidemo.QDQQFaceManager
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget

@Widget(name = "EmojiEditText", iconRes = R.mipmap.icon_grid_in_progress)
@LatestVisitRecord
class QDEmojiInputFragment : BaseFragment() {
    override fun onCreateView(): View {
        return EmojiLayout(requireContext())
    }
}

class EmojiLayout(context: Context): ConstraintLayout(context){
    val topBarLayout = QMUITopBarLayout(context).apply {
        fitsSystemWindows = true
        id = View.generateViewId()
    }
    val editText = EmojiEditText(context).apply {
        gravity = Gravity.TOP or Gravity.LEFT
        textParser = EmojiTextParser(QDQQFaceManager.getInstance()) { true }
    }

    val se = TextView(context).apply {
        text = "[色]"
        setPadding(0, dip(20), 0, dip(20))
        onClick {
            editText.replaceSelection("[色]")
        }
    }
    val weixiao = TextView(context).apply {
        text = "[微笑]"
        setPadding(0, dip(20), 0, dip(20))
        onClick {
            editText.replaceSelection("[微笑]")
        }
    }
    val daku = TextView(context).apply {
        text = "[大哭]"
        setPadding(0, dip(20), 0, dip(20))
        onClick {
            editText.replaceSelection("[大哭]")
        }
    }
    val delete = TextView(context).apply {
        text = "delete"
        setPadding(0, dip(20), 0, dip(20))
        onClick {
            editText.delete()
        }
    }
    val toolBar = LinearLayout(context).apply {
        id = View.generateViewId()
        orientation = LinearLayout.HORIZONTAL
        addView(se, LinearLayout.LayoutParams(0, wrapContent, 1f))
        addView(weixiao, LinearLayout.LayoutParams(0, wrapContent, 1f))
        addView(daku, LinearLayout.LayoutParams(0, wrapContent, 1f))
        addView(delete, LinearLayout.LayoutParams(0, wrapContent, 1f))
    }

    init {
        addView(topBarLayout, LayoutParams(0, wrapContent).apply {
            alignParentHor()
            topToTop = constraintParentId
        })
        addView(toolBar, LayoutParams(0, wrapContent).apply {
            alignParentHor()
            bottomToBottom = constraintParentId
        })

        addView(editText, LayoutParams(0, 0).apply {
            alignParentHor()
            topToBottom = topBarLayout.id
            bottomToTop = toolBar.id
        })

        editText.setText("反反复复[微笑][色]发发发方法")
    }

    fun handleClick(text: String){
        val origin = editText.text
        if(origin == null){
            editText.setText(text)
        }else{
            origin.append(text)
        }
    }
}