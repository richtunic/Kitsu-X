package eu.kanade.tachiyomi.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(
    private val defaultUserAgentProvider: () -> String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val userAgent = originalRequest.header("User-Agent")
        val overrideUserAgent = AnimeOnlineCloudflareCompat.userAgentFor(originalRequest.url)
        val thirdPartyUserAgent = AnimeOnlineCloudflareCompat.thirdPartyUserAgentFor(userAgent)

        return if (
            overrideUserAgent != null ||
            thirdPartyUserAgent != null ||
            userAgent.isNullOrEmpty() ||
            userAgent == DEFAULT_EXTENSION_USER_AGENT
        ) {
            val newRequest = originalRequest
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", overrideUserAgent ?: thirdPartyUserAgent ?: defaultUserAgentProvider())
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }

    private companion object {
        const val DEFAULT_EXTENSION_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:136.0) Gecko/20100101 Firefox/136.0"
    }
}
