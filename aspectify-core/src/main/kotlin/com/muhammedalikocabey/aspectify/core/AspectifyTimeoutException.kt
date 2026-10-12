package com.muhammedalikocabey.aspectify.core

/**
 * Method belirlenen süre limiti içinde tamamlanmazsa fırlatılır.
 */
class AspectifyTimeoutException(message: String = "Method timeout süresini aştı.") : AspectifyException(message)
