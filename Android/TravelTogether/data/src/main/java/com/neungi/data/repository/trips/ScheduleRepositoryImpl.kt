package com.neungi.data.repository.trips

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.ScheduleData
import com.neungi.domain.repository.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleRemoteDataSourceImpl: ScheduleRemoteDataSourceImpl
) : ScheduleRepository {
    override suspend fun getSchedulesById(tripId: Int): ApiResult<List<ScheduleData>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateSchedule(sId: Int, newPosition: Int): ApiResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSchedule(schedule: Int): ApiResult<Void> {
        TODO("Not yet implemented")
    }
}