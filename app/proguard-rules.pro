# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name.
-renamesourcefileattribute SourceFile

# --- Android 16 AppFunctions Keep Rules ---
# Keep any class containing methods annotated with @AppFunction
-keep class * {
    @androidx.appfunctions.service.AppFunction <methods>;
}

# Keep classes annotated with @AppFunctionSerializable and all their fields
-keep @androidx.appfunctions.AppFunctionSerializable class * {
    *;
}

