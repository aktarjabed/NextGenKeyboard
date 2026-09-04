# NxtGenKeyboard release hardening.
-keep class com.aktarjabed.nxtgenkeyboard.ime.NxtGenKeyboard { *; }
-keepattributes *Annotation*
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn org.xmlpull.v1.**