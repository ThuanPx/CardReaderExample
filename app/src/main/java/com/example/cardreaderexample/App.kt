package com.example.cardreaderexample

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.squareup.sdk.reader.ReaderSdk

/**
 * --------------------
 * Created by ThuanPx on 6/18/2019.
 * Screen name:
 * --------------------
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ReaderSdk.initialize(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // Required if minSdkVersion < 21
        MultiDex.install(this)
    }
}