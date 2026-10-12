package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, methodun belirtilen sürede tamamlanmasını zorunlu kılar.
 * Süre aşımı olursa TimeoutException fırlatılır.
 *
 * @property millis Süre limiti (ms cinsinden)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Timeout(val millis: Long)
