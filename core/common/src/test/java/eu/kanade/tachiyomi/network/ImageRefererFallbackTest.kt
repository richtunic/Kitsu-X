package eu.kanade.tachiyomi.network

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

class ImageRefererFallbackTest {
    private val request = Request.Builder().url("https://cdn.example/page.webp")
        .header("Authorization", "test-value").build()

    @Test
    fun `rejected image retries once with source origin and preserves original request`() = runBlocking<Unit> {
        val sent = mutableListOf<Request>()
        val response = mockk<Response>()
        val result = fetchImageWithRefererFallback(request, "https://source.example/catalog?token=private#fragment") {
            sent.add(it)
            if (sent.size == 1) throw HttpException(403)
            response
        }
        assertSame(response, result)
        sent.size shouldBe 2
        sent[1].header("Referer") shouldBe "https://source.example/"
        sent[1].header("Authorization") shouldBe "test-value"
        sent[1].url shouldBe request.url
        request.header("Referer") shouldBe null
    }

    @Test
    fun `successful requests never gain a referer or retry`() = runBlocking<Unit> {
        var calls = 0
        val response = mockk<Response>()
        assertSame(
            response,
            fetchImageWithRefererFallback(request, "https://source.example") {
                calls++
                assertSame(request, it)
                response
            },
        )
        calls shouldBe 1
    }

    @Test
    fun `configured referer non GET and invalid or credentialed origins never retry`() = runBlocking<Unit> {
        val cases = listOf(
            request.newBuilder().header(
                "Referer",
                "https://custom.example/chapter",
            ).build() to "https://source.example",
            request.newBuilder().head().build() to "https://source.example",
            request to "invalid origin",
            request to "https://user:password@source.example",
        )
        cases.forEach { (original, source) ->
            var calls = 0
            val error = HttpException(403)
            val thrown = runCatching {
                fetchImageWithRefererFallback(original, source) {
                    calls++
                    throw error
                }
            }.exceptionOrNull()
            assertSame(error, thrown)
            calls shouldBe 1
        }
    }

    @Test
    fun `other HTTP failures network errors and cancellation are preserved`() = runBlocking<Unit> {
        listOf(
            HttpException(401),
            HttpException(404),
            HttpException(429),
            HttpException(500),
            IOException("offline"),
            CancellationException("cancelled"),
        ).forEach { error ->
            var calls = 0
            val thrown = runCatching {
                fetchImageWithRefererFallback(request, "https://source.example") {
                    calls++
                    throw error
                }
            }.exceptionOrNull()
            assertSame(error, thrown)
            calls shouldBe 1
        }
    }

    @Test
    fun `failed recovery stops after one retry and reports its failure`() = runBlocking<Unit> {
        var calls = 0
        val error = HttpException(403)
        val thrown = runCatching {
            fetchImageWithRefererFallback(request, "https://source.example") {
                calls++
                throw error
            }
        }.exceptionOrNull()
        assertSame(error, thrown)
        calls shouldBe 2
    }

    @Test
    fun `HTTP image rejection recovers through actual OkHttp response handling`() = runBlocking<Unit> {
        ServerSocket(0).use { server ->
            server.soTimeout = 5000
            val received = FutureTask {
                (1..2).map { attempt ->
                    server.accept().use { socket ->
                        socket.soTimeout = 5000
                        val reader = socket.getInputStream().bufferedReader()
                        val headers = generateSequence { reader.readLine() }
                            .takeWhile { !it.isNullOrEmpty() }.toList()
                        val status = if (attempt == 1) "403 Forbidden" else "200 OK"
                        socket.getOutputStream().write(
                            "HTTP/1.1 $status\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray(),
                        )
                        headers.any { it.equals("Referer: https://source.example/", ignoreCase = true) }
                    }
                }
            }
            Thread(received).start()
            val client = OkHttpClient()
            try {
                val original = Request.Builder().url("http://127.0.0.1:${server.localPort}/page.webp").build()
                fetchImageWithRefererFallback(original, "https://source.example") {
                    client.newCall(it).awaitSuccess()
                }.use { it.code shouldBe 200 }
                received.get(5, TimeUnit.SECONDS) shouldBe listOf(false, true)
            } finally {
                client.dispatcher.executorService.shutdown()
                client.connectionPool.evictAll()
            }
        }
    }
}
