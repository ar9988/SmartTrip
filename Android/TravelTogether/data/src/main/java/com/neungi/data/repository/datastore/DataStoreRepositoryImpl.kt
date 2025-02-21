package com.neungi.data.repository.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.neungi.data.entity.NotificationEntity
import com.neungi.data.mapper.toDomain
import com.neungi.data.mapper.toEntity
import com.neungi.domain.model.LoginInfo
import com.neungi.domain.model.Notification
import com.neungi.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DataStoreRepository {

    override suspend fun setJWT(token: String) {
        dataStore.edit { preferences ->
            preferences[JWT_TOKEN] = token
        }
    }

    override fun getJWT(): Flow<String?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[JWT_TOKEN]
        }

    override suspend fun setUserId(id: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = id
        }
    }

    override fun getUserId(): Flow<String?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[USER_ID]
        }

    override suspend fun setUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

    override fun getUserEmail(): Flow<String?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[USER_EMAIL]
        }

    override suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    override fun getUserName(): Flow<String?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[USER_NAME]
        }

    override suspend fun setUserProfileMessage(message: String) {
        dataStore.edit { preferences ->
            preferences[USER_PROFILE_MESSAGE] = message
        }
    }

    override fun getUserProfileMessage(): Flow<String?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[USER_PROFILE_MESSAGE]
        }

    override suspend fun setUserProfile(profile: String) {
        dataStore.edit { preferences ->
            preferences[USER_PROFILE] = profile
        }
    }

    override fun getUserProfile(): Flow<String?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[USER_PROFILE]
        }

    override fun getLoginInfo(): Flow<LoginInfo?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            val userId = preferences[USER_ID]
            val userEmail = preferences[USER_EMAIL]
            val userName = preferences[USER_NAME]
            val userProfileMessage = preferences[USER_PROFILE_MESSAGE]
            val userProfile = preferences[USER_PROFILE]

            // userId, userEmail, userName, userProfileMessage 중 하나라도 null이면 null 리턴
            if (userId != null && userEmail != null && userName != null) {
                LoginInfo(
                    userId = userId,
                    userEmail = userEmail,
                    userName = userName,
                    userProfileMessage = userProfileMessage?:"",
                    userProfileImg = userProfile
                )
            }else {
                null
            }
        }

    override suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_NAME)
            preferences.remove(USER_PROFILE)
        }
    }

    override fun getDeviceId(): Flow<String?> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[DEVICE_ID]
        }

    override suspend fun setDeviceId(deviceID:String) {
        dataStore.edit { preferences ->
            preferences[DEVICE_ID] = deviceID
        }
    }

    override suspend fun saveNotification(newNotification: Notification) {

        dataStore.edit { preferences ->
            // 기존 알림 목록 가져오기
            val existingNotifications = preferences[NOTIFICATIONS]?.let {
                Json.decodeFromString<List<NotificationEntity>>(it)
            } ?: emptyList()


            val newNotifcationEntity = newNotification.toEntity()
            val updatedNotifications = existingNotifications + newNotifcationEntity

            preferences[NOTIFICATIONS] = Json.encodeToString(updatedNotifications)
        }
    }

    override fun getNotifications(): Flow<List<Notification>> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[NOTIFICATIONS]?.let {
                try {
                    Json.decodeFromString<List<NotificationEntity>>(it).map {notificationEntity ->
                        notificationEntity.toDomain()
                    }.sortedByDescending { notification -> notification.timestamp }
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }

    override suspend fun deleteNotification(notificationId: String) {
        dataStore.edit { preferences ->
            val notifications = preferences[NOTIFICATIONS]?.let {
                Json.decodeFromString<List<NotificationEntity>>(it)
            } ?: emptyList()

            val updatedNotifications = notifications.filter { it.id != notificationId }
            preferences[NOTIFICATIONS] = Json.encodeToString(updatedNotifications)
        }
    }

    override suspend fun clearNotifications() {
        dataStore.edit { preferences ->
            preferences.remove(NOTIFICATIONS)
        }
    }

    companion object {

        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_PROFILE_MESSAGE = stringPreferencesKey("user_profile_message")
        private val USER_PROFILE = stringPreferencesKey("user_profile")
        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val NOTIFICATIONS = stringPreferencesKey("notifications")
    }
}