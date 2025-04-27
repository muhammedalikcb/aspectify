package com.muhammedalikocabey.aspectify.core

import com.muhammedalikocabey.aspectify.annotations.*
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking

interface TestService {
    @Loggable
    @Retryable(times = 2)
    fun riskyOperation(): String

    @Cacheable
    fun cachedOperation(param: String): String

    @Timeout(millis = 100)
    fun slowOperation(): String

    @BackgroundThread
    fun backgroundOnlyOperation(): String

    @RateLimit(limit = 2, durationMs = 1000)
    fun limitedOperation(): String
}

class RealTestService : TestService {
    private var callCount = 0

    override fun riskyOperation(): String {
        callCount++
        if (callCount < 2) throw RuntimeException("Fail!")
        return "Success"
    }

    override fun cachedOperation(param: String): String {
        return "Hello $param"
    }

    override fun slowOperation(): String {
        runBlocking {
            kotlinx.coroutines.delay(300)
        }
        return "Slow Result"
    }

    override fun backgroundOnlyOperation(): String {
        return "Background Success"
    }

    override fun limitedOperation(): String {
        return "Limited Success"
    }
}

class InterceptorHandlerTest {

    private val service = proxyOf<TestService> {
        target = RealTestService()
    }

    @Test
    fun testRetryable() {
        val result = service.riskyOperation()
        assertEquals("Success", result)
    }

    @Test
    fun testCacheable() {
        val first = service.cachedOperation("World")
        val second = service.cachedOperation("World")
        assertEquals(first, second)
    }

    @Test
    fun testRateLimit() {
        val result1 = service.limitedOperation()
        val result2 = service.limitedOperation()
        val result3 = service.limitedOperation()

        assertEquals("Limited Success", result1)
        assertEquals("Limited Success", result2)
        assertNull(result3)
    }
}
