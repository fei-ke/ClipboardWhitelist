package one.yufz.clipboard.app.home

import android.content.pm.ApplicationInfo
import android.util.Log
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.yufz.clipboard.Prefs
import one.yufz.clipboard.app.App
import one.yufz.clipboard.app.util.registerPackageChangeFlow

data class AppInfo(
    val packageName: String,
    val name: CharSequence,
    var enabled: Boolean,
    val isSystem: Boolean,
)

data class HomeState(
    val available: Boolean = false,
    val appList: Async<List<AppInfo>> = Uninitialized,
    val includeSystem: Boolean = false,
    val searching: Boolean = false,
    val searchText: String? = null,
    private val orderByEnabled: Boolean = true
) : MavericksState {

    val filteredAppList: List<AppInfo> = filterAppList()

    private fun filterAppList(): List<AppInfo> {
        var list = appList() ?: emptyList()
        if (!includeSystem) {
            list = list.filter { !it.isSystem }
        }
        if (!searchText.isNullOrBlank()) {
            list = list.filter {
                it.name.contains(searchText, true) || it.packageName.contains(searchText, true)
            }
        }
        return list
    }
}

class HomeViewModel(initialState: HomeState) : MavericksViewModel<HomeState>(initialState) {
    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val getInstalledAppListSignalFlow = MutableSharedFlow<Unit>()

    private val prefs = Prefs(App.instance)

    init {
        setState {
            copy(available = prefs.available)
        }

        getInstalledAppListSignalFlow.map {
            Log.d(TAG, "installedAppListFlow called")
            setState { copy(appList = Loading(appList())) }
            buildAppList(loadInstalledAppList(), prefs.getAll()).sortedByDescending { it.enabled }
        }.setOnEach(dispatcher = Dispatchers.IO) {
            Log.d(TAG, "setOnEach() called")
            copy(appList = Success(it))
        }

        prefs.flow().map {
            buildAppList(awaitState().appList() ?: emptyList(), it)
        }.setOnEach(dispatcher = Dispatchers.IO) {
            copy(appList = Success(it))
        }

        App.instance.registerPackageChangeFlow()
            .onEach { getInstalledAppListSignalFlow.emit(Unit) }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        Log.d(TAG, "refresh() called")
        viewModelScope.launch {
            setState { copy(appList = Loading(appList())) }
            getInstalledAppListSignalFlow.emit(Unit)
        }
    }

    private suspend fun loadInstalledAppList(): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            val pm = App.instance.packageManager
            pm.getInstalledApplications(0).map {
                AppInfo(
                    packageName = it.packageName,
                    name = it.loadLabel(pm),
                    enabled = false,//not resolved
                    isSystem = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                )
            }
        }
    }

    private fun buildAppList(pkgList: List<AppInfo>, enabledAppList: Set<String>): List<AppInfo> {
        return pkgList.map {
            it.copy(enabled = enabledAppList.contains(it.packageName))
        }
    }

    fun putPackage(pkgName: String) {
        prefs.putPackage(pkgName)
    }

    fun removePackage(pkgName: String) {
        prefs.removePackage(pkgName)
    }


    fun setSearchText(text: String) {
        setState {
            copy(searchText = text)
        }
    }

    fun setSearching(searching: Boolean) {
        setState {
            copy(searching = searching)
        }
    }

    fun setIncludeSystem(includeSystem: Boolean) {
        setState {
            copy(includeSystem = includeSystem)
        }
    }
}