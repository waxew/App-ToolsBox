# Release signing

کلید Release هویت دائمی نسخه‌های منتشرشده برنامه است. اگر کلید Production افشا شود، امنیت زنجیره انتشار به خطر می‌افتد؛ اگر گم شود، انتشار به‌روزرسانی با همان هویت مشکل‌ساز می‌شود.

## فایل‌های خصوصی

این فایل‌ها نباید Commit شوند:

- `signing/App-ToolsBox-release.jks`
- `keystore.properties`
- `signing/KEY_INFO.txt`

`.gitignore` این موارد را مسدود می‌کند.

## تولید کلید جدید

```bash
./signing/generate-release-key.sh
```

فقط قبل از اولین انتشار عمومی درباره کلید نهایی تصمیم بگیرید. پس از انتشار، همان کلید/هویت امضا برای نسخه‌های بعدی حفظ شود.

## Fingerprint

```bash
keytool -list -v -keystore signing/App-ToolsBox-release.jks -alias app-toolsbox
```

از کلید و رمزها حداقل دو نسخه پشتیبان رمزگذاری‌شده و جدا از سیستم توسعه نگهداری کنید.
