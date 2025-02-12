package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult

interface InviteRepository {

    suspend fun postInviteLink(tripId: Int): ApiResult<String>

    suspend fun postInviteAccept(): ApiResult<String>
}