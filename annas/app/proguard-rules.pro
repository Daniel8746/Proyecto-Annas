# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Si usas Kotlinx Serialization
-keepattributes *Annotation*,Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Mantiene los metodos expuestos a WebView JavaScript aunque R8 minimice release.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Si usas Moshi (muy común con OkHttp puro)
#-keepattributes *Annotation*,Signature
#-dontwarn class org.codehaus.mojo.animal_sniffer.*

# Reglas críticas para la infraestructura de red de OkHttp
-dontwarn okhttp3.internal.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn openales.**
