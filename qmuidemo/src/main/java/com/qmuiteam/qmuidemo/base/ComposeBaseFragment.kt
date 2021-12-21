package com.qmuiteam.qmuidemo.base

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.qmuiteam.compose.provider.QMUIWindowInsetsProvider

abstract class ComposeBaseFragment(): BaseFragment() {
    override fun onCreateView(): View {
        return ComposeView(requireContext()).apply {
            setContent {
                QMUIWindowInsetsProvider {
                    PageContent()
                }
            }
        }.apply {
            ViewTreeLifecycleOwner.set(this, this@ComposeBaseFragment)
            ViewTreeViewModelStoreOwner.set(this, this@ComposeBaseFragment)
            ViewTreeSavedStateRegistryOwner.set(this, this@ComposeBaseFragment)
        }
    }

    @Composable
    protected abstract fun PageContent()
}