# SmartQR - Android App

تطبيق Android ذكي لمسح وتوليد أكواد QR.

## الميزات
- **المسح:** يدعم CameraX و ML Kit للمسح السريع مع الفلاش.
- **التوليد:** إنشاء QR للنصوص، الروابط، وشبكات Wi-Fi.
- **السجل:** حفظ آخر العمليات باستخدام Room Database.
- **التخزين:** حفظ الصور المولدة في المعرض.
- **الواجهة:** Jetpack Compose Material 3.

## متطلبات البناء
1. JDK 17.
2. Android SDK.

## كيفية البناء
1. امنح صلاحية التنفيذ لملف gradlew:
   ```bash
   chmod +x gradlew
   ```
2. قم بتنفيذ أمر البناء:
   ```bash
   ./gradlew assembleDebug
   ```
3. ستجد ملف APK في:
   `app/build/outputs/apk/debug/app-debug.apk`

*ملاحظة:* نظرًا لطبيعة هذا الملف المولد نصيًا، ملف `gradle/wrapper/gradle-wrapper.jar` (الباينري) غير موجود. عند تشغيل `./gradlew` لأول مرة، قد تحتاج إلى تثبيت Gradle محليًا أو تشغيل `gradle wrapper` إذا كان لديك Gradle مثبتًا مسبقًا لإنشاء ملف الـ JAR.

## صور
ضع صورة باسم `hero_image.png` في مجلد `app/src/main/res/drawable/` إذا أردت تخصيص واجهة السجل الفارغة.