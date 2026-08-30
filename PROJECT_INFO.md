# PROJECT_INFO

## Identity
- Repository: `waxew/App-ToolsBox`
- Application name: `جعبه ابزار`
- Application ID: `com.asteam.toolbox`
- Release alias: `app-toolsbox`
- Version: `2.1.0 (13)`
- Main tool count: `111`
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
Single-activity Compose application with stable tool IDs. `ToolCatalog` remains the authoritative 111-tool catalog and `ToolRouter` dispatches to feature-oriented modules.

Feature modules include base calculations/converters, CameraX scanner, advanced QR, professional measurement/GPS/audio, advanced calculations, text/developer tools, network diagnostics, Persian/Jalali calendar tools, local reminder, backup/restore, widget and Quick Settings tile.

`HardwareDiagnosticsTools.kt` is a management/diagnostics surface opened from Settings. It checks real Android hardware/permission state for GPS, microphone, sensors, camera/scanner, widget pinning, Quick Settings Tile and Reminder/notifications, then deep-links into the corresponding Toolbox feature.

## Home UI / personalization
- Grid/List, sort and card-size preferences remain persistent.
- Tool cards use fixed heights per selected size so neighboring cards stay aligned.
- Each card exposes only two vertical actions: Favorite (heart) and Hide.
- Bookmark/save/custom-collection UI and active storage logic are removed. Legacy customCollection data is discarded during upgrade/import.
- Settings shows the complete hidden-tool list with per-tool Restore and Restore-all.
- Home uses a graphical header, rounded search, icon badges, rounded cards, border and elevation.
- v2.1.0 coordinates Theme, Drawer and TopBar with the selected accent and warmer surface colors.

## Local data
`UserPreferences` keeps profile data, favorites, counter, scanner history, theme mode, accent color, home layout, sort mode, card size, hidden tools and recent tools. Existing supported keys are retained for update compatibility; removed features are migrated explicitly.

Backup schema version `3` is JSON-based and accepts legacy schemas `1` and `2`. Legacy customCollection data is ignored/removed. Profile image URI remains outside portable backup because provider URIs are device-specific.

## Permissions
- `CAMERA`: camera/scanner/flashlight tools.
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: GPS tools only.
- `RECORD_AUDIO`: relative dBFS sound meter; no audio file is saved.
- `POST_NOTIFICATIONS`: Android 13+ local reminder notification.
- `INTERNET`, network-state and Wi-Fi-state: network diagnostic tools.
- `VIBRATE`: short scanner feedback.
- legacy external-storage write is capped at Android 9 for QR PNG saving.

## Accuracy / data-source rules
- Display ruler accuracy depends on OEM DPI reporting.
- Sound meter reports relative `dBFS`, not calibrated `dB SPL`.
- GPS accuracy depends on device/environment.
- Fixed solar Persian occasions are local; moving lunar holidays are not guessed without authoritative year-specific data.
- Public IP uses `api.ipify.org`; WHOIS uses IANA TCP/43 and may be blocked by some networks.

## Android integrations
- Home-screen AppWidget has no background polling.
- Quick Settings Tile controls flashlight when camera permission is already granted.
- Reminder uses inexact `AlarmManager.setAndAllowWhileIdle`, avoiding exact-alarm special access.
- Hardware diagnostics can request relevant runtime permissions and launch supported integration setup flows.

## Update compatibility rules
1. Never change `applicationId` after public release.
2. Increase `versionCode` for each public build.
3. Keep supported preference keys stable or migrate them explicitly.
4. Never overwrite normal user data during update.
5. Production updates must use the same signing identity.
6. Database/schema changes require explicit migration.
7. Public GitHub must never contain private `.jks`, passwords or real `keystore.properties`.

## Release checklist
1. Run unit tests.
2. Build debug APK.
3. Build release APK/AAB.
4. Test navigation/back behavior and permission gates.
5. Test hardware diagnostics and scanner/QR, GPS, audio, network, Jalali calendar/reminder, widget and tile on supported real hardware.
6. Verify production signing certificate.
7. Update README, CHANGELOG, release notes and version metadata.
8. Keep owner-private signing bundle outside public Git.
