# === GoWeb ProGuard Keep Rules ===

# Keep all GoWeb classes - prevent R8 from removing anything
-keep class com.goweb.browser.** { *; }
-keep class com.goweb.browser.ui.activity.** { *; }
-keep class com.goweb.browser.ui.adapter.** { *; }
-keep class com.goweb.browser.ui.dialog.** { *; }
-keep class com.goweb.browser.utils.** { *; }
-keep class com.goweb.browser.webview.** { *; }

# Keep JSON
-keep class org.json.** { *; }

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep resource classes
-keep class **.R { *; }
-keep class **.R$* { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum
-keepclassmembers enum * { *; }

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable { *; }

# Keep Android
-keep class android.support.** { *; }
-keep class androidx.** { *; }
-dontwarn android.support.**
-dontwarn androidx.**

# Optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 3
-allowaccessmodification
-repackageclasses com.goweb.browser.internal

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}
