package eu.kanade.presentation.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class ItemNumberFormatterTest {
    @Test
    fun `float chapter numbers do not expose binary precision`() {
        formatChapterNumber(0.01f.toDouble()) shouldBe "0.01"
        formatChapterNumber(0.0099999776482582) shouldBe "0.01"
        formatChapterNumber(0.1f.toDouble()) shouldBe "0.1"
    }

    @Test
    fun `fractional chapters and whole numbers retain existing formatting`() {
        listOf(0.0, 0.001, 0.01, 1.5, 48.25, 82.0).map(::formatChapterNumber) shouldBe
            listOf("0", "0.001", "0.01", "1.5", "48.25", "82")
    }

    @Test
    fun `home and UI can format chapter and episode numbers concurrently`() {
        val executor = Executors.newFixedThreadPool(4)
        try {
            val results = executor.invokeAll(
                List(200) { index ->
                    Callable {
                        if (index % 2 == 0) formatChapterNumber(0.01f.toDouble()) else formatEpisodeNumber(48.25)
                    }
                },
            ).map { it.get() }
            results shouldBe List(200) { if (it % 2 == 0) "0.01" else "48.25" }
        } finally {
            executor.shutdownNow()
        }
    }
}
