package dev.mslalith.focuslauncher.feature.favorites

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mslalith.focuslauncher.core.common.appcoroutinedispatcher.AppCoroutineDispatcher
import dev.mslalith.focuslauncher.core.data.repository.FavoritesRepo
import dev.mslalith.focuslauncher.core.data.repository.settings.GeneralSettingsRepo
import dev.mslalith.focuslauncher.core.domain.apps.GetFavoriteColoredAppsUseCase
import dev.mslalith.focuslauncher.core.domain.launcherapps.GetDefaultFavoriteAppsUseCase
import dev.mslalith.focuslauncher.core.domain.theme.GetThemeUseCase
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.General.DEFAULT_THEME
import dev.mslalith.focuslauncher.core.model.app.App
import dev.mslalith.focuslauncher.core.model.app.AppWithColor
import dev.mslalith.focuslauncher.core.ui.extensions.launchInIO
import dev.mslalith.focuslauncher.core.ui.extensions.withinScope
import dev.mslalith.focuslauncher.feature.favorites.model.FavoritesContextMode
import dev.mslalith.focuslauncher.feature.favorites.model.FavoritesState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class FavoritesViewModel @Inject constructor(
    private val getDefaultFavoriteAppsUseCase: GetDefaultFavoriteAppsUseCase,
    getFavoriteColoredAppsUseCase: GetFavoriteColoredAppsUseCase,
    getThemeUseCase: GetThemeUseCase,
    private val generalSettingsRepo: GeneralSettingsRepo,
    private val favoritesRepo: FavoritesRepo,
    private val appCoroutineDispatcher: AppCoroutineDispatcher
) : ViewModel() {

    private val _favoritesContextualMode: MutableStateFlow<FavoritesContextMode> = MutableStateFlow(value = FavoritesContextMode.Closed)

    private val defaultFavoritesState = FavoritesState(
        favoritesContextualMode = _favoritesContextualMode.value,
        favoritesList = persistentListOf(),
        currentTheme = DEFAULT_THEME
    )

    private val allAppsWithIcons: Flow<List<AppWithColor>> = getFavoriteColoredAppsUseCase()
        .flowOn(context = appCoroutineDispatcher.io)

    val favoritesState = flowOf(value = defaultFavoritesState)
        .combine(flow = allAppsWithIcons) { state, favorites ->
            state.copy(favoritesList = favorites.toImmutableList())
        }.combine(flow = _favoritesContextualMode) { state, favoritesContextualMode ->
            state.copy(favoritesContextualMode = favoritesContextualMode)
        }.combine(flow = getThemeUseCase()) { state, theme ->
            state.copy(currentTheme = theme)
        }.withinScope(initialValue = defaultFavoritesState)

    fun addDefaultAppsIfRequired() {
        appCoroutineDispatcher.launchInIO {
            if (generalSettingsRepo.firstRunFlow.first()) {
                generalSettingsRepo.overrideFirstRun()
                favoritesRepo.addToFavorites(apps = getDefaultFavoriteAppsUseCase())
            }
        }
    }

    fun reorderFavorite(app: App, withApp: App, onReordered: () -> Unit) {
        if (app.packageName == withApp.packageName) {
            onReordered()
            return
        }
        appCoroutineDispatcher.launchInIO {
            favoritesRepo.reorderFavorite(app = app, withApp = withApp)
            withContext(appCoroutineDispatcher.main) {
                onReordered()
            }
        }
    }

    fun removeFromFavorites(app: App) {
        appCoroutineDispatcher.launchInIO {
            favoritesRepo.removeFromFavorites(packageName = app.packageName)
        }
    }

    fun isInContextualMode(): Boolean = _favoritesContextualMode.value != FavoritesContextMode.Closed

    fun changeFavoritesContextMode(mode: FavoritesContextMode) {
        _favoritesContextualMode.value = mode
    }

    fun isReordering(): Boolean = when (_favoritesContextualMode.value) {
        FavoritesContextMode.Reorder, is FavoritesContextMode.ReorderPickPosition -> true
        else -> false
    }

    fun isAppAboutToReorder(app: App): Boolean = if (_favoritesContextualMode.value is FavoritesContextMode.ReorderPickPosition) {
        (_favoritesContextualMode.value as FavoritesContextMode.ReorderPickPosition).app.packageName != app.packageName
    } else {
        false
    }

    fun hideContextualMode() {
        _favoritesContextualMode.value = FavoritesContextMode.Closed
    }
}
