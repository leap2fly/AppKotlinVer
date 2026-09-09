# Proguard rules for R8 Full Mode

# Keep Room entities and DAOs
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# Preserve SQLCipher JNI and native libraries
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.SQLiteDatabase { *; }
-keep class net.sqlcipher.database.SQLiteOpenHelper { *; }
-keep class net.sqlcipher.database.SupportFactory { *; }

# Keep Hilt / Dagger generated components
-keep class * extends javax.inject.Provider
-keep class dagger.hilt.android.internal.managers.** { *; }

# Keep DataStore preference classes
-keep class androidx.datastore.preferences.protobuf.** { *; }
