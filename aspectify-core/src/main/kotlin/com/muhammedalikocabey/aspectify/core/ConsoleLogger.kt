package com.muhammedalikocabey.aspectify.core

/**
 * Basit konsol üzerine log basan Aspectify logger implementasyonu.
 *
 * JVM ortamında (Android harici) kullanılması önerilir.
 */
object ConsoleLogger : AspectifyLogger {
    override fun log(message: String) {
        println("[Aspectify-Log] $message")
    }

    override fun error(message: String, throwable: Throwable?) {
        println("[Aspectify-Error] $message")
        throwable?.printStackTrace()
    }
}
