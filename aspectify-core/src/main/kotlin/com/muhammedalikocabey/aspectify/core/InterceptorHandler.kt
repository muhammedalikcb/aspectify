package com.muhammedalikocabey.aspectify.core

import com.muhammedalikocabey.aspectify.annotations.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resumeWithException

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
 *
 * `suspend` methodlar thread bloklanmadan, gerçek coroutine olarak çalıştırılır.
 * Bu methodlarda hata orijinal tipiyle fırlatılır, `@Timeout` aşımında
 * `TimeoutCancellationException` fırlatılır ve iptal (`CancellationException`) tekrar denenmez.
 */
class InterceptorHandler(private val target: Any) : InvocationHandler {

    companion object {
        /**
         * Loglama işlemleri için kullanılan varsayılan logger.
         */
        var defaultLogger: AspectifyLogger = ConsoleLogger
    }

    /**
     * Ön kontrollerin çağrıyı engellemediğini belirten işaret nesnesi.
     */
    private object Proceed

    private val cache = mutableMapOf<String, Any?>()
    private val lastCallTimes = mutableMapOf<String, Long>()
    private val rateLimits = mutableMapOf<String, Pair<Int, Long>>() // methodName -> (kalanHak, sonSıfırlamaZamanı)

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        if (method.isSuspend()) {
            return invokeSuspend(method, args!!)
        }

        val isLoggable = method.isAnnotationPresent(Loggable::class.java)
        val retryable = method.getAnnotation(Retryable::class.java)
        val isTimed = method.isAnnotationPresent(Timed::class.java)
        val isTransactional = method.isAnnotationPresent(Transactional::class.java)
        val isCacheable = method.isAnnotationPresent(Cacheable::class.java)
        val timeout = method.getAnnotation(Timeout::class.java)

        val methodName = method.name
        val cacheKey = "$methodName-${args?.joinToString() ?: ""}"

        val precheck = checkPreconditions(method, cacheKey)
        if (precheck !== Proceed) return precheck

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

    /**
     * Debounce, RateLimit, yetki, thread ve cache kontrollerini uygular.
     *
     * @return Çağrı devam edecekse [Proceed], aksi halde çağıranın alacağı değer (null veya cache sonucu)
     */
    private fun checkPreconditions(method: Method, cacheKey: String): Any? {
        val authenticated = method.getAnnotation(Authenticated::class.java)
        val isCacheable = method.isAnnotationPresent(Cacheable::class.java)
        val debounce = method.getAnnotation(Debounce::class.java)
        val rateLimit = method.getAnnotation(RateLimit::class.java)
        val backgroundThread = method.getAnnotation(BackgroundThread::class.java)

        val methodName = method.name

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
        if (authenticated != null && !Aspectify.roleChecker(authenticated.role)) {
            throw AuthorizationFailureException()
        }

        // BackgroundThread kontrolü
        if (backgroundThread != null && Aspectify.mainThreadChecker()) {
            throw IllegalStateException("$methodName sadece background thread'de çalıştırılabilir.")
        }

        // Cache kontrolü
        if (isCacheable && cache.containsKey(cacheKey)) {
            defaultLogger.log("$methodName cache'den alındı.")
            return cache[cacheKey]
        }

        return Proceed
    }

    /**
     * Kotlin `suspend` methodları derlendiğinde son parametre olarak `Continuation` alır.
     */
    private fun Method.isSuspend(): Boolean =
        parameterTypes.lastOrNull() == Continuation::class.java

    /**
     * `suspend` method çağrısını, proxy'ye gelen `Continuation` üzerinden coroutine olarak başlatır.
     * Method askıya alınırsa `COROUTINE_SUSPENDED` döner, sonuç daha sonra continuation'a iletilir.
     */
    @Suppress("UNCHECKED_CAST")
    private fun invokeSuspend(method: Method, args: Array<out Any>): Any? {
        val continuation = args.last() as Continuation<Any?>
        val realArgs = Array<Any?>(args.size - 1) { args[it] }

        val call: suspend () -> Any? = {
            try {
                interceptSuspend(method, realArgs)
            } catch (e: Exception) {
                // Checked exception'lar senkron fırlatılırsa Proxy onları UndeclaredThrowableException'a sarar.
                // Bunu önlemek için hata, askıya alındıktan sonra continuation üzerinden iletilir.
                if (e is RuntimeException) throw e
                e.suspendAndThrow()
            }
        }

        return call.startCoroutineUninterceptedOrReturn(continuation)
    }

    /**
     * `suspend` methodlar için anotasyon zincirini runBlocking kullanmadan uygular.
     */
    private suspend fun interceptSuspend(method: Method, args: Array<Any?>): Any? {
        val isLoggable = method.isAnnotationPresent(Loggable::class.java)
        val retryable = method.getAnnotation(Retryable::class.java)
        val isTimed = method.isAnnotationPresent(Timed::class.java)
        val isTransactional = method.isAnnotationPresent(Transactional::class.java)
        val isCacheable = method.isAnnotationPresent(Cacheable::class.java)
        val timeout = method.getAnnotation(Timeout::class.java)

        val methodName = method.name
        val cacheKey = "$methodName-${args.joinToString()}"

        val precheck = checkPreconditions(method, cacheKey)
        if (precheck !== Proceed) return precheck

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

                    val result = invokeTargetSuspend(method, args)

                    if (isTransactional) {
                        defaultLogger.log("$methodName işlemi başarıyla tamamlandı.")
                    }

                    if (isCacheable) {
                        cache[cacheKey] = result
                        defaultLogger.log("$methodName sonucu cache'lendi.")
                    }

                    result
                }

                val result = if (timeout != null) {
                    withTimeout(timeout.millis) {
                        finalCall()
                    }
                } else {
                    finalCall()
                }

                if (isTimed) {
                    val end = System.nanoTime()
                    val durationMs = (end - start) / 1_000_000
                    defaultLogger.log("$methodName çalışması $durationMs ms sürdü.")
                }

                if (isLoggable) defaultLogger.log("$methodName tamamlandı.")

                return result
            } catch (e: CancellationException) {
                // İptal ve timeout tekrar denenmez, olduğu gibi yukarı iletilir.
                if (isTransactional) {
                    defaultLogger.error("$methodName işlemi iptal edildi. Rollback uygulanıyor...", e)
                }
                throw e
            } catch (e: Exception) {
                attempt++
                if (isTransactional) {
                    defaultLogger.error("$methodName işlemi başarısız. Rollback uygulanıyor...", e)
                }
                if (attempt >= maxAttempts) {
                    defaultLogger.error("$methodName başarısız oldu. Retry limiti aşıldı.", e)
                    throw e
                }
                defaultLogger.log("$methodName tekrar deneniyor. Deneme: $attempt")
            }
        }
    }

    /**
     * Hedef nesnenin `suspend` methodunu mevcut continuation ile çağırır.
     * Hedef askıya alınırsa bu fonksiyon da askıya alınır.
     */
    private suspend fun invokeTargetSuspend(method: Method, args: Array<Any?>): Any? =
        suspendCoroutineUninterceptedOrReturn { continuation ->
            try {
                method.invoke(target, *args, continuation)
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }

    /**
     * Hatayı coroutine gerçekten askıya alındıktan sonra fırlatır.
     */
    private suspend fun Exception.suspendAndThrow(): Nothing {
        suspendCoroutineUninterceptedOrReturn<Nothing> { continuation ->
            Dispatchers.Default.dispatch(continuation.context) {
                continuation.intercepted().resumeWithException(this@suspendAndThrow)
            }
            COROUTINE_SUSPENDED
        }
    }
}
