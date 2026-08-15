# WarmWord - Android AI Mental Health Companion
# Build instructions and conventions

## Build Commands

```bash
# Assemble debug APK
./gradlew assembleDebug

# Run lint
./gradlew lint

# Run unit tests
./gradlew test

# Build release
./gradlew assembleRelease
```

## API Keys

Set the following properties in `local.properties` or pass via command line:

```
-PLACES_API_KEY=YOUR_GOOGLE_PLACES_API_KEY
-DGOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY  
```

Or add to `gradle.properties`:
```
PLACES_API_KEY=your_key_here
GOOGLE_MAPS_API_KEY=your_key_here
```

## Key Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| AGP | 8.13.0 | Android Gradle Plugin |
| Kotlin | 2.2.0 | Language |
| LiteRT-LM | 0.16.0 | On-device AI inference (com.google.ai.edge.litertlm:litertlm-android) |
| Compose BOM | 2026.02.00 | UI framework |
| Hilt | 2.58 | DI |
| Room | (via Hilt) | Local database |
| Play Services Maps | 19.1.0 | Maps UI |
| Play Services Location | 21.6.0 | Location services |
| WorkManager | 2.10.0 | Background model download |

## Architecture

Follows clean architecture pattern:
- `data/` - Data sources, local DB (Room), repositories, domain models
- `domain/` - Repository interfaces, use cases
- `ai/` - LiteRT-LM manager, model download worker, system prompt
- `ui/` - Compose screens, navigation, theme, components
- `util/` - Extensions, constants, disclaimers
- `di/` - Hilt modules

## Testing

- Unit tests: `app/src/test/java/com/warmword/app/`
- Instrumentation tests: `app/src/androidTest/java/com/warmword/app/`

Run: `./gradlew testDebugUnitTest`
