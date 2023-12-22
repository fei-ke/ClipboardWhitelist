@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)

package one.yufz.clipboard.app.home

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import one.yufz.clipboard.app.widget.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import one.yufz.clipboard.R
import one.yufz.clipboard.app.widget.SearchBar
import one.yufz.clipboard.app.widget.pullrefresh.PullRefreshIndicator
import one.yufz.clipboard.app.widget.pullrefresh.pullRefresh

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = mavericksViewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val uiState by homeViewModel.collectAsState()

    Scaffold(
        topBar = {
            AppBar(
                scrollBehavior,
                searching = uiState.searching,
                searchText = uiState.searchText ?: "",
                requestSearching = { homeViewModel.setSearching(it) },
                onSearchTextChanged = { homeViewModel.setSearchText(it) },
                includeSystem = uiState.includeSystem,
                onIncludeSystemChanged = { homeViewModel.setIncludeSystem(it) }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            if (uiState.available) {
                val refreshState = rememberPullRefreshState(uiState.appList is Loading, {
                    homeViewModel.refresh()
                })

                Box(Modifier.pullRefresh(refreshState)) {
                    AppListScreen(homeViewModel, uiState.filteredAppList)
                    PullRefreshIndicator(uiState.appList is Loading, refreshState, Modifier.align(Alignment.TopCenter))
                }
            } else {
                Unavailable()
            }
        }
    }
}

@Composable
fun AppListScreen(homeViewModel: HomeViewModel, appList: List<AppInfo>) {
    val bottomPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()

    LazyColumn(contentPadding = bottomPadding, modifier = Modifier.fillMaxSize()) {
        items(appList) { appInfo ->
            AppCard(appInfo) { checked ->
                if (checked) {
                    homeViewModel.putPackage(appInfo.packageName)
                } else {
                    homeViewModel.removePackage(appInfo.packageName)
                }
            }
        }
    }
}

@Composable
fun AppCard(info: AppInfo, onCheckedChange: (Boolean) -> Unit) {
    val drawable by loadAppIcon(LocalContext.current, info.packageName)
    val drawablePainter = rememberDrawablePainter(drawable)

    ListItem(
        headlineContent = {
            Text(text = info.name.toString(), maxLines = 1)
        },
        supportingContent = {
            Text(text = info.packageName, maxLines = 1)
        },
        leadingContent = {
            Icon(
                painter = drawablePainter,
                contentDescription = "icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(56.dp)
                    .padding(all = 8.dp)
            )
        },
        trailingContent = {
            Switch(checked = info.enabled, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable {
            onCheckedChange(!info.enabled)
        }
    )
}

@Composable
private fun loadAppIcon(context: Context, packageName: String): MutableState<Drawable?> {
    val drawable = remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(packageName) {
        launch(Dispatchers.IO) {
            drawable.value = context.packageManager.getApplicationIcon(packageName)
        }
    }

    return drawable
}

@Composable
fun Unavailable() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = stringResource(R.string.unavailable), modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun AppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    searching: Boolean,
    searchText: String,
    requestSearching: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    includeSystem: Boolean,
    onIncludeSystemChanged: (Boolean) -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        scrollBehavior = scrollBehavior,
        actions = {
            //Search
            if (!searching) {
                IconButton(onClick = { requestSearching(true) }) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                }
            } else {
                SearchBar(
                    searchText = searchText,
                    placeholderText = stringResource(id = R.string.menu_search),
                    onNavigateBack = { requestSearching(false) },
                    onSearchTextChanged = onSearchTextChanged
                )
            }
            //More
            AppBarMoreMenu(includeSystem, onIncludeSystemChanged)
        }
    )
}

@Composable
private fun AppBarMoreMenu(includeSystem: Boolean, onIncludeSystemChanged: (Boolean) -> Unit) {
    //More
    var openMoreMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { openMoreMenu = true }) {
        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
        DropdownMenu(
            expanded = openMoreMenu,
            onDismissRequest = { openMoreMenu = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(id = R.string.include_system))
                },
                trailingIcon = {
                    Checkbox(checked = includeSystem, onCheckedChange = {
                        onIncludeSystemChanged(it)
                        openMoreMenu = false
                    })
                },
                onClick = {
                    onIncludeSystemChanged(!includeSystem)
                    openMoreMenu = false
                }
            )
        }
    }
}