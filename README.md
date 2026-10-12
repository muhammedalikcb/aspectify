# Aspectify

[![Maven Central](https://img.shields.io/maven-central/v/io.github.muhammedalikcb/aspectify-core)](https://central.sonatype.com/artifact/io.github.muhammedalikcb/aspectify-core)
[![CI](https://github.com/muhammedalikcb/aspectify/actions/workflows/ci.yml/badge.svg)](https://github.com/muhammedalikcb/aspectify/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
[![Last Commit](https://img.shields.io/github/last-commit/muhammedalikcb/aspectify)](https://github.com/muhammedalikcb/aspectify/commits/main)
[![Stars](https://img.shields.io/github/stars/muhammedalikcb/aspectify?style=social)](https://github.com/muhammedalikcb/aspectify/stargazers)

---

## 🇹🇷 Türkçe

### ✨ Proje Tanıtımı

**Aspectify**, Android ve Kotlin JVM projeleri için geliştirilen, hafif, modüler ve modern bir **Aspect-Oriented Programming (AOP)** framework'üdür.

Amacı, uygulamalarda tekrar eden işlemleri (loglama, yetkilendirme, timeout, cache vb.) **ana iş mantığından ayırarak** temiz, okunabilir ve yönetilebilir kod mimarisi sağlamaktır.

Dynamic Proxy ve Kotlin Reflection altyapısıyla çalışır.

---

### 🎯 Proje Hedefi

- **Cross-Cutting Concern'ları** (Loglama, Güvenlik, Performans, Caching) merkezi yönetmek,
- **Business Logic** kodlarının sade ve net olmasını sağlamak,
- **Test edilebilirliği** artırmak,
- **Kod tekrarını azaltmak**.

---

### 🚀 Desteklenen Özellikler

- `@Loggable` → Method giriş/çıkış loglaması
- `@Retryable` → Hata alınca belirli sayıda tekrar deneme
- `@Authenticated` → Kullanıcı rol kontrolü
- `@Transactional` → İşlem (Transaction) yönetimi
- `@Cacheable` → Method sonuçlarını cache'leme
- `@Timed` → Method çalışma süresi ölçümü
- `@Debounce` → Hızlı ardışık method çağrılarını engelleme
- `@Timeout` → Belirli sürede method tamamlama zorunluluğu
- `@RateLimit` → Belirli zaman diliminde çağrı sınırı
- `@BackgroundThread` → Sadece background thread üzerinde çalıştırma

---

### 📦 Kurulum ve Kullanım

#### 0. Bağımlılığı Ekleme

Aspectify Maven Central'da üç modül olarak yayınlanır:

| Modül | Ne için |
| --- | --- |
| `aspectify-core` | Anotasyonlar, proxy ve interceptor. Saf Kotlin/JVM, Android'e bağımlı değil. |
| `aspectify-android` | `AndroidLogger`, `UserSession` ve ana thread kontrolü. `aspectify-core`'u da getirir. |
| `aspectify-okhttp` | OkHttp istekleri için loglama ve süre ölçümü yapan interceptor. |

```kotlin
dependencies {
    implementation("io.github.muhammedalikcb:aspectify-android:1.0.0") // Android
    // implementation("io.github.muhammedalikcb:aspectify-core:1.0.0") // saf Kotlin/JVM
    // implementation("io.github.muhammedalikcb:aspectify-okhttp:1.0.0") // OkHttp
}
```

Android'de uygulama açılırken bir kez çağırın. `@BackgroundThread` ve `@Authenticated` bu sayede çalışır:

```kotlin
AspectifyAndroid.install(applicationContext)
```

Saf JVM'de rol kontrolünü kendiniz bağlayın:

```kotlin
Aspectify.roleChecker = { role -> currentUser.roles.contains(role) }
```

#### 1. Proxy Nesnesi Oluşturma

```kotlin
val service = proxyOf<UserService> {
    target = RealUserService()
    logger = ConsoleLogger // Android projelerinde AndroidLogger da kullanılabilir
}
```

#### 2. Annotation Kullanımı

```kotlin
@Loggable
@Retryable(times = 3)
@Authenticated(role = "admin")
fun deleteUser(userId: String) {
    // Kullanıcı silme işlemleri
}
```

#### 3. Proxy Üzerinden Çağrı Yapmak

```kotlin
val service = proxyOf<UserService> {
    target = RealUserService()
}

service.deleteUser("user123")
```

---

### ⚙️ Gereksinimler

- Kotlin 2.0+
- JVM 11+ (`aspectify-core`, `aspectify-okhttp`)
- Android minSdk 24 (`aspectify-android`)
- Kotlin Coroutines (bağımlılık olarak otomatik gelir)

---

### 🔄 Continuous Integration (CI)

Her push ve pull request'te GitHub Actions projeyi derler, testleri çalıştırır ve kütüphaneleri yerel Maven deposuna yayınlayarak paketlemenin bozulmadığını kontrol eder (`.github/workflows/ci.yml`).

`v1.0.0` gibi bir etiket gönderildiğinde `.github/workflows/release.yml` kütüphaneleri imzalayıp Maven Central'a yayınlar ve bir GitHub Release oluşturur.


---

### 📑 Dikkat Edilmesi Gerekenler

- `AndroidLogger` **sadece Android projelerinde** kullanılmalıdır.
- JVM projeleri için **ConsoleLogger** kullanılması önerilir.
- `UserSession` sınıfı SharedPreferences kullanarak kalıcı oturum yönetimi sağlar.

---

### 🧪 Test Kapsamı

- Retry mekanizması test edilmiştir.
- Cache işlemleri test edilmiştir.
- Timeout yönetimi doğrulanmıştır.
- Background thread kontrolü sağlanmıştır.
- Rate limit sınırlamaları test edilmiştir.

Tüm detaylar `InterceptorHandlerTest` dosyasında bulunmaktadır.

---

### 🛠️ Katkıda Bulunmak

Katkıda bulunmak isterseniz:

1. Fork yapın 🍴
2. Feature branch oluşturun (`feature/ozellik-ekleme`)
3. Pull request gönderin ✔️

---

### 🛡️ Lisans

Bu proje [MIT License](LICENSE) altında lisanslanmıştır.

---

## 🇬🇧 English

### ✨ Project Introduction

**Aspectify** is a lightweight, modular, and modern **Aspect-Oriented Programming (AOP)** framework designed for Android and Kotlin JVM projects.

It aims to **separate cross-cutting concerns** (logging, authentication, timeout, caching etc.) from core business logic, achieving cleaner, readable, and maintainable code structure.

Built on top of Dynamic Proxy and Kotlin Reflection.

---

### 🎯 Project Goals

- Manage **cross-cutting concerns** centrally,
- Keep **business logic** clean and focused,
- Increase **testability**,
- Reduce **code duplication**.

---

### 🚀 Supported Features

- `@Loggable` → Logs method entry/exit
- `@Retryable` → Retries on failure
- `@Authenticated` → User role validation
- `@Transactional` → Transaction management
- `@Cacheable` → Caches method results
- `@Timed` → Measures execution time
- `@Debounce` → Debounces rapid method calls
- `@Timeout` → Forces method completion within time
- `@RateLimit` → Rate limits method calls
- `@BackgroundThread` → Forces background thread execution

---

### 📦 Installation & Usage

#### 0. Add the Dependency

Aspectify is published to Maven Central as three modules:

| Module | Use it for |
| --- | --- |
| `aspectify-core` | Annotations, proxy and interceptor. Pure Kotlin/JVM, no Android dependency. |
| `aspectify-android` | `AndroidLogger`, `UserSession` and main thread checks. Brings in `aspectify-core`. |
| `aspectify-okhttp` | OkHttp interceptor that logs requests and their duration. |

```kotlin
dependencies {
    implementation("io.github.muhammedalikcb:aspectify-android:1.0.0") // Android
    // implementation("io.github.muhammedalikcb:aspectify-core:1.0.0") // plain Kotlin/JVM
    // implementation("io.github.muhammedalikcb:aspectify-okhttp:1.0.0") // OkHttp
}
```

On Android, call this once at startup so `@BackgroundThread` and `@Authenticated` work:

```kotlin
AspectifyAndroid.install(applicationContext)
```

On plain JVM, plug in your own role check:

```kotlin
Aspectify.roleChecker = { role -> currentUser.roles.contains(role) }
```

#### 1. Create a Proxy

```kotlin
val service = proxyOf<UserService> {
    target = RealUserService()
    logger = ConsoleLogger // Or AndroidLogger for Android platform
}
```

#### 2. Annotate Your Methods

```kotlin
@Loggable
@Retryable(times = 3)
@Authenticated(role = "admin")
fun deleteUser(userId: String) {
    // User deletion logic
}
```

#### 3. Make Calls via Proxy

```kotlin
val service = proxyOf<UserService> {
    target = RealUserService()
}

service.deleteUser("user123")
```

---

### ⚙️ Requirements

- Kotlin 2.0+
- JVM 11+ (`aspectify-core`, `aspectify-okhttp`)
- Android minSdk 24 (`aspectify-android`)
- Kotlin Coroutines (pulled in automatically)

---

### 🔄 Continuous Integration (CI)

On every push and pull request GitHub Actions builds the project, runs the tests and publishes the libraries to the local Maven repository to make sure packaging still works (`.github/workflows/ci.yml`).

Pushing a tag like `v1.0.0` runs `.github/workflows/release.yml`, which signs the libraries, publishes them to Maven Central and creates a GitHub Release.


---

### 📑 Important Notes

- Use `AndroidLogger` **only on Android platform**.
- For JVM/Desktop use **ConsoleLogger**.
- `UserSession` provides persistent user role management using SharedPreferences.

---

### 🧪 Test Coverage

- Retry mechanism verified.
- Cache behavior tested.
- Timeout handling validated.
- Background thread enforcement checked.
- Rate limiting functionality confirmed.

Check the `InterceptorHandlerTest` file for more.

---

### 🛠️ Contributing

If you wish to contribute:

1. Fork 🍴
2. Create a feature branch (`feature/add-something`)
3. Open a pull request ✔️

---

### 🛡️ License

Distributed under the [MIT License](LICENSE).

---

## 📢 Project Summary

> **Aspectify** delivers a modern, modular, and powerful code architecture by integrating **Aspect-Oriented Programming** with **Dynamic Proxy** and **Annotation-driven** design in Android and JVM environments.  
> It separates cross-cutting concerns cleanly, enhances maintainability, and increases testability.  
> This project is a strong demonstration of advanced software design principles, Kotlin Reflection mastery, and modern Android development practice.

---
