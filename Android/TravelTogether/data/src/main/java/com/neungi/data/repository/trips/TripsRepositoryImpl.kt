import android.util.Log
import com.google.gson.GsonBuilder
import com.neungi.data.mapper.TripMapper
import com.neungi.data.repository.trips.LocalDateTimeSerializer
import com.neungi.data.repository.trips.TripsRemoteDataSource
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.CreateTripRequest
import com.neungi.domain.model.Trip
import com.neungi.domain.repository.TripsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class TripsRepositoryImpl @Inject constructor(
    private val tripsRemoteDataSource: TripsRemoteDataSource
) : TripsRepository {

    override suspend fun getTrips(userId: String): ApiResult<List<Trip>> =
        try {
            // 네트워크 요청 시작 전 로그
            Log.d("TripRepository", "getTrips() - userId: $userId, 네트워크 요청 시작")

            // 실제 네트워크 요청
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                tripsRemoteDataSource.getTrips(userId)
            }

            // 응답 상태 코드와 본문 로그
            val body = response.body()

// 응답이 성공적이고 body가 null이 아닌지 확인
            if (response.isSuccessful && body != null) {
                Log.d("TripRepository", "Response Code: ${response.code()}")
                Log.d("TripRepository", "Response Body: ${body.toString()}") // 응답 본문도 확인
            } else {
                Log.e("TripRepository", "응답 실패 - Error Body: ${response.errorBody()?.string()}")
            }


            if (response.isSuccessful && body != null) {
                // 성공적인 응답 로그
                Log.d("TripRepository", "응답 성공 - 데이터 매핑 시작")
                ApiResult.success(TripMapper(body.trips))
            } else {
                // 오류 발생 시 로그
                Log.e("TripRepository", "응답 실패 - Error Body: ${response.errorBody()?.string()}")
                ApiResult.error(response.errorBody()?.string() ?: "알 수 없는 오류", null)
            }
        } catch (e: Exception) {
            // 예외 발생 시 로그
            Log.e("TripRepository", "예외 발생: ${e.localizedMessage}")
            e.printStackTrace()
            ApiResult.fail()
        }

    override suspend fun deleteTrip(userId: String, tripId: Int): ApiResult<Boolean> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                tripsRemoteDataSource.deleteTrip(userId,tripId)
            }

            val responseBody = response.body()
            if (response.isSuccessful && (responseBody != null)) {
                ApiResult.success(responseBody)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun createTrip(
        userId: String,
        title: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): ApiResult<Boolean> =
        try {
            // 네트워크 요청 시작 전 로그
            Log.d("TripsRepository", "createTrips() - userId: $userId, 네트워크 요청 시작")

            // CreateTripRequest 객체를 JSON으로 변환
            val createTripRequest = CreateTripRequest(
                title,
                startDate.atTime(9,0),
                endDate.atTime(23,0),
                userId.toInt()
            )

            Log.d("TripsRepository", "createTripRequest: ${createTripRequest.toString()}")
            // Gson으로 JSON 문자열 생성
            val gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java,LocalDateTimeSerializer()).create()
            val jsonRequestBody = gson.toJson(createTripRequest)

            // 생성된 JSON 문자열 로그로 찍기
            Log.d("TripsRepository", "Request Body JSON: $jsonRequestBody")

            // 실제 네트워크 요청
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                tripsRemoteDataSource.createTrip(
                    jsonRequestBody.toRequestBody("application/json".toMediaTypeOrNull())
                )
            }

            // 응답 상태 코드와 본문 로그
            val body = response.body()
            Log.d("TripsRepository", response.toString())

            if (response.isSuccessful && (body != null)) {
                ApiResult.success(body)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }
        } catch (e: Exception) {
            // 예외 발생 시 로그
            Log.e("TripRepository", "create 예외 발생: ${e.localizedMessage}")
            e.printStackTrace()
            ApiResult.fail()
        }


}
