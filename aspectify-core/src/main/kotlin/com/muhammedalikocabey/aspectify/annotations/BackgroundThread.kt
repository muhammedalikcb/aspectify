package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, methodun mutlaka background thread'de çalıştırılması gerektiğini belirtir.
 * Eğer UI thread'de çalıştırılırsa IllegalStateException fırlatılır.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BackgroundThread
