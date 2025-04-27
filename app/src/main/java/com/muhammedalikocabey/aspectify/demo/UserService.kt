package com.muhammedalikocabey.aspectify.demo

/**
 * Kullanıcı işlemleri için örnek bir servis arayüzü.
 */
interface UserService {
    fun login(username: String, password: String): Boolean

    fun deleteUser(userId: String)

    fun logout()
}
