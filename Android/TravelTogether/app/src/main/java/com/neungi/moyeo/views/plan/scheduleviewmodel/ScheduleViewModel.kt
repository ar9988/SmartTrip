package com.neungi.moyeo.views.plan.scheduleviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(

) : ViewModel() {
    private val _scheduleData = MutableStateFlow<List<ScheduleData>>(emptyList())
    val scheduleData = _scheduleData.asStateFlow()
    val serverUrl = "ws://192.168.100.203:9987/ws"
    var tripId = 0
    val client = OkHttpClient()
    val request = Request.Builder().url(serverUrl).build()
    val webSocket = client.newWebSocket(request,object :WebSocketListener(){
        override fun onMessage(webSocket: WebSocket, text: String) {
            print("onMessage")
        }


        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            super.onFailure(webSocket, t, response)
//            Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            super.onOpen(webSocket, response)
//            sendMessage("Hello from Android!")
        }

    })

    init {
        val newSchedule = mutableListOf<ScheduleData>()
        newSchedule.add(ScheduleData(0, "일정1", "10시", "11시", "동굴", "30분"))
        newSchedule.add(ScheduleData(1, "일정2", "12시", "13시", "식당", "30분"))
        newSchedule.add(ScheduleData(2, "일정3", "14시", "15시", "공원", "30분"))
        newSchedule.add(ScheduleData(3, "일정4", "15시30분", "16시", "등산", "30분"))
        newSchedule.add(ScheduleData(4, "일정5 ", "17시", "19시", "식당", "30분"))
        _scheduleData.value = newSchedule.toList()
    }
}