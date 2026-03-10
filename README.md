# Couplebase

Wedding planning & marriage life companion app for couples.

## About

Couplebase helps couples plan their wedding and build their life together — checklists, budgets, guest lists, shared finances, daily check-ins, and more. Everything syncs in real-time between partners, works offline, and runs on Android, iOS, and Web.

## Tech Stack

- **Kotlin Multiplatform** + **Compose Multiplatform** (Android, iOS, Web)
- **Supabase** (Auth, PostgreSQL, Realtime, Storage)
- **SQLDelight** (offline-first local database)
- **Decompose** (navigation & lifecycle)
- **Koin** (dependency injection)
- **Ktor** (networking)

## Project Structure

```
couplebase/
├── build-logic/         # Gradle convention plugins
├── core/                # Shared infrastructure modules
├── feature/             # Feature modules (auth, wedding, finance, etc.)
├── composeApp/          # Platform entry points (Android, iOS, Web)
└── gradle/              # Version catalog
```

## License

Proprietary. All rights reserved.
