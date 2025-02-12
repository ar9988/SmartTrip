package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.repository.InviteRepository
import javax.inject.Inject

class GetInviteUseCase @Inject constructor(
    private val inviteRepository: InviteRepository
) {

    suspend fun invite(tripId: Int): ApiResult<String> {
        return inviteRepository.postInviteLink(tripId)
    }

    suspend fun inviteAccept(): ApiResult<String> {
        return inviteRepository.postInviteAccept()
    }
}