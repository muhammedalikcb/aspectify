package com.muhammedalikocabey.aspectify.core

import com.muhammedalikocabey.aspectify.annotations.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

interface SuspendTestService {
    suspend fun delayedGreeting(name: String): String

    suspend fun failingAfterDelay(): String

    suspend fun failingWithCheckedException(): String

    @Retryable(times = 3)
    suspend fun flakyOperation(): String

    @Retryable(times = 2)
    suspend fun alwaysFailing(): String

    @Retryable(times = 3)
    @Timeout(millis = 100)
    suspend fun slowOperation(): String

    @Retryable(times = 3)
    suspend fun longRunning(): String

    @Cacheable
    suspend fun cachedOperation(param: String): String

    fun plainOperation(value: Int): Int

    @Retryable(times = 2)
    fun plainFailing(): String
}

class RealSuspendTestService : SuspendTestService {
    var flakyCalls = 0
    var alwaysFailingCalls = 0
    var slowCalls = 0
    var longRunningCalls = 0
    var longRunningCancelled = false
    var cachedCalls = 0

    override suspend fun delayedGreeting(name: String): String {
        delay(10_000)
        return "Hello $name"
    }

    override suspend fun failingAfterDelay(): String {
        delay(100)
        throw IllegalArgumentException("Geçersiz argüman")
    }

    override suspend fun failingWithCheckedException(): String {
        throw IOException("Ağ hatası")
    }

    override suspend fun flakyOperation(): String {
        flakyCalls++
        delay(50)
        if (flakyCalls < 3) throw IllegalStateException("Fail $flakyCalls")
        return "Success"
    }

    override suspend fun alwaysFailing(): String {
        alwaysFailingCalls++
        delay(10)
        throw UnsupportedOperationException("Her zaman başarısız")
    }

    override suspend fun slowOperation(): String {
        slowCalls++
        delay(300)
        return "Slow Result"
    }

    override suspend fun longRunning(): String {
        longRunningCalls++
        try {
            delay(10_000)
        } catch (e: CancellationException) {
            longRunningCancelled = true
            throw e
        }
        return "Done"
    }

    override suspend fun cachedOperation(param: String): String {
        cachedCalls++
        delay(100)
        return "Hello $param"
    }

    override fun plainOperation(value: Int): Int = value * 2

    override fun plainFailing(): String = throw IllegalStateException("Sync fail")
}

@OptIn(ExperimentalCoroutinesApi::class)
class SuspendInterceptorTest {

    private val real = RealSuspendTestService()

    private val service = proxyOf<SuspendTestService> {
        target = real
    }

    private suspend inline fun <reified T : Throwable> expectThrows(crossinline block: suspend () -> Unit): T {
        try {
            block()
        } catch (e: Throwable) {
            if (e is T) return e
            throw AssertionError("Beklenen ${T::class.java.name}, gelen ${e::class.java.name}", e)
        }
        throw AssertionError("Beklenen ${T::class.java.name} fırlatılmadı")
    }

    @Test
    fun suspendMethodReturnsValueWithoutBlocking() = runTest {
        assertEquals("Hello World", service.delayedGreeting("World"))
        assertEquals(10_000L, currentTime)

        // Tek thread üzerinde iki çağrı aynı anda askıda kalabilmeli
        val first = async { service.delayedGreeting("A") }
        val second = async { service.delayedGreeting("B") }
        assertEquals("Hello A", first.await())
        assertEquals("Hello B", second.await())
        assertEquals(20_000L, currentTime)
    }

    @Test
    fun suspendExceptionKeepsOriginalType() = runTest {
        val error = expectThrows<IllegalArgumentException> { service.failingAfterDelay() }
        assertEquals("Geçersiz argüman", error.message)
    }

    @Test
    fun checkedExceptionThrownBeforeSuspensionKeepsOriginalType() = runTest {
        val error = expectThrows<IOException> { service.failingWithCheckedException() }
        assertEquals("Ağ hatası", error.message)
    }

    @Test
    fun retryableRetriesSuspendCallUntilSuccess() = runTest {
        assertEquals("Success", service.flakyOperation())
        assertEquals(3, real.flakyCalls)
        assertEquals(150L, currentTime)
    }

    @Test
    fun retryableRethrowsOriginalExceptionWhenLimitExceeded() = runTest {
        expectThrows<UnsupportedOperationException> { service.alwaysFailing() }
        assertEquals(2, real.alwaysFailingCalls)
    }

    @Test
    fun timeoutThrowsTimeoutCancellationExceptionAndIsNotRetried() = runTest {
        expectThrows<TimeoutCancellationException> { service.slowOperation() }
        assertEquals(1, real.slowCalls)
        assertEquals(100L, currentTime)
    }

    @Test
    fun cancellingCallerCancelsTargetWithoutRetry() = runTest {
        val job = launch { service.longRunning() }

        advanceTimeBy(1_000)
        assertEquals(1, real.longRunningCalls)

        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertTrue(real.longRunningCancelled)
        assertEquals(1, real.longRunningCalls)
    }

    @Test
    fun cacheableWorksForSuspendMethods() = runTest {
        assertEquals("Hello World", service.cachedOperation("World"))
        assertEquals("Hello World", service.cachedOperation("World"))
        assertEquals(1, real.cachedCalls)

        assertEquals("Hello Kotlin", service.cachedOperation("Kotlin"))
        assertEquals(2, real.cachedCalls)
    }

    @Test
    fun nonSuspendMethodsKeepWorking() {
        assertEquals(42, service.plainOperation(21))

        val error = assertThrows(AspectifyException::class.java) { service.plainFailing() }
        assertTrue(error.cause?.cause is IllegalStateException)
    }
}
