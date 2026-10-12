package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, method çağrılmadan önce
 * kullanıcının belirli bir role sahip olup olmadığını kontrol eder.
 *
 * @property role Erişim için gereken kullanıcı rolü
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authenticated(val role: String)
