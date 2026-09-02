# Instagram Smart Connect

نسخه غیرتجاری و اختصاصی Android بر پایه Oblivion/Aether است.

## رفتار اختصاصی

- از Cloudflare WARP با هسته Aether استفاده می‌کند؛ Worker یا VPS لازم ندارد.
- در سطح `VpnService` فقط `com.instagram.android` با
  `addAllowedApplication()` وارد تونل می‌شود.
- سایر برنامه‌ها و خود کلاینت از اینترنت عادی استفاده می‌کنند.
- اگر Instagram رسمی نصب نباشد، رابط TUN ساخته نمی‌شود و اتصال با خطا متوقف
  می‌شود؛ بنابراین هیچ‌گاه ناخواسته کل گوشی وارد VPN نخواهد شد.

## ساخت APK

Workflow موجود در `.github/workflows/release.yaml` با اجرای دستی یا Tag نسخه،
APK عمومی و APKهای جداگانه ABI را تولید می‌کند. برای گوشی‌های جدید Samsung،
فایل `instagram-smart-connect-arm64-v8a.apk` مناسب است.

## مجوز

این مشتق باید فقط به‌صورت غیرتجاری و با حفظ انتساب و شرایط
CC BY-NC-SA 4.0 پروژه اصلی منتشر شود. مجوز وابستگی‌های Native نیز باید حفظ شود.
