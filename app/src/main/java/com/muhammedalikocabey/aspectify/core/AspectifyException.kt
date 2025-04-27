package com.muhammedalikocabey.aspectify.core

/**
 * Aspectify sisteminde oluşabilecek tüm özel hataların üst sınıfıdır.
 */
open class AspectifyException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Yetkilendirme hatası durumunda fırlatılır.
 */
class AuthorizationFailureException(message: String = "Yetkisiz Erişim!") : AspectifyException(message)
