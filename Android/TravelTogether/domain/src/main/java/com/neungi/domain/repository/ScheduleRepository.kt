package com.neungi.domain.repository
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Schedule

interface ScheduleRepository {
    suspend fun getSchedulesById(tripId: Int): ApiResult<List<Schedule>>
    suspend fun updateSchedule(): ApiResult<Boolean>
    suspend fun deleteSchedule(schedule: Int): ApiResult<Void>
}