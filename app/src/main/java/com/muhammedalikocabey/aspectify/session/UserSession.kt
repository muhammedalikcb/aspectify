package com.muhammedalikocabey.aspectify.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Kullanıcının rol bilgisini saklayan basit oturum yönetim sınıfı.
 *
 * SharedPreferences kullanılarak kalıcı olarak veri saklar.
 *
 * Kullanım:
 * - `UserSession.init(context)`
 * - `UserSession.loginAs("admin")`
 * - `UserSession.hasRole("admin")`
 */
object UserSession {

    private lateinit var preferences: SharedPreferences

    private const val KEY_ROLE = "user_role"
    private const val DEFAULT_ROLE = "guest"

    fun init(context: Context) {
        preferences = context.getSharedPreferences("AspectifySession", Context.MODE_PRIVATE)
    }

    fun loginAs(role: String) {
        preferences.edit().putString(KEY_ROLE, role).apply()
    }

    fun logout() {
        preferences.edit().putString(KEY_ROLE, DEFAULT_ROLE).apply()
    }

    fun hasRole(role: String): Boolean {
        return preferences.getString(KEY_ROLE, DEFAULT_ROLE) == role
    }
}
