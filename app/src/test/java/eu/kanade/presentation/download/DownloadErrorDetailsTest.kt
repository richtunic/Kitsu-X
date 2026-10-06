package eu.kanade.presentation.download

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DownloadErrorDetailsTest {
    @Test
    fun `copied errors omit URL credentials query tokens and authentication headers`() {
        val error = "HTTP 403 https://user:password@cdn.example/image?token=secret\n" +
            "Authorization: Bearer private-value\nCookie: session=private-cookie\ntoken=private-token"
        safeDownloadError(error) shouldBe
            "HTTP 403 [URL]\nAuthorization: [redacted]\nCookie: [redacted]\ntoken=[redacted]"
    }

    @Test
    fun `ordinary diagnostics remain readable`() {
        safeDownloadError("HTTP error 403\nNot enough storage") shouldBe "HTTP error 403\nNot enough storage"
    }
}
