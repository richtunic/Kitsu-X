package eu.kanade.tachiyomi.network.interceptor

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Test

class ManhwaWebImageInterceptorTest {
    private fun intercepted(request: Request): Request {
        var sent: Request? = null
        val chain = mockk<Interceptor.Chain> {
            every { request() } returns request
            every { proceed(any()) } answers {
                sent = firstArg()
                mockk<Response>()
            }
        }
        ManhwaWebImageInterceptor().intercept(chain)
        return checkNotNull(sent)
    }

    @Test
    fun `image CDN receives required referer when extension omits it`() {
        val request = Request.Builder().url("https://img2mw.xyz/manhwas/lucky-mia/chapter_33/001.webp").build()
        intercepted(request).header("Referer") shouldBe "https://manhwaweb.com/"
    }

    @Test
    fun `existing extension referer and authorization are preserved`() {
        val request = Request.Builder().url("https://img2mw.xyz/manhwas/test/image.webp")
            .header("Referer", "https://manhwaweb.com/leer/test")
            .header("Authorization", "test-value").build()
        intercepted(request) shouldBe request
    }

    @Test
    fun `other hosts protocols and CDN paths are unaffected`() {
        listOf(
            "https://other.example/manhwas/image.webp",
            "http://img2mw.xyz/manhwas/image.webp",
            "https://img2mw.xyz/api/search",
            "https://img2mw.xyz.other.example/manhwas/image.webp",
        ).forEach {
            val request = Request.Builder().url(it).build()
            intercepted(request) shouldBe request
        }
    }
}
