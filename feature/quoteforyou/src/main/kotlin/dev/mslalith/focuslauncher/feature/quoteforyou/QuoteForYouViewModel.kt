package dev.mslalith.focuslauncher.feature.quoteforyou

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mslalith.focuslauncher.core.common.model.State
import dev.mslalith.focuslauncher.core.common.appcoroutinedispatcher.AppCoroutineDispatcher
import dev.mslalith.focuslauncher.core.common.network.NetworkMonitor
import dev.mslalith.focuslauncher.core.data.repository.QuotesRepo
import dev.mslalith.focuslauncher.core.data.repository.settings.QuotesSettingsRepo
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.Quotes.DEFAULT_SHOW_QUOTES
import dev.mslalith.focuslauncher.core.ui.extensions.launchInIO
import dev.mslalith.focuslauncher.core.ui.extensions.withinScope
import dev.mslalith.focuslauncher.feature.quoteforyou.model.QuoteForYouState
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

@HiltViewModel
internal class QuoteForYouViewModel @Inject constructor(
    private val quotesRepo: QuotesRepo,
    private val quotesSettingsRepo: QuotesSettingsRepo,
    private val networkMonitor: NetworkMonitor,
    private val appCoroutineDispatcher: AppCoroutineDispatcher
) : ViewModel() {

    init {
        appCoroutineDispatcher.launchInIO {
            quotesRepo.addInitialQuotesIfNeeded()
            quotesRepo.nextRandomQuote()
        }
    }

    private val defaultQuoteForYouState = QuoteForYouState(
        showQuotes = DEFAULT_SHOW_QUOTES,
        currentQuote = State.Initial
    )

    val quoteForYouState = flowOf(value = defaultQuoteForYouState)
        .combine(flow = quotesSettingsRepo.showQuotesFlow) { state, showQuotes ->
            state.copy(showQuotes = showQuotes)
        }.combine(flow = quotesRepo.currentQuoteStateFlow) { state, currentQuote ->
            state.copy(currentQuote = currentQuote)
        }.withinScope(initialValue = defaultQuoteForYouState)

    val isFetchingQuotes = quotesRepo.isFetchingQuotesStateFlow.withinScope(initialValue = false)

    fun fetchQuotesIfRequired() {
        if (!networkMonitor.isCurrentlyConnected()) return

        appCoroutineDispatcher.launchInIO {
            if (quotesRepo.hasQuotesReachedLimit()) return@launchInIO
            if (quotesRepo.isFetchingQuotesStateFlow.value) return@launchInIO

            quotesRepo.fetchQuotes(maxPages = 2)
        }
    }

    fun nextRandomQuote() {
        appCoroutineDispatcher.launchInIO {
            quotesRepo.nextRandomQuote()
        }
    }

    fun toggleShowQuotes() {
        appCoroutineDispatcher.launchInIO {
            quotesSettingsRepo.toggleShowQuotes()
        }
    }
}
