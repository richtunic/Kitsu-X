package eu.kanade.tachiyomi.network.interceptor

import okhttp3.HttpUrl

object AnimeOnlineCloudflareCompat {
    private const val BRAVE_USER_AGENT =
        "Brave 1.62.152, Chromium 121.0.6167.101"
    private const val CHROME_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.6167.101 Mobile Safari/537.36"

    fun userAgentFor(url: HttpUrl): String? {
        return if (url.host.contains("animeonline.ninja") || url.host.contains("animeninja.online")) {
            BRAVE_USER_AGENT
        } else {
            null
        }
    }

    fun thirdPartyUserAgentFor(userAgent: String?): String? {
        return if (userAgent == BRAVE_USER_AGENT) CHROME_USER_AGENT else null
    }
}
