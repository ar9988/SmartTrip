package com.neungi.moyeo

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
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
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_CLIENT_ID)

        var keyHash = Utility.getKeyHash(this)
        Timber.d("KeyHash: $keyHash")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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