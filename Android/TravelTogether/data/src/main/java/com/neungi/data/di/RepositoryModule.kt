package com.neungi.data.di

import com.neungi.data.repository.aiplanning.FestivalRepositoryImpl
import com.neungi.data.repository.aiplanning.datasource.FestivalRemoteDataSource
import com.neungi.data.repository.albums.AlbumsRemoteDataSource
import com.neungi.data.repository.albums.AlbumsRepositoryImpl
import com.neungi.data.repository.trips.TripsRemoteDataSource
import com.neungi.data.repository.trips.TripsRepositoryImpl
import com.neungi.domain.repository.AlbumsRepository
import com.neungi.domain.repository.FestivalRepository
import com.neungi.domain.repository.TripsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAlbumsRepository(albumsRemoteDataSource: AlbumsRemoteDataSource): AlbumsRepository {
        return AlbumsRepositoryImpl(albumsRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideTripsRepository(tripsRemoteDataSource: TripsRemoteDataSource): TripsRepository {
        return TripsRepositoryImpl(tripsRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideFestivalRepository(festivalDataSource: FestivalRemoteDataSource): FestivalRepository {
        return FestivalRepositoryImpl(festivalDataSource)
    }
}