package eu.kanade.tachiyomi.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response

/** Retries a rejected image with its source origin when the extension supplied no referer. */
suspend fun fetchImageWithRefererFallback(
    request: Request,
    sourceUrl: String,
    fetch: suspend (Request) -> Response,
): Response {
    return try {
        fetch(request)
    } catch (error: HttpException) {
        if (error.code != 403 || request.method != "GET" || !request.header("Referer").isNullOrBlank()) {
            throw error
        }
        val source = sourceUrl.toHttpUrlOrNull() ?: throw error
        if (source.username.isNotEmpty() || source.password.isNotEmpty()) throw error
        val origin = source.newBuilder().encodedPath("/").query(null).fragment(null).build()
        fetch(request.newBuilder().header("Referer", origin.toString()).build())
    }
}
