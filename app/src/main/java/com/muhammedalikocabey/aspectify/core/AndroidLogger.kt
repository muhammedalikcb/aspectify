package com.muhammedalikocabey.aspectify.core

import android.util.Log

/**
 * Android Logcat üzerinde loglama yapan Aspectify logger implementasyonu.
 *
 * Not: Sadece Android ortamında kullanılmalıdır.
 */
object AndroidLogger : AspectifyLogger {
    private const val TAG = "Aspectify"

    override fun log(message: String) {
        Log.d(TAG, message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Log.e(TAG, message, throwable)
    }
}
