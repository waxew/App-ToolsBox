package com.asteam.toolbox.data

/** Logical groups shown on the home page. Stable ids keep favorites compatible across updates. */
enum class ToolCategory(val title: String) {
    MEASUREMENT("اندازه‌گیری"),
    CALCULATION("محاسبات"),
    CONVERSION("تبدیل واحد"),
    TIME_DATE("زمان و تاریخ"),
    DIGITAL("دیجیتال"),
    SYSTEM("سیستم"),
}

data class ToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ToolCategory,
    val symbol: String,
)

object ToolCatalog {
    val tools = listOf(
        ToolItem("compass", "قطب‌نما", "جهت‌یابی با سنسورهای دستگاه", ToolCategory.MEASUREMENT, "◉"),
        ToolItem("level", "تراز", "اندازه‌گیری شیب سطح", ToolCategory.MEASUREMENT, "⊖"),
        ToolItem("light", "نورسنج", "شدت نور محیط بر حسب lux", ToolCategory.MEASUREMENT, "☀"),
        ToolItem("magnetic", "میدان مغناطیسی", "نمایش شدت میدان µT", ToolCategory.MEASUREMENT, "⌁"),
        ToolItem("pressure", "فشارسنج", "فشار هوا با سنسور دستگاه", ToolCategory.MEASUREMENT, "▤"),
        ToolItem("altitude", "ارتفاع‌سنج", "برآورد ارتفاع از فشار هوا", ToolCategory.MEASUREMENT, "△"),
        ToolItem("accelerometer", "شتاب‌سنج", "محورهای X، Y و Z", ToolCategory.MEASUREMENT, "↗"),
        ToolItem("gyroscope", "ژیروسکوپ", "سرعت زاویه‌ای دستگاه", ToolCategory.MEASUREMENT, "⟳"),
        ToolItem("flashlight", "چراغ‌قوه", "کنترل فلش دوربین", ToolCategory.MEASUREMENT, "✦"),
        ToolItem("magnifier", "ذره‌بین", "نمای زنده دوربین با بزرگ‌نمایی", ToolCategory.MEASUREMENT, "⌕"),
        ToolItem("mirror", "آینه", "نمای زنده دوربین جلویی", ToolCategory.MEASUREMENT, "◫"),
        ToolItem("ruler", "خط‌کش", "اندازه‌گیری تقریبی روی نمایشگر", ToolCategory.MEASUREMENT, "▥"),
        ToolItem("protractor", "نقاله", "نمایش و مقایسه زاویه ۰ تا ۱۸۰ درجه", ToolCategory.MEASUREMENT, "∠"),
        ToolItem("angle_meter", "زاویه‌سنج", "Pitch و Roll زنده با شتاب‌سنج", ToolCategory.MEASUREMENT, "⌁°"),
        ToolItem("vibrometer", "لرزش‌سنج", "شدت لرزش نسبی با شتاب‌سنج", ToolCategory.MEASUREMENT, "≋"),
        ToolItem("gps_dashboard", "داشبورد GPS", "سرعت، مختصات، ارتفاع و دقت", ToolCategory.MEASUREMENT, "GPS"),
        ToolItem("distance_tracker", "مسافت‌سنج GPS", "جمع مسافت حرکت با نقاط GPS", ToolCategory.MEASUREMENT, "↔"),
        ToolItem("sound_meter", "صدا‌سنج", "شدت نسبی صدا به‌صورت dBFS", ToolCategory.MEASUREMENT, "dB"),

        ToolItem("calculator", "ماشین‌حساب علمی", "عبارت، توان، ریشه و توابع مثلثاتی", ToolCategory.CALCULATION, "ƒx"),
        ToolItem("percentage", "درصد", "درصد یک عدد و نسبت دو عدد", ToolCategory.CALCULATION, "%"),
        ToolItem("discount", "تخفیف", "قیمت نهایی و مقدار تخفیف", ToolCategory.CALCULATION, "٪"),
        ToolItem("tax", "مالیات", "محاسبه مالیات و جمع نهایی", ToolCategory.CALCULATION, "+%"),
        ToolItem("profit", "سود", "سود مبلغی و درصد سود", ToolCategory.CALCULATION, "↗"),
        ToolItem("loan", "وام و قسط", "قسط ماهانه و کل بازپرداخت", ToolCategory.CALCULATION, "₮"),
        ToolItem("bmi", "BMI", "شاخص توده بدنی", ToolCategory.CALCULATION, "BMI"),
        ToolItem("bmr", "BMR", "برآورد انرژی پایه روزانه", ToolCategory.CALCULATION, "kcal"),
        ToolItem("split", "تقسیم صورتحساب", "تقسیم مبلغ و انعام بین افراد", ToolCategory.CALCULATION, "÷"),
        ToolItem("ratio", "نسبت", "ساده‌سازی نسبت دو مقدار", ToolCategory.CALCULATION, ":"),

        ToolItem("length", "طول", "mm، cm، m، km، inch، ft، mile", ToolCategory.CONVERSION, "↔"),
        ToolItem("mass", "جرم", "mg، g، kg، oz، lb", ToolCategory.CONVERSION, "kg"),
        ToolItem("temperature", "دما", "سلسیوس، فارنهایت، کلوین", ToolCategory.CONVERSION, "°"),
        ToolItem("area", "مساحت", "m²، km²، hectare، ft²", ToolCategory.CONVERSION, "□"),
        ToolItem("volume", "حجم", "ml، L، m³، cup، gallon", ToolCategory.CONVERSION, "▱"),
        ToolItem("speed", "سرعت", "m/s، km/h، mph، knot", ToolCategory.CONVERSION, "≫"),
        ToolItem("time", "زمان", "ثانیه، دقیقه، ساعت، روز", ToolCategory.CONVERSION, "⌛"),
        ToolItem("data", "حجم داده", "B، KB، MB، GB، TB", ToolCategory.CONVERSION, "GB"),
        ToolItem("pressure_convert", "فشار", "Pa، kPa، bar، psi، atm", ToolCategory.CONVERSION, "Pa"),
        ToolItem("energy", "انرژی", "J، kJ، cal، kcal، Wh", ToolCategory.CONVERSION, "E"),
        ToolItem("angle", "زاویه", "درجه و رادیان", ToolCategory.CONVERSION, "∠"),

        ToolItem("stopwatch", "کرنومتر", "اندازه‌گیری زمان سپری‌شده", ToolCategory.TIME_DATE, "00"),
        ToolItem("timer", "تایمر", "شمارش معکوس ساده", ToolCategory.TIME_DATE, "⌛"),
        ToolItem("age", "محاسبه سن", "سن دقیق بر اساس تاریخ تولد", ToolCategory.TIME_DATE, "🎂"),
        ToolItem("date_diff", "اختلاف تاریخ", "فاصله بین دو تاریخ میلادی", ToolCategory.TIME_DATE, "↔"),

        ToolItem("random", "عدد تصادفی", "انتخاب عدد در بازه دلخواه", ToolCategory.DIGITAL, "#"),
        ToolItem("dice", "تاس", "تاس ۶ وجهی", ToolCategory.DIGITAL, "⚄"),
        ToolItem("coin", "شیر یا خط", "انتخاب تصادفی دوحالته", ToolCategory.DIGITAL, "◐"),
        ToolItem("password", "رمزساز", "رمز قوی با طول دلخواه", ToolCategory.DIGITAL, "***"),
        ToolItem("base64", "Base64", "کدگذاری و رمزگشایی متن", ToolCategory.DIGITAL, "64"),
        ToolItem("sha256", "SHA-256", "هش استاندارد متن", ToolCategory.DIGITAL, "SHA"),
        ToolItem("text_stats", "شمارش متن", "کاراکتر، کلمه و خط", ToolCategory.DIGITAL, "ABC"),
        ToolItem("number_base", "مبنای عدد", "دودویی، دهدهی و شانزدهی", ToolCategory.DIGITAL, "01"),
        ToolItem("clipboard", "کلیپ‌بورد", "مشاهده و کپی متن", ToolCategory.DIGITAL, "▣"),
        ToolItem("counter", "شمارنده", "شمارنده دائمی ساده", ToolCategory.DIGITAL, "+1"),
        ToolItem("qr", "QR ساز", "ساخت QR حرفه‌ای و ذخیره/اشتراک", ToolCategory.DIGITAL, "▦"),
        ToolItem("qr_scanner", "اسکن QR", "اسکن زنده QR با دوربین", ToolCategory.DIGITAL, "⌗"),
        ToolItem("barcode_scanner", "اسکن بارکد", "خواندن بارکدهای رایج با دوربین", ToolCategory.DIGITAL, "▥"),
        ToolItem("scan_history", "تاریخچه اسکن", "مشاهده و کپی اسکن‌های اخیر", ToolCategory.DIGITAL, "≡"),

        ToolItem("battery", "باتری", "شارژ، وضعیت و دما", ToolCategory.SYSTEM, "⚡"),
        ToolItem("storage", "فضای ذخیره‌سازی", "کل، مصرف‌شده و آزاد", ToolCategory.SYSTEM, "▰"),
        ToolItem("device", "مشخصات دستگاه", "مدل، Android و ABI", ToolCategory.SYSTEM, "▣"),
        ToolItem("sensors", "فهرست سنسورها", "سنسورهای موجود روی دستگاه", ToolCategory.SYSTEM, "≋"),
    )
}
