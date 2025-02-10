package com.neungi.moyeo

import android.app.Application
import android.content.Context
import com.naver.maps.map.NaverMapSdk
import com.neungi.domain.usecase.SaveNotificationUseCase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MoyeoApplication : Application() {

    lateinit var saveNotificationUseCase: SaveNotificationUseCase
        private set

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()


        Timber.plant(Timber.DebugTree())

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_CLIENT_ID)
    }

    @Inject
    fun injectUseCase(useCase: SaveNotificationUseCase) {
        saveNotificationUseCase = useCase
    }

    companion object {

        var instance: MoyeoApplication? = null

        fun myContext(): Context {
            return instance!!.applicationContext
        }
    }
}