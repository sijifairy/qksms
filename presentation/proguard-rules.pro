-dontobfuscate

# android-smsmms
# -keep class android.net.** { *; }
-dontwarn android.net.ConnectivityManager
-dontwarn android.net.LinkProperties

# autodispose
-dontwarn com.uber.autodispose.**

# ez-vcard
-dontwarn ezvcard.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn org.python.core.**

# okio
-dontwarn okio.**

-dontwarn com.android.installreferrer

# Flurry
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**

# Tapjoy
-keep class com.tapjoy.** { *; }
-dontwarn com.tapjoy.**
-dontwarn com.google.**
-dontwarn com.ihs.**
-dontwarn com.messagecenter.**
-dontwarn net.appcloudbox.**
-dontwarn com.amazon.**
-dontwarn com.appsflyer.FirebaseInstanceIdListener**