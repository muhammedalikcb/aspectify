package com.muhammedalikocabey.aspectify

import android.content.Context
import android.os.Looper
import com.muhammedalikocabey.aspectify.core.Aspectify
import com.muhammedalikocabey.aspectify.session.UserSession

/**
 * Aspectify'ı Android'e bağlar.
 *
 * - `@BackgroundThread` için ana thread kontrolünü `Looper` ile yapar.
 * - `@Authenticated` için rolleri [UserSession] üzerinden kontrol eder.
 *
 * Kullanım (Application ya da ilk Activity içinde, bir kez):
 * ```
 * AspectifyAndroid.install(applicationContext)
 * ```
 */
object AspectifyAndroid {

    fun install(context: Context) {
        UserSession.init(context.applicationContext)
        Aspectify.mainThreadChecker = { Looper.getMainLooper().thread == Thread.currentThread() }
        Aspectify.roleChecker = UserSession::hasRole
    }
}
