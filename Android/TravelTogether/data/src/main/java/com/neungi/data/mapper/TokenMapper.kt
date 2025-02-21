package com.neungi.data.mapper

import com.neungi.data.entity.TokenEntity
import com.neungi.domain.model.User

object TokenMapper {

    operator fun invoke(tokenEntity: TokenEntity): Pair<User, String> {
        return Pair(
            User(
                id = tokenEntity.id.toString(),
                email = tokenEntity.email,
                passwordHash = "",
                nickname = tokenEntity.name,
                profile = tokenEntity.profile ?: "",
                profileImage = tokenEntity.profileImage ?: "",
                createdAt = "",
                updatedAt = ""
            ),
            tokenEntity.token
        )
    }
}