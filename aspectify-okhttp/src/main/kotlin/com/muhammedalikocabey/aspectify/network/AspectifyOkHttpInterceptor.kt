package com.muhammedalikocabey.aspectify.network

import com.muhammedalikocabey.aspectify.core.AspectifyLogger
import com.muhammedalikocabey.aspectify.core.ConsoleLogger
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp için Aspectify interceptor'ü.
 * Her request öncesi ve sonrası loglama ve süre ölçümü yapar.
 *
 * @param logger Logların yazılacağı logger. Android'de `AndroidLogger` verilebilir.
 */
class AspectifyOkHttpInterceptor @JvmOverloads constructor(
    private val logger: AspectifyLogger = ConsoleLogger,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val start = System.nanoTime()
        logger.log("[HTTP] Request -> ${request.method} ${request.url}")

        val response = chain.proceed(request)

        val end = System.nanoTime()
        val durationMs = (end - start) / 1_000_000
        logger.log("[HTTP] Response <- ${response.code} (${durationMs} ms)")

        return response
    }
}
