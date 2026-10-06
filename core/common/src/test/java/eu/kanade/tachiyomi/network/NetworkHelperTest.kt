package eu.kanade.tachiyomi.network

import android.content.Context
import android.webkit.CookieManager
import androidx.core.content.ContextCompat
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import okhttp3.Request
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.ServerSocket
import java.util.concurrent.Executor
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

class NetworkHelperTest {

    @TempDir
    lateinit var cacheDir: File

    private lateinit var network: NetworkHelper

    @BeforeEach
    fun setUp() {
        mockkStatic(CookieManager::class, ContextCompat::class)
        val cookies = mockk<CookieManager>(relaxed = true)
        every { CookieManager.getInstance() } returns cookies
        every { cookies.getCookie(any()) } returns null
        val context = mockk<Context>()
        every { context.cacheDir } returns cacheDir
        every { ContextCompat.getMainExecutor(context) } returns Executor { it.run() }
        val preferences = mockk<NetworkPreferences>()
        every { preferences.dohProvider().get() } returns -1
        every { preferences.defaultUserAgent().get() } returns "KitsuX test"
        network = NetworkHelper(context, preferences)
    }

    @AfterEach
    fun tearDown() {
        network.client.cache?.close()
        unmockkAll()
    }

    @Test
    fun `default client satisfies Keiyoushi compression and interceptor requirements`() {
        val names = network.client.interceptors.map { it.javaClass.simpleName }
        names.containsAll(
            listOf(
                "UncaughtExceptionInterceptor",
                "UserAgentInterceptor",
                "ManhwaWebImageInterceptor",
                "CloudflareInterceptor",
            ),
        ) shouldBe true
        for (client in listOf(network.client, network.nonCloudflareClient, network.cloudflareClient)) {
            client.networkInterceptors.none {
                it.javaClass.simpleName in listOf("IgnoreGzipInterceptor", "BrotliInterceptor")
            } shouldBe true
            (client.cookieJar === network.cookieJar) shouldBe true
        }
    }

    @Test
    fun `default client still decodes gzip for sources without custom compression`() {
        val expected = "legacy anime and manga response"
        val compressed = ByteArrayOutputStream().apply {
            GZIPOutputStream(this).use { it.write(expected.toByteArray()) }
        }.toByteArray()
        ServerSocket(0).use { server ->
            server.soTimeout = 5000
            val response = FutureTask {
                server.accept().use { socket ->
                    socket.soTimeout = 5000
                    val reader = socket.getInputStream().bufferedReader()
                    val headers = generateSequence { reader.readLine() }.takeWhile { it.isNotEmpty() }.toList()
                    headers.any { it.equals("Accept-Encoding: gzip", ignoreCase = true) } shouldBe true
                    socket.getOutputStream().apply {
                        val responseHeaders = "HTTP/1.1 200 OK\r\n" +
                            "Content-Encoding: gzip\r\n" +
                            "Content-Length: ${compressed.size}\r\n" +
                            "Connection: close\r\n\r\n"
                        write(responseHeaders.toByteArray())
                        write(compressed)
                        flush()
                    }
                }
            }
            Thread(response).apply { isDaemon = true }.start()
            val request = Request.Builder().url("http://127.0.0.1:${server.localPort}/").build()
            network.client.newCall(request).execute().use {
                it.body.string() shouldBe expected
                it.header("Content-Encoding") shouldBe null
            }
            response.get(5, TimeUnit.SECONDS)
        }
    }
}
