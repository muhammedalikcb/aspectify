# Aspectify

[![Maven Central](https://img.shields.io/maven-central/v/io.github.muhammedalikcb/aspectify-core)](https://central.sonatype.com/artifact/io.github.muhammedalikcb/aspectify-core)
[![CI](https://github.com/muhammedalikcb/aspectify/actions/workflows/ci.yml/badge.svg)](https://github.com/muhammedalikcb/aspectify/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
[![Last Commit](https://img.shields.io/github/last-commit/muhammedalikcb/aspectify)](https://github.com/muhammedalikcb/aspectify/commits/main)
[![Stars](https://img.shields.io/github/stars/muhammedalikcb/aspectify?style=social)](https://github.com/muhammedalikcb/aspectify/stargazers)

A small aspect-oriented programming library for Kotlin and Android. You put annotations like `@Loggable`, `@Retryable` or `@Timeout` on an interface, wrap the implementation in a proxy, and Aspectify runs that logic around every call. No bytecode weaving, no Gradle plugin: it's a plain `java.lang.reflect.Proxy`.

I wrote it because logging, retries and permission checks kept getting copy pasted into every repository and use case. With Aspectify the business code stays about the business.

[Türkçe açıklama aşağıda.](#türkçe)

## Annotations

| Annotation | What it does |
| --- | --- |
| `@Loggable` | Logs when the method starts and finishes |
| `@Retryable(times)` | Runs the method again if it throws, up to `times` attempts |
| `@Timeout(millis)` | Fails the call if it takes longer than `millis` |
| `@Timed` | Logs how long the call took |
| `@Cacheable` | Returns the cached result for the same arguments |
| `@Debounce(millis)` | Ignores calls that come in faster than `millis` |
| `@RateLimit(limit, durationMs)` | Allows at most `limit` calls per window |
| `@Authenticated(role)` | Throws `AuthorizationFailureException` if the current user doesn't have `role` |
| `@BackgroundThread` | Throws if the method is called on the main thread |
| `@Transactional` | Logs start, commit and rollback around the call |

`suspend` methods are supported. They run as real coroutines without blocking the calling thread, cancellation reaches the target, and `@Timeout` uses `withTimeout`.

## Installation

Aspectify is on Maven Central as three modules:

| Module | Use it for |
| --- | --- |
| `aspectify-core` | Annotations, proxy and interceptor. Pure Kotlin/JVM, no Android dependency. |
| `aspectify-android` | `AndroidLogger`, `UserSession` and the main thread check. Brings in `aspectify-core`. |
| `aspectify-okhttp` | OkHttp interceptor that logs requests and how long they took. |

```kotlin
dependencies {
    implementation("io.github.muhammedalikcb:aspectify-android:1.1.0") // Android
    // implementation("io.github.muhammedalikcb:aspectify-core:1.1.0") // plain Kotlin/JVM
    // implementation("io.github.muhammedalikcb:aspectify-okhttp:1.1.0") // OkHttp
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

## Usage

Annotate the interface methods:

```kotlin
interface UserService {
    @Loggable
    @Retryable(times = 3)
    @Authenticated(role = "admin")
    fun deleteUser(userId: String)
}
```

Wrap the real implementation and call it through the proxy:

```kotlin
val service = proxyOf<UserService> {
    target = RealUserService()
    logger = ConsoleLogger // AndroidLogger on Android
}

service.deleteUser("user123")
```

## Requirements

- Kotlin 2.0+
- JVM 11+ (`aspectify-core`, `aspectify-okhttp`)
- Android minSdk 24 (`aspectify-android`)
- Kotlin Coroutines (comes in as a dependency)

## Good to know

- Annotations work on interface methods, because the proxy is a JDK dynamic proxy.
- `AndroidLogger` is for Android only. On the JVM use `ConsoleLogger` or your own `AspectifyLogger`.
- `UserSession` keeps the current user's roles in SharedPreferences.
- `@Debounce` and `@RateLimit` return `null` when they skip a call.

## Tests and CI

`InterceptorHandlerTest` covers retry, cache and rate limit. `SuspendInterceptorTest` covers suspend methods: no blocking, exception types, retry, timeout, cancellation and cache. Every push and pull request builds the project, runs the tests and publishes to the local Maven repository to make sure packaging still works. Pushing a `v*` tag signs the artifacts, publishes them to Maven Central and creates a GitHub release.

## Contributing

Issues and pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. See [LICENSE](LICENSE).

---

## Türkçe

Aspectify, Kotlin ve Android için küçük bir aspect-oriented programming kütüphanesi. Bir interface'in methodlarına `@Loggable`, `@Retryable` ya da `@Timeout` gibi anotasyonlar koyuyorsun, gerçek implementasyonu bir proxy ile sarıyorsun ve Aspectify bu mantığı her çağrının etrafında çalıştırıyor. Bytecode weaving ya da Gradle plugin yok, düz bir `java.lang.reflect.Proxy`.

Loglama, retry ve yetki kontrolleri her repository ve use case içine kopyalanıp duruyordu, bunun için yazdım. Aspectify ile iş kodu sadece işle ilgileniyor.

### Anotasyonlar

| Anotasyon | Ne yapar |
| --- | --- |
| `@Loggable` | Method başlarken ve biterken log atar |
| `@Retryable(times)` | Hata alırsa methodu en fazla `times` kez tekrar dener |
| `@Timeout(millis)` | Çağrı `millis` süresini aşarsa hata verir |
| `@Timed` | Çağrının ne kadar sürdüğünü loglar |
| `@Cacheable` | Aynı argümanlar için cache'teki sonucu döner |
| `@Debounce(millis)` | `millis` süresinden sık gelen çağrıları yok sayar |
| `@RateLimit(limit, durationMs)` | Belirli bir sürede en fazla `limit` çağrıya izin verir |
| `@Authenticated(role)` | Kullanıcıda `role` yoksa `AuthorizationFailureException` fırlatır |
| `@BackgroundThread` | Method ana thread'den çağrılırsa hata fırlatır |
| `@Transactional` | Çağrının başlangıç, commit ve rollback adımlarını loglar |

`suspend` methodlar destekleniyor. Çağıran thread'i bloklamadan gerçek coroutine olarak çalışıyorlar, iptal hedef methoda kadar ulaşıyor ve `@Timeout` için `withTimeout` kullanılıyor.

### Kurulum

Aspectify Maven Central'da üç modül olarak yayında:

| Modül | Ne için |
| --- | --- |
| `aspectify-core` | Anotasyonlar, proxy ve interceptor. Saf Kotlin/JVM, Android bağımlılığı yok. |
| `aspectify-android` | `AndroidLogger`, `UserSession` ve ana thread kontrolü. `aspectify-core`'u da getirir. |
| `aspectify-okhttp` | OkHttp istekleri için loglama ve süre ölçümü yapan interceptor. |

```kotlin
dependencies {
    implementation("io.github.muhammedalikcb:aspectify-android:1.1.0") // Android
    // implementation("io.github.muhammedalikcb:aspectify-core:1.1.0") // saf Kotlin/JVM
    // implementation("io.github.muhammedalikcb:aspectify-okhttp:1.1.0") // OkHttp
}
```

Android'de uygulama açılırken bir kez çağır, `@BackgroundThread` ve `@Authenticated` bu sayede çalışır:

```kotlin
AspectifyAndroid.install(applicationContext)
```

Saf JVM'de rol kontrolünü kendin bağla:

```kotlin
Aspectify.roleChecker = { role -> currentUser.roles.contains(role) }
```

### Kullanım

```kotlin
interface UserService {
    @Loggable
    @Retryable(times = 3)
    @Authenticated(role = "admin")
    fun deleteUser(userId: String)
}

val service = proxyOf<UserService> {
    target = RealUserService()
    logger = ConsoleLogger // Android'de AndroidLogger
}

service.deleteUser("user123")
```

### Bilmekte fayda var

- Proxy bir JDK dynamic proxy olduğu için anotasyonlar interface methodlarında çalışır.
- `AndroidLogger` sadece Android içindir. JVM'de `ConsoleLogger` ya da kendi `AspectifyLogger`'ını kullan.
- `UserSession` kullanıcının rollerini SharedPreferences'ta tutar.
- `@Debounce` ve `@RateLimit` bir çağrıyı atladığında `null` döner.

### Test ve CI

`InterceptorHandlerTest` retry, cache ve rate limit davranışını test ediyor. `SuspendInterceptorTest` ise suspend methodları test ediyor: bloklamama, hata tipleri, retry, timeout, iptal ve cache. Her push ve PR'da proje derlenir, testler çalışır ve paketlemenin bozulmadığını görmek için yerel Maven deposuna yayınlanır. `v*` etiketi gönderildiğinde artifact'ler imzalanıp Maven Central'a yüklenir ve bir GitHub release oluşturulur.

### Katkı

Issue ve PR'lara açığım. Ayrıntılar [CONTRIBUTING.md](CONTRIBUTING.md) dosyasında.

### Lisans

MIT. Ayrıntılar [LICENSE](LICENSE) dosyasında.
