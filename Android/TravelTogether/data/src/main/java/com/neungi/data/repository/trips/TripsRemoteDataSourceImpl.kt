package com.neungi.data.repository.trips

import android.util.Log
import com.neungi.data.api.TripsApi
import com.neungi.data.entity.TripsResponse
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class TripsRemoteDataSourceImpl @Inject constructor(
    private val tripsApi: TripsApi
) : TripsRemoteDataSource {
    override suspend fun getTrips(userId: String): Response<TripsResponse> {
        // 요청을 보내기 전에 로그 출력
        Log.d("TripsRemoteDataSource", "Requesting trips for userId: $userId")

        val response = tripsApi.getTrips(userId)

        // 응답 결과 로그 출력
        Log.d("TripsRemoteDataSource", "Received response: ${response.body()}")

        return response
    }

    override suspend fun createTrip(body: RequestBody): Response<Boolean> {
        tripsApi.createTrip(body)
        return tripsApi.createTrip(body)
    }

    override suspend fun deleteTrip(userId: String, tripId: Int): Response<Boolean> {
        return tripsApi.deleteTrip(userId,tripId)
    }

    override suspend fun getLatestTrip(): Response<TripsResponse> = tripsApi.getLatestTrip()


}