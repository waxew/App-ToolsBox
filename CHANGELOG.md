# Changelog

تمام تغییرات قابل‌توجه پروژه در این فایل ثبت می‌شوند. هر Release باید قبل از نهایی‌شدن در README، CHANGELOG و Release Notes مستند شود.

## 2.0.1 - 2026-08-29

### Added
- مرکز تست سخت‌افزار داخل تنظیمات برای بررسی GPS، میکروفون، سنسورها، دوربین، QR/Barcode Scanner، Home Widget، Quick Settings Tile و Reminder.
- امکان درخواست Runtime Permission مرتبط از همان مرکز تست.
- امکان Pin کردن Widget روی لانچرهای سازگار و درخواست افزودن Tile چراغ‌قوه در Android 13+.
- نمایش کامل لیست ابزارهای مخفی در تنظیمات با امکان Restore تکی یا Restore همه.

### Changed
- نسخه به `2.0.1 / versionCode 12` ارتقا یافت.
- اکشن «سیو/نشانک/مجموعه من» از کارت ابزارها حذف شد؛ فقط «قلب» و «مخفی» باقی ماند.
- دکمه‌های قلب و مخفی به‌صورت عمودی در کنار کارت قرار گرفتند.
- همه کارت‌های ابزار در هر اندازه انتخابی ارتفاع یکسان دارند.
- کارت‌ها با Badge نماد ابزار، گوشه‌های گرد، Border، Elevation و محدودیت خطوط عنوان/توضیح بازطراحی شدند.
- صفحه Home دارای Header گرافیکی رنگی و Search با فرم نرم‌تر شد.
- فیلتر «مجموعه من» از Home حذف شد؛ Favorites همچنان از بخش علاقه‌مندی‌ها مدیریت می‌شود.

### Notes
- قابلیت‌های سخت‌افزاری از API واقعی Android خوانده می‌شوند و نتیجه جعلی تولید نمی‌شود.
- وجود سخت‌افزار، فعال بودن سرویس‌ها و Runtime Permissionها ممکن است بین گوشی‌ها متفاوت باشد.

## 2.0.0 - 2026-08-29

### Added — Measurement
- نمودار زنده 60 نمونه نور محیط.
- جهت GPS با Bearing و سرعت حرکت.
- ارتفاع‌سنج قابل کالیبراسیون با فشار مرجع سطح دریا.
- حفظ ابزارهای v1.2: خط‌کش، نقاله، زاویه‌سنج، لرزش‌سنج، GPS Dashboard، Distance Tracker و dBFS Sound Meter.

### Added — Advanced calculations
- سود مرکب، تغییر درصد، مالیات معکوس و تخفیف چندمرحله‌ای.
- محاسبه عمومی حقوق خالص و اضافه‌کاری با ورودی‌های قابل تنظیم.
- سن دقیق پیشرفته، روزهای کاری، مساحت و حجم اشکال، فیثاغورس، معادله درجه‌دو و GCD/LCM.

### Added — Text / Developer
- تبدیل ارقام فارسی/عربی/انگلیسی.
- مرتب‌سازی خطوط و حذف خطوط تکراری.
- Case Converter: lower, upper, title, camel, snake, kebab.
- JSON Formatter pretty/compact.
- URL و HTML encode/decode.
- UUID v4.
- Hash Suite: MD5، SHA-1، SHA-256، SHA-512.
- مقایسه متن، Roman Number، Slug، Reverse Text و Line Numbering.

### Added — Network
- وضعیت اتصال و Transport فعال.
- Local IPv4/IPv6 و Public IP.
- DNS Lookup و Reachability/Ping.
- TCP Port Test با timeout.
- Wi-Fi signal/link speed/frequency.
- WHOIS مستقیم از IANA روی TCP/43.
- سرعت لحظه‌ای دانلود/آپلود بر پایه TrafficStats کل دستگاه.
- مصرف داده بر پایه TrafficStats از زمان Boot.

### Added — Persian calendar / reminders
- امروز شمسی.
- تبدیل دوطرفه Gregorian/Jalali.
- اختلاف تاریخ شمسی و Weekday finder.
- تقویم ماهانه جلالی با هفته شنبه تا جمعه.
- فهرست مناسبت‌های ثابت خورشیدی.
- بررسی جمعه و تعطیلات ثابت خورشیدی.
- Local Reminder با AlarmManager غیر Exact و اعلان محلی.

### Added — Personalization
- Theme: system / light / dark.
- Accent colors: blue / green / orange / purple.
- Home layout: grid / list.
- Sort: catalog / title / recent.
- Card size: compact / normal / large.
- ابزارهای اخیر.
- مخفی‌سازی ابزار و Restore همه ابزارهای مخفی.
- «مجموعه من» مستقل از Favorites با فیلتر مستقیم خانه.

