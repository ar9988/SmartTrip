package com.neungi.data.di

import com.neungi.data.api.AlbumsApi
import com.neungi.data.api.TripsApi
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

    private const val BASE_URL = ""

    @Provides
    @Singleton
    fun provideMoshiConverterFactory(): MoshiConverterFactory {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    @Named("Moyeo")
    fun provideMoyeoRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient())
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
}