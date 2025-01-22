package com.neungi.data.repository.trips

import com.neungi.data.api.TripsApi
import javax.inject.Inject

class TripsRemoteDataSourceImpl @Inject constructor(
    private val tripsApi: TripsApi
) : TripsRemoteDataSource {

}