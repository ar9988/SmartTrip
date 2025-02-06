package com.neungi.data.di

import TripsRepositoryImpl
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.neungi.data.repository.aiplanning.AiPlanningRepositoryImpl
import com.neungi.data.repository.aiplanning.datasource.AiPlanningDataSource
import com.neungi.data.repository.festival.FestivalRepositoryImpl
import com.neungi.data.repository.festival.datasource.FestivalRemoteDataSource
import com.neungi.data.repository.albums.AlbumsRemoteDataSource
import com.neungi.data.repository.albums.AlbumsRepositoryImpl
import com.neungi.data.repository.auth.AuthRemoteDataSource
import com.neungi.data.repository.auth.AuthRepositoryImpl
import com.neungi.data.repository.datastore.DataStoreRepositoryImpl
import com.neungi.data.repository.trips.TripsRemoteDataSource
import com.neungi.domain.repository.AiPlanningRepository
import com.neungi.domain.repository.AlbumsRepository
import com.neungi.domain.repository.FestivalRepository
import com.neungi.domain.repository.AuthRepository
import com.neungi.domain.repository.DataStoreRepository
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

    @Provides
    @Singleton
    fun provideAuthRepository(authRemoteDataSource: AuthRemoteDataSource): AuthRepository {
        return AuthRepositoryImpl(authRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideDataStoreRepository(dataStore: DataStore<Preferences>): DataStoreRepository {
        return DataStoreRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideAiPlanningRepository(aiPlanningDataSource: AiPlanningDataSource): AiPlanningRepository {
        return AiPlanningRepositoryImpl(aiPlanningDataSource)
    }
}