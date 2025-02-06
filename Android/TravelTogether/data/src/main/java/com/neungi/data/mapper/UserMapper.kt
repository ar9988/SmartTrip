package com.neungi.data.mapper

import com.neungi.data.entity.UserEntity
import com.neungi.domain.model.User

object UserMapper {

    operator fun invoke(userEntity: UserEntity): User {
        return User(
            id = userEntity.id.toString(),
            email = userEntity.email,
            passwordHash = "",
            nickname = userEntity.name,
            profile = userEntity.profile ?: "",
            createdAt = userEntity.createdAt ?: "",
            updatedAt = userEntity.updatedAt ?: ""
        )
    }
}