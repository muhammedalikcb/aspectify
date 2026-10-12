package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, method çağrıları arasında minimum bekleme süresi uygulanmasını sağlar.
 * Genellikle hızlı ardışık button tıklamalarını engellemek için kullanılır.
 *
 * @property millis Minimum bekleme süresi (ms cinsinden)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Debounce(val millis: Long = 500)
