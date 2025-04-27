package com.muhammedalikocabey.aspectify.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp için Aspectify interceptor'ü.
 * Her request öncesi ve sonrası loglama ve süre ölçümü yapar.
 */
class AspectifyOkHttpInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val start = System.nanoTime()
        println("[Aspectify-HTTP] Request -> ${request.method} ${request.url}")

        val response = chain.proceed(request)

        val end = System.nanoTime()
        val durationMs = (end - start) / 1_000_000
        println("[Aspectify-HTTP] Response <- ${response.code} (${durationMs} ms)")

        return response
    }
}
