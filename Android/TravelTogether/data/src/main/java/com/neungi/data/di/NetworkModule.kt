package com.neungi.data.di

import com.neungi.data.api.AiPlanningApi
import com.neungi.data.api.AlbumsApi
import com.neungi.data.api.FestivalApi
import com.neungi.data.api.AuthApi
import com.neungi.data.api.FCMApi
import com.neungi.data.api.TripsApi
import com.neungi.data.util.JwtInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

//    private const val BASE_URL3 = "http://43.202.51.112:8081/"
    private const val BASE_URL = "http://43.202.51.112:8080/"
//    private const val WEBSOCKET_URL = "ws://43.202.51.112:8080/"
//    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideMoshiConverterFactory(): MoshiConverterFactory {
        val moshi = Moshi.Builder()
            .add(ZonedDateTimeJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(jwtInterceptor: JwtInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(jwtInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("Moyeo")
    fun provideMoyeoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(provideMoshiConverterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideTripsApiService(@Named("Moyeo") retrofit: Retrofit): TripsApi {
        return retrofit.create(TripsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAlbumsApiService(@Named("Moyeo") retrofit: Retrofit): AlbumsApi {
        return retrofit.create(AlbumsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFestivalApiService(@Named("Moyeo") retrofit: Retrofit): FestivalApi {
        return retrofit.create(FestivalApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAuthApiService(@Named("Moyeo") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAiPlanningApiService(@Named("Moyeo") retrofit: Retrofit): AiPlanningApi {
        return retrofit.create(AiPlanningApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFCMApiService(@Named("Moyeo") retrofit: Retrofit): FCMApi {
        return retrofit.create(FCMApi::class.java)
    }
}