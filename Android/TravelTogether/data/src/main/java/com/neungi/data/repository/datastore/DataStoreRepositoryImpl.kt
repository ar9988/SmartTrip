package com.neungi.data.repository.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.neungi.domain.model.LoginInfo
import com.neungi.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
            preferences[JWT_TOKEN].toString()
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
            preferences[USER_ID].toString()
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
            preferences[USER_EMAIL].toString()
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
            preferences[USER_NAME] ?: ""
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
            preferences[USER_PROFILE].toString()
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
            val userProfile = preferences[USER_PROFILE]

            // userId, userEmail, userName 중 하나라도 null이면 null 리턴
            if (userId != null && userEmail != null && userName != null) {
                LoginInfo(
                    userId = userId,
                    userEmail = userEmail,
                    userName = userName,
                    userProfileImg = userProfile
                )
            } else {
                null
            }
        }


    companion object {

        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_PROFILE = stringPreferencesKey("user_profile")
    }
}