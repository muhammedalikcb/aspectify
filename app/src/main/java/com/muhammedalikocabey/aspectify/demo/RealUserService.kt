package com.muhammedalikocabey.aspectify.demo

import com.muhammedalikocabey.aspectify.annotations.Authenticated
import com.muhammedalikocabey.aspectify.annotations.Loggable
import com.muhammedalikocabey.aspectify.annotations.Retryable
import com.muhammedalikocabey.aspectify.annotations.Timed

/**
 * UserService arayüzünün gerçek implementasyonu.
 */
class RealUserService : UserService {

    @Loggable
    override fun login(username: String, password: String): Boolean {
        println("Gerçek login işlemi yapılıyor...")
        return username == "admin" && password == "1234"
    }

    @Loggable
    @Retryable(times = 2)
    @Authenticated(role = "admin")
    override fun deleteUser(userId: String) {
        println("Kullanıcı siliniyor: $userId")
        throw RuntimeException("Simüle edilen hata!")
    }

    @Loggable
    override fun logout() {
        println("Gerçek logout işlemi yapılıyor...")
    }

    @Timed
    @Loggable
    fun simulateHeavyOperation() {
        println("Ağır işlem başlatıldı...")
        Thread.sleep(500) // 500ms bekletiyoruz
        println("Ağır işlem tamamlandı.")
    }

}
