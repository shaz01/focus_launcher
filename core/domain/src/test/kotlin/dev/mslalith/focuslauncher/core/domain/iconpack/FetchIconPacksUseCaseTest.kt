package dev.mslalith.focuslauncher.core.domain.iconpack

import com.google.common.truth.Truth.assertThat
import dev.mslalith.focuslauncher.core.domain.utils.toIconPacks
import dev.mslalith.focuslauncher.core.testing.CoroutineTest
import dev.mslalith.focuslauncher.core.testing.TestApps
import dev.mslalith.focuslauncher.core.testing.extensions.awaitItem
import dev.mslalith.focuslauncher.core.testing.launcherapps.TestIconPackManager
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
class FetchIconPacksUseCaseTest : CoroutineTest() {

    private lateinit var useCase: FetchIconPacksUseCase

    private val testIconPackManager = TestIconPackManager()

    @Before
    fun setup() {
        useCase = FetchIconPacksUseCase(iconPackManager = testIconPackManager)
    }

    @Test
    fun `01 - fetch icon packs must return proper list`() = runCoroutineTest {
        val iconPacks = TestApps.all.toIconPacks()

        testIconPackManager.setIconPackApps(iconPacks = iconPacks)
        useCase()

        assertThat(testIconPackManager.iconPacksFlow.awaitItem()).isEqualTo(iconPacks)
    }
}
