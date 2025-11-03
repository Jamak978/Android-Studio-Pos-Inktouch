package com.example.inktouch

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class InkTouchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable vector drawable support for older Android versions
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}
