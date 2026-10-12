package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, methodun bir süre zarfında maksimum kaç kez çalıştırılabileceğini sınırlar.
 *
 * @property limit İzin verilen maksimum çağrı sayısı
 * @property durationMs Süre limiti (ms cinsinden)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(val limit: Int, val durationMs: Long)
