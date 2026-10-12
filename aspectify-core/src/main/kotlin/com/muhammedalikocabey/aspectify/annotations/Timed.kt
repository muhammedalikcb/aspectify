package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, methodun çalışma süresinin otomatik olarak ölçülmesini sağlar.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Timed
