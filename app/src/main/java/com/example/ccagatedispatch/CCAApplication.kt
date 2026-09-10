package com.example.ccagatedispatch

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.sqlcipher.database.SQLiteDatabase

@HiltAndroidApp
class CCAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize SQLCipher native libraries for 256-bit AES database encryption
        SQLiteDatabase.loadLibs(this)
    }
}
