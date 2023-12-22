@file:OptIn(ExperimentalMaterial3Api::class)

package one.yufz.clipboard.app.widget

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp

@Composable
fun rememberPullToRefreshState(
    positionalThreshold: Dp = PullToRefreshDefaults.PositionalThreshold,
    enabled: () -> Boolean = { true },
    onRefresh: () -> Unit = {}
): BetterPullToRefreshState {
    val origin = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState(positionalThreshold, enabled)

    val state = remember { BetterPullToRefreshState(origin) }
    if (state.isRefreshing && !state.refreshState) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }
    return state
}

class BetterPullToRefreshState(private val origin: PullToRefreshState) : PullToRefreshState by origin {
    internal var refreshState = false
        private set

    override var isRefreshing: Boolean
        get() = origin.isRefreshing
        set(value) {
            refreshState = value

            if (value) {
                startRefresh()
            } else {
                endRefresh()
            }
        }
}