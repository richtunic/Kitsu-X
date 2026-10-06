package eu.kanade.presentation.browse

import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.network.HttpException
import tachiyomi.i18n.MR
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

internal fun sourceSearchErrorMessage(error: Throwable): StringResource {
    val causes = generateSequence(error) { it.cause }.take(8).toList()
    return when {
        causes.any { it is LinkageError } -> MR.strings.kitsux_source_compatibility
        causes.any { it is SocketTimeoutException } -> MR.strings.kitsux_source_timeout
        causes.any { it is SSLException } -> MR.strings.kitsux_source_secure
        causes.any { it is HttpException && it.code == 429 } -> MR.strings.kitsux_source_rate_limit
        causes.any { it is HttpException && it.code in listOf(401, 403) } -> MR.strings.kitsux_source_blocked
        causes.any { it is IOException } -> MR.strings.kitsux_source_connection
        else -> MR.strings.kitsux_source_failed
    }
}
