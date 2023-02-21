package dev.mslalith.focuslauncher.core.ui

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun BackPressHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val backPressedCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }

    LocalOnBackPressedDispatcherOwner.current?.let {
        val backPressedDispatcher = it.onBackPressedDispatcher
        DisposableEffect(enabled) {
            when (enabled) {
                true -> backPressedDispatcher.addCallback(backPressedCallback)
                false -> backPressedCallback.remove()
            }
            onDispose { backPressedCallback.remove() }
        }
    }
}