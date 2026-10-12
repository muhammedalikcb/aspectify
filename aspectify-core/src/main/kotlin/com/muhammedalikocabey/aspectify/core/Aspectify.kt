package com.muhammedalikocabey.aspectify.core

/**
 * Aspectify'ın platforma bağlı davranışlarını ayarlamak için kullanılan merkezi yapılandırma.
 *
 * Çekirdek modül saf Kotlin/JVM'dir. Android projelerinde `aspectify-android` modülündeki
 * `AspectifyAndroid.install(context)` bu değerleri Android'e uygun şekilde ayarlar.
 *
 * Kullanım (saf JVM):
 * ```
 * Aspectify.roleChecker = { role -> currentUser.roles.contains(role) }
 * ```
 */
object Aspectify {

    /**
     * Çağrının ana (UI) thread'den yapılıp yapılmadığını döner. `@BackgroundThread` tarafından kullanılır.
     * Saf JVM'de ana thread kavramı olmadığı için varsayılan değer her zaman `false` döner.
     */
    @Volatile
    var mainThreadChecker: () -> Boolean = { false }

    /**
     * Mevcut kullanıcının verilen role sahip olup olmadığını döner. `@Authenticated` tarafından kullanılır.
     * Varsayılan olarak hiçbir role izin verilmez, yani yapılandırılmadığında yetki kontrolü başarısız olur.
     */
    @Volatile
    var roleChecker: (role: String) -> Boolean = { false }
}
