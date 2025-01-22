package com.neungi.data.repository.trips

import com.neungi.domain.repository.TripsRepository
import javax.inject.Inject

class TripsRepositoryImpl @Inject constructor(
    private val tripsRemoteDataSource: TripsRemoteDataSource
) : TripsRepository {

}