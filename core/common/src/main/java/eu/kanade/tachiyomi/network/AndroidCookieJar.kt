package eu.kanade.tachiyomi.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class AndroidCookieJar : CookieJar {

    private val manager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()

        cookies.forEach { manager.setCookie(urlString, it.toString()) }
        manager.flush()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return get(url)
    }

    fun get(url: HttpUrl): List<Cookie> {
        var cookies = manager.getCookie(url.toString())

        // Mirror Cloudflare cookies across animeonline.ninja and animeninja.online domains/subdomains if missing
        val isTargetDomain = url.host.contains("animeonline.ninja") || url.host.contains("animeninja.online")
        if (isTargetDomain) {
            val cfCookies = listOf("cf_clearance", "__cf_bm")
            val altUrls = listOf(
                "https://ver.animeonline.ninja",
                "https://ww3.animeonline.ninja",
                "https://animeonline.ninja",
                "https://animeninja.online",
                "https://www.animeninja.online"
            )
            var updated = false
            for (cookieName in cfCookies) {
                if (cookies.isNullOrEmpty() || !cookies.contains(cookieName)) {
                    for (altUrl in altUrls) {
                        try {
                            val altHttpUrl = altUrl.toHttpUrl()
                            if (altHttpUrl.host == url.host) continue
                            val altCookies = manager.getCookie(altUrl)
                            if (altCookies != null && altCookies.contains(cookieName)) {
                                val cookieValue = altCookies.split(";").firstOrNull { it.trim().startsWith("$cookieName=") }
                                if (cookieValue != null) {
                                    manager.setCookie(url.toString(), cookieValue.trim())
                                    updated = true
                                    break
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
            if (updated) {
                manager.flush()
                cookies = manager.getCookie(url.toString())
            }
        }

        return if (cookies != null && cookies.isNotEmpty()) {
            cookies.split(";").mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }

    fun remove(url: HttpUrl, cookieNames: List<String>? = null, maxAge: Int = -1): Int {
        val urlString = url.toString()
        val cookies = manager.getCookie(urlString) ?: return 0

        fun List<String>.filterNames(): List<String> {
            return if (cookieNames != null) {
                this.filter { it in cookieNames }
            } else {
                this
            }
        }

        return cookies.split(";")
            .map { it.substringBefore("=") }
            .filterNames()
            .onEach {
                manager.setCookie(urlString, "$it=;Max-Age=$maxAge;Path=/")
                manager.setCookie(urlString, "$it=;Max-Age=$maxAge;Domain=${url.host};Path=/")

                val rootDomain = url.host.split(".").takeLast(2).joinToString(".")
                if (rootDomain != url.host) {
                    manager.setCookie(urlString, "$it=;Max-Age=$maxAge;Domain=.$rootDomain;Path=/")
                }
            }
            .also { manager.flush() }
            .count()
    }

    fun removeAll() {
        manager.removeAllCookies { manager.flush() }
    }
}
