package com.neungi.moyeo

import android.app.Application
import android.content.Context
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MoyeoApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()


        Timber.plant(Timber.DebugTree())

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_CLIENT_ID)
    }

    companion object {

        var instance: MoyeoApplication? = null

        fun myContext(): Context {
            return instance!!.applicationContext
        }
    }
}