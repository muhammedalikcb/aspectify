package com.muhammedalikocabey.aspectify.core

import java.lang.reflect.Proxy

/**
 * ProxyBuilder, verilen bir arayüz (interface) için dinamik bir proxy oluşturulmasını sağlar.
 *
 * Kullanım:
 * ```
 * val service = proxyOf<ServiceInterface> {
 *     target = RealServiceImplementation()
 * }
 * ```
 *
 * Burada proxy, araya girerek method çağrılarını yakalar ve Aspectify'ın sunduğu
 * AOP destekli işlemleri uygular.
 */
class ProxyBuilder<T : Any>(
    private val interfaceClass: Class<T>
) {
    lateinit var target: T
    var logger: AspectifyLogger = ConsoleLogger

    @Suppress("UNCHECKED_CAST")
    fun build(): T {
        InterceptorHandler.defaultLogger = logger
        return Proxy.newProxyInstance(
            interfaceClass.classLoader,
            arrayOf(interfaceClass),
            InterceptorHandler(target)
        ) as T
    }
}

/**
 * Aspectify framework için DSL destekli proxy oluşturucu.
 *
 * @param block ProxyBuilder konfigürasyon bloğu
 */
inline fun <reified T : Any> proxyOf(block: ProxyBuilder<T>.() -> Unit): T {
    val builder = ProxyBuilder(T::class.java)
    builder.block()
    return builder.build()
}
