package dev.mslalith.focuslauncher.core.data.repository

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dev.mslalith.focuslauncher.core.data.database.AppDatabase
import dev.mslalith.focuslauncher.core.data.dto.toAppRoom
import dev.mslalith.focuslauncher.core.model.app.App
import dev.mslalith.focuslauncher.core.testing.CoroutineTest
import dev.mslalith.focuslauncher.core.testing.TestApps
import dev.mslalith.focuslauncher.core.testing.extensions.awaitItem
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
internal class FavoritesRepoTest : CoroutineTest() {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repo: FavoritesRepo

    @Inject
    lateinit var appDatabase: AppDatabase

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            appDatabase.appsDao().addApps(apps = TestApps.all.map(App::toAppRoom))
        }
    }

    @After
    fun teardown() {
        appDatabase.close()
    }

    @Test
    fun `initially favorites must be empty`() = runCoroutineTest {
        val items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEmpty()
    }

    @Test
    fun `when an app is added to favorite, make sure it stays as favorite`() = runCoroutineTest {
        val app = TestApps.Chrome
        repo.addToFavorites(app = app)

        val items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(listOf(app))
    }

    @Test
    fun `when multiple apps are added to favorites, make sure they stays as favorites`() = runCoroutineTest {
        val apps = listOf(TestApps.Chrome, TestApps.Phone)
        repo.addToFavorites(apps = apps)

        val items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(apps)
    }

    @Test
    fun `when an app is removed from favorites, make sure it isn't present`() = runCoroutineTest {
        val app = TestApps.Chrome
        repo.addToFavorites(app = app)

        var items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(listOf(app))

        repo.removeFromFavorites(packageName = app.packageName)

        items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).doesNotContain(app)
        assertThat(items).isEmpty()
    }

    @Test
    fun `when favorites are cleared, list should be empty`() = runCoroutineTest {
        val apps = TestApps.all
        repo.addToFavorites(apps = apps)

        var items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(apps)

        repo.clearFavorites()

        items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEmpty()
    }

    @Test
    fun `when querying for a favorite app, isFavorite must return true`() = runCoroutineTest {
        val app = TestApps.Youtube
        repo.addToFavorites(app = app)

        val items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(listOf(app))

        val isHidden = repo.isFavorite(packageName = app.packageName)
        assertThat(isHidden).isTrue()
    }

    @Test
    fun `when querying for an un-favorite app, isFavorite must return false`() = runCoroutineTest {
        val app = TestApps.Chrome
        repo.addToFavorites(app = app)

        val items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(listOf(app))

        val isHidden = repo.isFavorite(packageName = TestApps.Phone.packageName)
        assertThat(isHidden).isFalse()
    }

    @Test
    fun `when 2 favorites are present and are reordered, make sure it returns reordered list`() = runCoroutineTest {
        val appsBeforeReorder = listOf(TestApps.Phone, TestApps.Youtube)
        repo.addToFavorites(apps = appsBeforeReorder)

        var items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(appsBeforeReorder)

        repo.reorderFavorite(app = TestApps.Youtube, withApp = TestApps.Phone)
        val appsAfterReorder = listOf(TestApps.Youtube, TestApps.Phone)

        items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(appsAfterReorder)
    }

    @Test
    fun `when more than 2 favorites are present and two of them are reordered, make sure it returns reordered list`() = runCoroutineTest {
        val appsBeforeReorder = listOf(TestApps.Phone, TestApps.Chrome, TestApps.Youtube)
        repo.addToFavorites(apps = appsBeforeReorder)

        var items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(appsBeforeReorder)

        repo.reorderFavorite(app = TestApps.Youtube, withApp = TestApps.Phone)
        val appsAfterReorder = listOf(TestApps.Youtube, TestApps.Chrome, TestApps.Phone)

        items = repo.onlyFavoritesFlow.awaitItem()
        assertThat(items).isEqualTo(appsAfterReorder)
    }
}
