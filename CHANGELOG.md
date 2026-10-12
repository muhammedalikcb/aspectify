# Changelog

## 1.1.0

- `suspend` methods now run as real coroutines. The proxy no longer wraps them in `runBlocking`, so the calling thread is not blocked.
- For `suspend` methods, `@Timeout` uses `withTimeout` and throws `TimeoutCancellationException`.
- Cancelling the caller also cancels the target. Cancellation and timeouts are never retried by `@Retryable`.
- Exceptions from `suspend` methods keep their original type instead of being wrapped in `AspectifyException`.
- `@Cacheable` keys for `suspend` methods ignore the internal `Continuation` argument, so caching works as expected.
- The cache, debounce and rate limit state is now thread safe, since suspend calls can resume on another thread.
- Regular (non suspend) methods behave exactly as before.

## 1.0.0

First release on Maven Central (`io.github.muhammedalikcb`).

- The library is split into three modules: `aspectify-core` (pure Kotlin/JVM), `aspectify-android` and `aspectify-okhttp`.
- `aspectify-core` no longer depends on Android. Main thread and role checks are configurable through `Aspectify.mainThreadChecker` and `Aspectify.roleChecker`.
- `AspectifyAndroid.install(context)` wires up the Android behaviour (`Looper` based main thread check, `UserSession` roles).
- `AspectifyOkHttpInterceptor` takes an optional `AspectifyLogger` instead of always printing to the console.
- `@Authenticated` now fails by default until a role checker is configured.
