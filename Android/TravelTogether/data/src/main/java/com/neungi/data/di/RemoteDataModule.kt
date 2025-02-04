package com.neungi.data.di

import com.neungi.data.api.AlbumsApi
import com.neungi.data.api.FestivalApi
import com.neungi.data.api.TripsApi
import com.neungi.data.repository.aiplanning.FestivalRepositoryImpl
import com.neungi.data.repository.aiplanning.datasource.FestivalRemoteDataSource
import com.neungi.data.repository.aiplanning.datasource.FestivalRemoteDataSourceImpl
import com.neungi.data.repository.albums.AlbumsRemoteDataSource
import com.neungi.data.repository.albums.AlbumsRemoteDataSourceImpl
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

//    @Provides
//    @Singleton
//    fun provideAiPlanningLocalRegionDataSource(): AiPlanningLocalRegionDataSource {
//        return AiPlanningLocalRegionDataSourceImpl()
//    }
}