package com.muhammedalikocabey.aspectify.demo

import com.muhammedalikocabey.aspectify.core.proxyOf
import com.muhammedalikocabey.aspectify.session.UserSession

/**
 * Aspectify sisteminin JVM demo kullanım örneği.
 */
fun main() {
    val proxyService = proxyOf<UserService> {
        target = RealUserService()
        // İstersen burada logger değiştirilebilir: logger = ConsoleLogger / AndroidLogger
    }

    UserSession.loginAs("admin") // admin olarak giriş yapıyoruz

    proxyService.login("admin", "1234")

    try {
        proxyService.deleteUser("user123")
    } catch (e: Exception) {
        println("Kullanıcı silme hatası: ${e.message}")
    }

    proxyService.logout()
}
