# Changelog

## 1.0.0

First release on Maven Central (`io.github.muhammedalikcb`).

- The library is split into three modules: `aspectify-core` (pure Kotlin/JVM), `aspectify-android` and `aspectify-okhttp`.
- `aspectify-core` no longer depends on Android. Main thread and role checks are configurable through `Aspectify.mainThreadChecker` and `Aspectify.roleChecker`.
- `AspectifyAndroid.install(context)` wires up the Android behaviour (`Looper` based main thread check, `UserSession` roles).
- `AspectifyOkHttpInterceptor` takes an optional `AspectifyLogger` instead of always printing to the console.
- `@Authenticated` now fails by default until a role checker is configured.
