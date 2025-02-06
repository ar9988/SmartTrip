import android.util.Log
import com.neungi.data.mapper.TripMapper
import com.neungi.data.repository.trips.TripsRemoteDataSource
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import com.neungi.domain.repository.TripsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripsRepositoryImpl @Inject constructor(
    private val tripsRemoteDataSource: TripsRemoteDataSource
) : TripsRepository {

    override suspend fun getTrips(userId: Int): ApiResult<List<Trip>> =
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
}
