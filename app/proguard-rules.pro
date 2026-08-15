# ProGuard rules for WarmWord
# Keep LiteRT-LM and LiteRT classes
-keep class com.google.ai.edge.litertlm.** { *; }
-keep class com.google.mlkit.** { *; }

# Keep Hilt
-keep class dagger.hilt.internal.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep class * extends dagger.hilt.android.processor.HiltProcessor { *; }

# Keep Room
-keep @androidx.room.Database class * { *; }
-keepclassmembernames class * {
    @androidx.room.PrimaryKey *;
}

# Keep Kotlin coroutines
-keepmembers class kotlinx.coroutines.** { *; }

# Keep data binding
-keep class * extends androidx.databinding.Observable { *; }
-keep class com.warmword.app.** { *; }
