package dev.mslalith.focuslauncher.core.testing.extensions

import app.cash.turbine.testIn
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

context (CoroutineScope)
suspend fun <T> Flow<T>.assertFor(
    expected: T
) {
    assertFor(
        expected = expected,
        valueFor = { it }
    )
}

context (CoroutineScope)
suspend fun <T, R> Flow<T>.assertFor(
    expected: R,
    valueFor: (T) -> R
) {
    assertFor(
        expected = expected,
        valueFor = valueFor,
        compare = { a, b -> a == b },
        assertion = { assertThat(it).isEqualTo(expected) }
    )
}

context (CoroutineScope)
suspend fun <T, R> Flow<T>.assertFor(
    expected: R,
    valueFor: (T) -> R,
    compare: (R, R) -> Boolean,
    assertion: (R) -> Unit
) {
    val turbine = testIn(scope = this@CoroutineScope)
    var changedItem = valueFor(turbine.expectMostRecentItem())
    try {
        if (compare(changedItem, expected)) {
            turbine.cancelAndIgnoreRemainingEvents()
            assertThat(changedItem).isEqualTo(expected)
            return
        }

        while (!compare(changedItem, expected)) {
            changedItem = valueFor(turbine.awaitItem())
        }

        turbine.cancelAndIgnoreRemainingEvents()
        assertion(changedItem)
    } catch (ex: AssertionError) {
        println(
            """
                Test Failed:
                expected: $expected
                actual: $changedItem
            """.trimIndent()
        )
        throw ex
    }
}
