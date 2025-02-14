package com.neungi.domain.usecase

import com.neungi.domain.model.Notification
import com.neungi.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveNotificationUseCase@Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    suspend fun saveNotifiacation(newNotification: Notification) = dataStoreRepository.saveNotification(newNotification)

    fun getNotification():Flow<List<Notification>> = dataStoreRepository.getNotifications()

    suspend fun deleteNotification(notificationId: String) = dataStoreRepository.deleteNotification(notificationId)

    suspend fun clearNotification() = dataStoreRepository.clearNotifications()
}