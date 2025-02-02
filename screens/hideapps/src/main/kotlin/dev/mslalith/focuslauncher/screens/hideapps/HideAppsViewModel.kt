package dev.mslalith.focuslauncher.screens.hideapps

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mslalith.focuslauncher.core.common.appcoroutinedispatcher.AppCoroutineDispatcher
import dev.mslalith.focuslauncher.core.data.repository.AppDrawerRepo
import dev.mslalith.focuslauncher.core.data.repository.FavoritesRepo
import dev.mslalith.focuslauncher.core.data.repository.HiddenAppsRepo
import dev.mslalith.focuslauncher.core.model.app.App
import dev.mslalith.focuslauncher.core.model.app.SelectedHiddenApp
import dev.mslalith.focuslauncher.core.ui.extensions.launchInIO
import dev.mslalith.focuslauncher.core.ui.extensions.withinScope
import dev.mslalith.focuslauncher.screens.hideapps.model.HideAppsState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@HiltViewModel
internal class HideAppsViewModel @Inject constructor(
    appDrawerRepo: AppDrawerRepo,
    private val favoritesRepo: FavoritesRepo,
    private val hiddenAppsRepo: HiddenAppsRepo,
    private val appCoroutineDispatcher: AppCoroutineDispatcher
) : ViewModel() {

    private val defaultHideAppsState = HideAppsState(
        hiddenApps = persistentListOf()
    )

    private val hiddenAppsFlow: Flow<ImmutableList<SelectedHiddenApp>> = appDrawerRepo.allAppsFlow
        .map { it.map { app -> SelectedHiddenApp(app = app, isSelected = false, isFavorite = false) } }
        .combine(flow = favoritesRepo.onlyFavoritesFlow) { appsList, onlyFavoritesList ->
            appsList.map { hiddenApp ->
                val isFavorite = onlyFavoritesList.any { it.packageName == hiddenApp.app.packageName }
                hiddenApp.copy(isFavorite = isFavorite)
            }
        }.combine(flow = hiddenAppsRepo.onlyHiddenAppsFlow) { appsList, onlyHiddenAppsList ->
            appsList.map { hiddenApp ->
                val isSelected = onlyHiddenAppsList.any { it.packageName == hiddenApp.app.packageName }
                hiddenApp.copy(isSelected = isSelected)
            }.toImmutableList()
        }

    val hideAppsState = flowOf(value = defaultHideAppsState)
        .combine(flow = hiddenAppsFlow) { state, hiddenApps ->
            state.copy(hiddenApps = hiddenApps)
        }.withinScope(initialValue = defaultHideAppsState)

    fun removeFromFavorites(app: App) {
        appCoroutineDispatcher.launchInIO { favoritesRepo.removeFromFavorites(packageName = app.packageName) }
    }

    fun addToHiddenApps(app: App) {
        appCoroutineDispatcher.launchInIO { hiddenAppsRepo.addToHiddenApps(app = app) }
    }

    fun removeFromHiddenApps(app: App) {
        appCoroutineDispatcher.launchInIO { hiddenAppsRepo.removeFromHiddenApps(packageName = app.packageName) }
    }

    fun clearHiddenApps() {
        appCoroutineDispatcher.launchInIO { hiddenAppsRepo.clearHiddenApps() }
    }
}
