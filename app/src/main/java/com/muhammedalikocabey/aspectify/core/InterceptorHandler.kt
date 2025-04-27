package com.muhammedalikocabey.aspectify.core

import android.os.Looper
import com.muhammedalikocabey.aspectify.annotations.*
import com.muhammedalikocabey.aspectify.session.UserSession
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Aspectify'nın kalbi olan sınıf.
 * Tüm proxy method çağrılarını yakalar ve ilgili anotasyonlara göre işlem yapar.
 *
 * Desteklenen Anotasyonlar:
 * - @Loggable
 * - @Retryable
 * - @Authenticated
 * - @Transactional
 * - @Cacheable
 * - @Timed
 * - @Debounce
 * - @Timeout
 * - @RateLimit
 * - @BackgroundThread
 *
 * Aspectify, her method çalıştırmadan önce bu anotasyonların kurallarını uygular.
 */
class InterceptorHandler(private val target: Any) : InvocationHandler {

    companion object {
        /**
         * Loglama işlemleri için kullanılan varsayılan logger.
         */
        var defaultLogger: AspectifyLogger = ConsoleLogger
    }

    private val cache = mutableMapOf<String, Any?>()
    private val lastCallTimes = mutableMapOf<String, Long>()
    private val rateLimits = mutableMapOf<String, Pair<Int, Long>>() // methodName -> (kalanHak, sonSıfırlamaZamanı)

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        val isLoggable = method.isAnnotationPresent(Loggable::class.java)
        val retryable = method.getAnnotation(Retryable::class.java)
        val authenticated = method.getAnnotation(Authenticated::class.java)
        val isTimed = method.isAnnotationPresent(Timed::class.java)
        val isTransactional = method.isAnnotationPresent(Transactional::class.java)
        val isCacheable = method.isAnnotationPresent(Cacheable::class.java)
        val debounce = method.getAnnotation(Debounce::class.java)
        val timeout = method.getAnnotation(Timeout::class.java)
        val rateLimit = method.getAnnotation(RateLimit::class.java)
        val backgroundThread = method.getAnnotation(BackgroundThread::class.java)

        val methodName = method.name
        val cacheKey = "$methodName-${args?.joinToString() ?: ""}"

        // Debounce kontrolü
        if (debounce != null) {
            val lastCallTime = lastCallTimes[methodName] ?: 0
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCallTime < debounce.millis) {
                defaultLogger.log("$methodName debounce uygulandı, çağrı atlandı.")
                return null
            }
            lastCallTimes[methodName] = currentTime
        }

        // RateLimit kontrolü
        if (rateLimit != null) {
            val currentTime = System.currentTimeMillis()
            val (remainingCalls, lastResetTime) = rateLimits[methodName] ?: (rateLimit.limit to currentTime)
            val elapsed = currentTime - lastResetTime

            if (elapsed > rateLimit.durationMs) {
                rateLimits[methodName] = rateLimit.limit to currentTime
            } else {
                if (remainingCalls <= 0) {
                    defaultLogger.log("$methodName rate limit aşıldı, çağrı atlandı.")
                    return null
                } else {
                    rateLimits[methodName] = (remainingCalls - 1) to lastResetTime
                }
            }
        }

        // Yetkilendirme kontrolü
        if (authenticated != null && !UserSession.hasRole(authenticated.role)) {
            throw AuthorizationFailureException()
        }

        // BackgroundThread kontrolü
        if (backgroundThread != null && Looper.getMainLooper().thread == Thread.currentThread()) {
            throw IllegalStateException("$methodName sadece background thread'de çalıştırılabilir.")
        }

        // Cache kontrolü
        if (isCacheable && cache.containsKey(cacheKey)) {
            defaultLogger.log("$methodName cache'den alındı.")
            return cache[cacheKey]
        }

        var attempt = 0
        val maxAttempts = retryable?.times ?: 1

        while (true) {
            try {
                if (isLoggable) defaultLogger.log("$methodName başlıyor...")

                val start = if (isTimed) System.nanoTime() else 0L

                val finalCall: suspend () -> Any? = {
                    if (isTransactional) {
                        defaultLogger.log("$methodName işlemi başladı.")
                    }

                    val result = method.invoke(target, *(args ?: arrayOf()))

                    if (isTransactional) {
                        defaultLogger.log("$methodName işlemi başarıyla tamamlandı.")
                    }

                    if (isCacheable) {
                        cache[cacheKey] = result
                        defaultLogger.log("$methodName sonucu cache'lendi.")
                    }

                    result
                }

                val result = runBlocking {
                    try {
                        if (timeout != null) {
                            withTimeout(timeout.millis) {
                                finalCall()
                            }
                        } else {
                            finalCall()
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        throw AspectifyTimeoutException("${methodName} timeout süresi aşıldı.")
                    }
                }


                if (isTimed) {
                    val end = System.nanoTime()
                    val durationMs = (end - start) / 1_000_000
                    defaultLogger.log("$methodName çalışması $durationMs ms sürdü.")
                }

                if (isLoggable) defaultLogger.log("$methodName tamamlandı.")

                return result
            } catch (e: Exception) {
                attempt++
                if (isTransactional) {
                    defaultLogger.error("$methodName işlemi başarısız. Rollback uygulanıyor...", e)
                }
                if (attempt >= maxAttempts) {
                    defaultLogger.error("$methodName başarısız oldu. Retry limiti aşıldı.", e)
                    throw AspectifyException("$methodName çalıştırılamadı.", e)
                }
                defaultLogger.log("$methodName tekrar deneniyor. Deneme: $attempt")
            }
        }
    }
}
