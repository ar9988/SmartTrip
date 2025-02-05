package com.neungi.data.repository.trips

import android.util.Log
import com.neungi.data.api.TripsApi
import com.neungi.data.entity.TripEntity
import retrofit2.Response
import javax.inject.Inject

class TripsRemoteDataSourceImpl @Inject constructor(
    private val tripsApi: TripsApi
) : TripsRemoteDataSource {
    override suspend fun getTrips(userId: String): Response<List<TripEntity>> {
        // 요청을 보내기 전에 로그 출력
        Log.d("TripsRemoteDataSource", "Requesting trips for userId: $userId")

        val response = tripsApi.getTrips(userId)

        // 응답 결과 로그 출력
        Log.d("TripsRemoteDataSource", "Received response: ${response.body()}")

        return response
    }
}