# App-ToolsBox | جعبه ابزار همه‌کاره

`App-ToolsBox` یک جعبه‌ابزار فارسی، ماژولار و Offline-first برای Android است. نسخه `2.0.0` با **111 ابزار اصلی** مسیر توسعه تعریف‌شده تا v2 را در یک کدبیس پایدار و Update-friendly جمع می‌کند.

## وضعیت نسخه
- Version: `2.0.0`
- versionCode: `11`
- Main tools: `111`
- applicationId: `com.asteam.toolbox`
- Kotlin: `2.3.21`
- Jetpack Compose + Material 3
- minSdk: `26` / targetSdk: `36` / compileSdk: `37`
- JDK: `17` / AGP: `9.3.1` / Gradle: `9.5.0`

## ابزارهای اندازه‌گیری، سنسور و دوربین
قطب‌نما، تراز، نورسنج، نمودار زنده نور، میدان مغناطیسی، فشارسنج، ارتفاع‌سنج پایه و کالیبره، شتاب‌سنج، ژیروسکوپ، چراغ‌قوه، ذره‌بین، آینه، خط‌کش، نقاله، زاویه‌سنج، لرزش‌سنج، داشبورد GPS، مسافت‌سنج GPS، جهت GPS و صدا‌سنج نسبی dBFS.

## محاسبات
ماشین‌حساب علمی، درصد، تخفیف، مالیات، سود، وام/قسط، BMI، BMR، تقسیم صورتحساب، نسبت، سود مرکب، تغییر درصد، مالیات معکوس، تخفیف چندمرحله‌ای، حقوق خالص، اضافه‌کاری، سن دقیق پیشرفته، روزهای کاری، مساحت/حجم اشکال، فیثاغورس، معادله درجه‌دو و ب.م.م/ک.م.م.

## تبدیل واحد
طول، جرم، دما، مساحت، حجم، سرعت، زمان، حجم داده، فشار، انرژی و زاویه.

## زمان، تقویم و یادآوری
کرنومتر، تایمر، سن و اختلاف تاریخ میلادی، امروز شمسی، تبدیل دوطرفه شمسی/میلادی، اختلاف تاریخ شمسی، روز هفته، تقویم ماهانه شمسی، مناسبت‌های ثابت خورشیدی، بررسی تعطیلی، Unix Time و یادآور محلی با اعلان Android.

## دیجیتال، QR، متن و توسعه
QR Generator حرفه‌ای شامل URL-to-QR، Wi-Fi، vCard، تلفن، SMS و Email؛ QR/Barcode Scanner زنده و از گالری؛ تاریخچه اسکن؛ Base64، Hash Suite، UUID، JSON Formatter، URL/HTML Codec، Case Converter، تبدیل ارقام، مرتب‌سازی/حذف تکراری خطوط، مقایسه متن، Slug، اعداد رومی، معکوس متن، شماره‌گذاری خطوط، رمزساز، شمارنده و ابزارهای تصادفی.

## سیستم و شبکه
باتری، فضای ذخیره‌سازی، مشخصات دستگاه، سنسورها، وضعیت اتصال، IP محلی و عمومی، DNS Lookup، Ping/Reachability، Port Test، Wi-Fi Info، WHOIS، سرعت لحظه‌ای شبکه و TrafficStats مصرف داده.

## QR و Scanner
- CameraX + bundled ML Kit؛ پردازش QR/Barcode روی دستگاه.
- اسکن زنده یا از گالری، فلش، کادر راهنما، توقف خودکار پس از نتیجه و «اسکن مجدد».
- ویبره/صدای کوتاه، تشخیص نوع محتوا و اکشن هوشمند.
- تاریخچه محلی تا 100 نتیجه.
- ذخیره PNG و Share امن تصویر QR با FileProvider.

## شخصی‌سازی
- Theme: System / Light / Dark.
- Accent Color: آبی، سبز، نارنجی، بنفش.
- Home Layout: Grid / List.
- Sort: پیش‌فرض، نام، اخیراً استفاده‌شده.
- Card Size: فشرده، معمولی، بزرگ.
- علاقه‌مندی‌ها، «مجموعه من»، ابزارهای اخیر و مخفی‌سازی ابزارها.
- پروفایل محلی با نام و تصویر.

## Backup / Restore
Storage Access Framework یک فایل JSON نسخه‌دار از تنظیمات، Accent، Layout، Sort، Card Size، علاقه‌مندی‌ها، مجموعه من، ابزارهای مخفی/اخیر، شمارنده و تاریخچه اسکن می‌سازد یا بازیابی می‌کند. URI تصویر پروفایل به دلیل وابستگی به دستگاه در Backup قابل‌انتقال ذخیره نمی‌شود.

## Android integration
- Home-screen Widget بدون polling پس‌زمینه.
- Quick Settings Tile چراغ‌قوه.
- Local Reminder با AlarmManager غیر Exact؛ نیازی به special exact-alarm access ندارد.
- Back navigation داخلی قبل از خروج.

## مجوزها و حریم خصوصی
مجوز Camera، Location، Microphone و Notification فقط هنگام نیاز قابلیت مرتبط استفاده/درخواست می‌شود. صدا‌سنج فایل صوتی ذخیره نمی‌کند. GPS listener با خروج از ابزار حذف می‌شود. ابزار Public IP یک درخواست HTTPS کوتاه به `api.ipify.org` می‌فرستد و WHOIS از TCP port 43 استفاده می‌کند؛ سایر بخش‌های اصلی مستقل از سرویس ابری هستند.

تقویم مناسبت‌ها فقط تعطیلات/مناسبت‌های ثابت خورشیدی را محلی نگه می‌دارد؛ تاریخ‌های قمری متغیر بدون منبع رسمی همان سال به‌صورت حدسی نمایش داده نمی‌شوند.

## Update-friendly و Signing
- applicationId ثابت است.
- versionCode با انتشار افزایش می‌یابد.
- کلیدهای SharedPreferences قبلی حفظ یا سازگار وارد می‌شوند.
- کلید Production Signing و رمزها هرگز نباید در GitHub عمومی Commit شوند.
- حذف برنامه یا پاک‌کردن App Data تنها حالت عادی حذف داده‌های محلی است.

## Build
```bash
./gradlew test
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

GitHub Actions Unit Test، Debug APK، Release APK و Source Snapshot را تولید می‌کند.

## تاریخچه اصلی
- `v1.0.0`: هسته 49 ابزاری، فارسی RTL، Drawer، جستجو، علاقه‌مندی و CI.
- `v1.1.0`: CameraX، Scanner، QR حرفه‌ای، ذره‌بین و آینه.
- `v1.2.0`: اندازه‌گیری حرفه‌ای، GPS و صدا.
- Roadmap `v1.3–v1.9`: محاسبات پیشرفته، متن/Dev، شبکه، شمسی، شخصی‌سازی، Widget/Quick Tile و Backup/Restore.
- `v2.0.0`: ادغام نهایی Roadmap با 111 ابزار اصلی.

جزئیات کامل در `CHANGELOG.md`، `PROJECT_INFO.md` و `distribution/release-notes-fa.md` ثبت می‌شود.
