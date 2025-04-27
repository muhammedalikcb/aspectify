package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, method çağrıldığında otomatik olarak
 * log basılmasını sağlar.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Loggable
