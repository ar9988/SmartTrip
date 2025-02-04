package com.neungi.domain.repository
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.ScheduleData

interface ScheduleRepository {
    suspend fun getSchedulesById(tripId: Int): ApiResult<List<ScheduleData>>
    suspend fun updateSchedule(): ApiResult<Boolean>
    suspend fun deleteSchedule(schedule: Int): ApiResult<Void>
}