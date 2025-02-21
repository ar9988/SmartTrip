package com.neungi.data.repository.invite

import android.util.Log
import com.neungi.domain.model.ApiResult
import com.neungi.domain.repository.InviteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import javax.inject.Inject

class InviteRepositoryImpl @Inject constructor(
    private val inviteRemoteDataSource: InviteRemoteDataSource
) : InviteRepository {

    override suspend fun postInviteLink(tripId: Int): ApiResult<String> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                inviteRemoteDataSource.postInviteLink(tripId)
            }

            val responseBody = response.body()
            Log.d("InviteRepositoryImpl", "Invite: $responseBody")
            if (response.isSuccessful && (responseBody != null)) {
                Log.d("InviteRepositoryImpl", "Invite Success: $responseBody")
                ApiResult.success(responseBody.token)
            } else {
                Log.d("InviteRepositoryImpl", "Invite Not Success: ${response.errorBody().toString()}")
                ApiResult.error(response.errorBody().toString(), null)
            }
        } catch (e: Exception) {
            Log.d("InviteRepositoryImpl", "Fail: ${e.message}")
            ApiResult.fail()
        }

    override suspend fun postInviteAccept(body: RequestBody): ApiResult<Pair<String, Int>> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                inviteRemoteDataSource.postInviteAccept(body)
            }

            val responseBody = response.body()
            if (response.isSuccessful && (responseBody != null)) {
                ApiResult.success(Pair(responseBody.message, responseBody.tripId))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }
        } catch (e: Exception) {
            ApiResult.fail()
        }
}