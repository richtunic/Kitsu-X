package eu.kanade.tachiyomi.network.interceptor

import okhttp3.HttpUrl

object AnimeOnlineCloudflareCompat {
    private const val BRAVE_USER_AGENT =
        "Brave 1.62.152, Chromium 121.0.6167.101"

    fun userAgentFor(url: HttpUrl): String? {
        return if (url.host.contains("animeonline.ninja") || url.host.contains("animeninja.online")) {
            BRAVE_USER_AGENT
        } else {
            null
        }
    }
}
