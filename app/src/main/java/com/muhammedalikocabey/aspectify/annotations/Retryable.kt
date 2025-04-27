package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, method çalıştırılırken hata oluşursa
 * belirli sayıda tekrar denenmesini sağlar.
 *
 * @property times Yeniden deneme sayısı
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Retryable(val times: Int = 3)
