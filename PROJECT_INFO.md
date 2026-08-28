# PROJECT_INFO

## Identity

- Repository: `waxew/App-ToolsBox`
- Application name: `جعبه ابزار`
- Application ID: `com.asteam.toolbox`
- Release alias: `app-toolsbox`
- Version: `2.0.0 (11)`
- Main tool count: `101`
- Development group: `as Team`
- Support: `AS.Support.info@gmail.com`

## Android baseline

- Kotlin: `2.3.21`
- Jetpack Compose BOM: `2026.08.00`
- Android Gradle Plugin: `9.3.1`
- Gradle: `9.5.0`
- JDK: `17`
- compileSdk: `37`
- targetSdk: `36`
- minSdk: `26`

## Architecture

Single-activity Compose application with stable tool IDs and feature-oriented modules. `ToolCatalog` is the authoritative catalog; `ToolRouter` routes each tool family to its implementation module.

Major modules now include:

- base calculations, converters, date/time and system tools
- CameraX + bundled ML Kit scanner
- professional QR generator
- professional measurement/GPS/audio tools
- advanced calculations
- text/developer utilities
- network diagnostics
- Persian/Jalali date utilities
- personalization preferences
- JSON backup/restore
- home-screen widget
- Quick Settings flashlight tile

## Local data

`UserPreferences` stores profile data, favorites, persistent counter, scanner history, theme mode, home layout, hidden-tool IDs and recent-tool IDs. Stable keys preserve compatibility during normal updates.

Backup format is a versioned JSON envelope. Profile image URI is intentionally excluded from portable backup because document-provider URIs are device-specific.

## Permissions

- `CAMERA`: requested at runtime by camera/scanner/flashlight tools.
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: requested only by GPS tools.
- `RECORD_AUDIO`: requested only by the relative sound meter; no audio file is saved.
- `INTERNET` / network-state permissions: used by network diagnostics; most app features remain offline-first.
- `VIBRATE`: short local scanner feedback.
- legacy storage permission is capped at Android 9 for QR PNG saving.

## Update compatibility rules

1. Never change `applicationId` after public release.
2. Increase `versionCode` for every public build.
3. Keep existing preference keys stable or migrate them explicitly.
4. Never overwrite user data during a normal update.
5. Future production updates must use the same release signing identity.
6. Database/schema changes must have explicit migrations before release.
7. Public GitHub must never contain the private `.jks`, passwords or real `keystore.properties`.

## Release checklist

1. Run unit tests.
2. Build debug APK.
3. Build release APK/AAB.
4. Test Back navigation and runtime permission gates.
5. Test QR scanner/generator, GPS, sound meter, widget and Quick Settings tile on supported hardware.
6. Verify signing certificate for production release.
7. Update README, CHANGELOG, release notes and `distribution/version.json`.
8. Produce owner-private source package with signing material only outside public Git.
