# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Ignore warnings and keep org.bouncycastle classes to prevent compilation and runtime errors
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }
-keep interface org.bouncycastle.** { *; }

# Keep our data classes and database entities intact
-keep class com.example.data.** { *; }
-keep interface com.example.data.** { *; }

# Keep Room database and models from being stripped or broken by R8
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**
-keepclassmembers class * {
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
    @androidx.room.Embedded *;
    @androidx.room.Relation *;
}

# Preserve Moshi classes and generated adapters
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep class *JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Preserve OkHttp3 and Okio core
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Preserve Retrofit and standard serialization traits
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exception
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Strip all android.util.Log calls from the release build.
# R8 will completely remove these call sites, producing a smaller and
# cleaner release binary with no logcat output leaking internal state.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static java.lang.String getStackTraceString(java.lang.Throwable);
}
