package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import okhttp3.RequestBody

interface InviteRepository {

    suspend fun postInviteLink(tripId: Int): ApiResult<String>

    suspend fun postInviteAccept(body: RequestBody): ApiResult<Pair<String, Int>>
}