### Added — Backup / Android integration
- Backup JSON schema v2 با Import سازگار schema v1.
- Backup شامل profile name، counter، theme، accent، layout، sort، card size، favorites، custom collection، hidden/recent tools و scan history.
- Home-screen AppWidget بدون polling پس‌زمینه.
- Quick Settings Tile برای flashlight.
- Notification receiver داخلی برای Reminder.

### Changed
- نسخه به `2.0.0 / versionCode 11` ارتقا یافت.
- Catalog به **111 ابزار اصلی** رسید.
- `ToolRouter` به dispatch ماژولار برای تمام خانواده‌های جدید توسعه یافت.
- `UserPreferences` با حفظ کلیدهای قبلی برای شخصی‌سازی و Backup schema v2 توسعه یافت.
- Theme و Accent در زمان اجرا بدون نیاز به restart کامل Activity از state Compose اعمال می‌شوند.
- HomeScreen از Grid/List، sort، card sizes، favorites، custom collection و hide action پشتیبانی می‌کند.
- QR Generator به‌طور مستقیم URL-to-QR را نیز پوشش می‌دهد.

### Privacy / Permissions
- `POST_NOTIFICATIONS` برای Android 13+ Reminder اضافه شد و Runtime request فقط داخل ابزار مربوط انجام می‌شود.
- Camera، Location و Microphone همچنان فقط برای قابلیت مرتبط درخواست/استفاده می‌شوند.
- Reminder از exact-alarm special access استفاده نمی‌کند.
- Sound Meter فایل صوتی ذخیره نمی‌کند.
- Profile image URI در Backup portable ذخیره نمی‌شود.
- Public IP یک HTTPS request کوتاه به `api.ipify.org` دارد؛ WHOIS ممکن است به دلیل مسدودبودن TCP/43 در بعضی شبکه‌ها در دسترس نباشد.
- تعطیلات قمری متغیر بدون منبع رسمی سالانه به‌صورت حدسی در تقویم درج نمی‌شوند.

### Documentation
- README، PROJECT_INFO، CHANGELOG، Release Notes و `distribution/version.json` برای 2.0.0 / 111 tools همگام شدند.

## 1.2.0 - 2026-08-29

### Added
- خط‌کش مبتنی بر DPI نمایشگر.
- نقاله 0..180 درجه.
- زاویه‌سنج Pitch/Roll.
- لرزش‌سنج نسبی و Peak.
- GPS dashboard برای speed/coordinates/altitude/accuracy.
- GPS distance tracker با حذف jitter کوچک و پرش‌های غیرمنطقی.
- Sound Meter با PCM/RMS و dBFS.

### Changed / Privacy
- نسخه `1.2.0 / versionCode 3`.
- Runtime Location فقط برای GPS و Runtime Microphone فقط برای Sound Meter.
- GPS listener هنگام خروج حذف می‌شود و هیچ فایل صوتی ذخیره نمی‌شود.
- محدودیت‌های دقت DPI، GPS و dBFS در UI مستند شدند.

## 1.1.0 - 2026-08-29

### Added
- QR Scanner و Barcode Scanner با CameraX و bundled ML Kit.
- اسکن از Gallery، scan guide، flash control، content detection و smart actions.
- توقف خودکار بعد از اولین تشخیص، «اسکن مجدد»، beep/vibration و share result.
- تاریخچه محلی تا 100 نتیجه با حذف تکی/کامل.
- Magnifier و Mirror.
- QR Generator حرفه‌ای برای text/link، Wi-Fi، vCard، phone، SMS و email.
- ذخیره QR PNG، FileProvider share و payload copy/share.

### Changed / Privacy
- نسخه `1.1.0 / versionCode 2` و تعداد ابزارها 54.
- Scanner در pause پردازش frame را متوقف می‌کند.
- QR/Barcode روی دستگاه پردازش می‌شوند.
- Legacy write storage فقط maxSdk 28 برای QR PNG.

## 1.0.0 - 2026-08-28

### Added
- پروژه Kotlin + Jetpack Compose با Material 3 و RTL فارسی.
- 49 ابزار پایه در measurement، calculation، conversion، time/date، digital و system.
- Search، category filter، adaptive grid، Favorites و Drawer سمت راست.
- پروفایل محلی، Settings، About و Contact.
- SharedPreferences برای داده‌های محلی.
- Release signing configuration بدون Commit کلید خصوصی.
- Expression Evaluator unit test.
- GitHub Actions برای tests، debug APK، release APK و source snapshot.

### Changed
- Back navigation داخلی قبل از خروج.
- About بدون نمایش Package Name.
- applicationId ثابت و ساختار Update-friendly برای حفظ داده در آپدیت عادی.
