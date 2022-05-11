package com.qmuiteam.qmuidemo.base

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.qmuiteam.compose.core.provider.QMUIWindowInsetsProvider
import com.qmuiteam.qmui.kotlin.matchParent

abstract class ComposeBaseFragment(): BaseFragment() {
    override fun onCreateView(): View {
        return object: FrameLayout(requireContext()){

            private val composeView = ComposeView(requireContext()).apply {
                setContent {
                    QMUIWindowInsetsProvider {
                        PageContent()
                    }
                }
            }.apply {
                ViewTreeLifecycleOwner.set(this, this@ComposeBaseFragment)
                ViewTreeViewModelStoreOwner.set(this, this@ComposeBaseFragment)
                setViewTreeSavedStateRegistryOwner(this@ComposeBaseFragment)
            }

            init {
                addView(composeView, LayoutParams(matchParent, matchParent))
            }

            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val wm = MeasureSpec.getMode(widthMeasureSpec)
                val ws = MeasureSpec.getSize(widthMeasureSpec).coerceAtMost(0x2FFFF)
                val hm = MeasureSpec.getMode(heightMeasureSpec)
                val hs = MeasureSpec.getSize(heightMeasureSpec).coerceAtMost(0x2FFFF)
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(ws, wm),
                    MeasureSpec.makeMeasureSpec(hs, hm)
                )
            }
        }
    }

    @Composable
    protected abstract fun PageContent()
}