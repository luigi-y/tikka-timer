# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide original source file name
-renamesourcefileattribute SourceFile

# ============================================================================
# Hilt / Dagger
# ============================================================================
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt generated classes
-keep class **_HiltModules* { *; }
-keep class **_GeneratedInjector { *; }

# ============================================================================
# Room Database
# ============================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room DAOs
-keep interface * extends androidx.room.Dao { *; }

# ============================================================================
# Jetpack Compose
# ============================================================================
-dontwarn androidx.compose.**

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }

# ============================================================================
# Kotlin
# ============================================================================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep Kotlin data classes
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ============================================================================
# Coroutines
# ============================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================================================
# App-specific rules
# ============================================================================
# Keep domain models (used for serialization/reflection)
-keep class com.tikkatimer.domain.model.** { *; }

# Keep Room entities
-keep class com.tikkatimer.data.local.entity.** { *; }
