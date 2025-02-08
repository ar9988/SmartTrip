package com.neungi.data.di

import com.neungi.data.api.AiPlanningApi
import com.neungi.data.api.AlbumsApi
import com.neungi.data.api.FestivalApi
import com.neungi.data.api.AuthApi
import com.neungi.data.api.FCMApi
import com.neungi.data.api.TripsApi
import com.neungi.data.repository.aiplanning.datasource.AiPlanningDataSource
import com.neungi.data.repository.aiplanning.datasource.AiPlanningDataSourceImpl
import com.neungi.data.repository.festival.datasource.FestivalRemoteDataSource
import com.neungi.data.repository.festival.datasource.FestivalRemoteDataSourceImpl
import com.neungi.data.repository.albums.AlbumsRemoteDataSource
import com.neungi.data.repository.albums.AlbumsRemoteDataSourceImpl
import com.neungi.data.repository.auth.AuthRemoteDataSource
import com.neungi.data.repository.auth.AuthRemoteDataSourceImpl
import com.neungi.data.repository.fcm.datasource.FCMDataSource
import com.neungi.data.repository.fcm.datasource.FCMDataSourceImpl
import com.neungi.data.repository.trips.TripsRemoteDataSource
import com.neungi.data.repository.trips.TripsRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataModule {

    @Provides
    @Singleton
    fun provideAlbumsRemoteDataSource(albumsApi: AlbumsApi): AlbumsRemoteDataSource {
        return AlbumsRemoteDataSourceImpl(albumsApi)
    }

    @Provides
    @Singleton
    fun provideTripsRemoteDataSource(tripsApi: TripsApi): TripsRemoteDataSource {
        return TripsRemoteDataSourceImpl(tripsApi)
    }

    @Provides
    @Singleton
    fun provideFestivalRemoteDataSource(festivalApi: FestivalApi): FestivalRemoteDataSource {
        return FestivalRemoteDataSourceImpl(festivalApi)
    }


    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(authApi: AuthApi): AuthRemoteDataSource {
        return AuthRemoteDataSourceImpl(authApi)
    }

    @Provides
    @Singleton
    fun provideAiPlanningDataSource(aiPlanningApi: AiPlanningApi): AiPlanningDataSource {
        return AiPlanningDataSourceImpl(aiPlanningApi)
    }

    @Provides
    @Singleton
    fun provideFCMSource(fcmApi: FCMApi): FCMDataSource {
        return FCMDataSourceImpl(fcmApi)
    }
}