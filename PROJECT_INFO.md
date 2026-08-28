# PROJECT_INFO

## Identity

- Repository: `waxew/App-ToolsBox`
- Application name: `جعبه ابزار`
- Application ID: `com.asteam.toolbox`
- Release alias: `app-toolsbox`
- Version: `1.2.0 (3)`
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

Single-activity Compose app. Tools use stable ids in `ToolCatalog` and are routed to feature families under `tools/`. Profile, favorites, counter and scan history are stored locally by `UserPreferences`.

### v1.2 measurement modules
- display-based ruler and interactive protractor
- live accelerometer angle meter
- vibration meter
- GPS dashboard and distance tracker
- relative on-device sound meter (dBFS)

Location and microphone permissions are runtime-gated and only required when those specific tools are opened.

## Update compatibility rules

1. Do not change `applicationId` after public release.
2. Increase `versionCode` for every public build.
3. Keep existing preference keys stable or migrate them.
4. Never overwrite user data during a normal update.
5. Back up the Production signing key; future updates must use the same signing identity.
6. Add explicit migrations before changing any future database schema.

## Release checklist

1. Run unit tests.
2. Build and test the debug APK.
3. Test navigation/back behavior and permission-gated tools.
4. Build the signed release APK/AAB with the owner keystore.
5. Verify certificate fingerprints.
6. Update README, CHANGELOG, release notes and `distribution/version.json`.
7. Keep `.jks`, passwords and `keystore.properties` private.
