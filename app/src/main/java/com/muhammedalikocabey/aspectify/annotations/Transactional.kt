package com.muhammedalikocabey.aspectify.annotations

/**
 * Bu anotasyon, methodun bir işlem (transaction) gibi
 * yönetilmesini sağlar. Başarısız olursa rollback işlemi tetiklenir.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Transactional
