package com.muhammedalikocabey.aspectify.core

/**
 * Aspectify log sistemi için arayüz.
 *
 * Kendi Logger implementasyonlarınızı (ConsoleLogger, AndroidLogger vb.) oluşturabilirsiniz.
 */
interface AspectifyLogger {
    /**
     * Genel bir bilgi logu atar.
     */
    fun log(message: String)

    /**
     * Hata durumlarında log basar.
     *
     * @param throwable İsteğe bağlı hata nesnesi
     */
    fun error(message: String, throwable: Throwable? = null)
}
