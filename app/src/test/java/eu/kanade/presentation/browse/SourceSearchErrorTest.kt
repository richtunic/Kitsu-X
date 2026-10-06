package eu.kanade.presentation.browse

import eu.kanade.tachiyomi.network.HttpException
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.i18n.MR
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

class SourceSearchErrorTest {
    @Test
    fun `wrapped extension compatibility failures are distinguished from network errors`() {
        sourceSearchErrorMessage(IllegalStateException("wrapped", NoClassDefFoundError("missing"))) shouldBe
            MR.strings.kitsux_source_compatibility
    }

    @Test
    fun `timeout and TLS failures retain their specific recovery message`() {
        sourceSearchErrorMessage(SocketTimeoutException()) shouldBe MR.strings.kitsux_source_timeout
        sourceSearchErrorMessage(SSLException("certificate")) shouldBe MR.strings.kitsux_source_secure
        sourceSearchErrorMessage(IOException("network")) shouldBe MR.strings.kitsux_source_connection
    }

    @Test
    fun `rate limits and verification failures differ from a source failure`() {
        sourceSearchErrorMessage(HttpException(429)) shouldBe MR.strings.kitsux_source_rate_limit
        sourceSearchErrorMessage(HttpException(403)) shouldBe MR.strings.kitsux_source_blocked
        sourceSearchErrorMessage(HttpException(500)) shouldBe MR.strings.kitsux_source_failed
        sourceSearchErrorMessage(IllegalStateException("parser")) shouldBe MR.strings.kitsux_source_failed
    }
}
