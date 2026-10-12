package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, method sonucunun cache'lenmesini sağlar.
 * Aynı parametrelerle çağrılırsa tekrar hesaplamak yerine cache'den döner.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cacheable
