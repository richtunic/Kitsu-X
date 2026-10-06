package eu.kanade.tachiyomi.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/** Supplies the image CDN's required source referer for older ManhwaWeb extensions. */
class ManhwaWebImageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val imageRequest = request.url.isHttps &&
            request.url.host == "img2mw.xyz" &&
            request.url.encodedPath.startsWith("/manhwas/")
        val compatibleRequest = if (imageRequest && request.header("Referer").isNullOrBlank()) {
            request.newBuilder().header("Referer", "https://manhwaweb.com/").build()
        } else {
            request
        }
        return chain.proceed(compatibleRequest)
    }
}
