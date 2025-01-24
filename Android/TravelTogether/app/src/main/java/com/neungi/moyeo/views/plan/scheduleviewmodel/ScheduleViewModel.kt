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
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.conversions.moshi.withMoshi
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.stomp.use
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(

) : ViewModel() {
    private val _scheduleData = MutableStateFlow<List<ScheduleData>>(emptyList())
    val scheduleData = _scheduleData.asStateFlow()
    val endPoint = ""
    val client = StompClient(OkHttpWebSocketClient())
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()


    init {
        val newSchedule = mutableListOf<ScheduleData>()
        newSchedule.add(ScheduleData(0,"일정1","10시","11시","동굴","30분"))
        newSchedule.add(ScheduleData(1,"일정2","12시","13시","식당","30분"))
        newSchedule.add(ScheduleData(2,"일정3","14시","15시","공원","30분"))
        newSchedule.add(ScheduleData(3,"일정4","15시30분","16시","등산","30분"))
        newSchedule.add(ScheduleData(4,"일정5 ","17시","19시","식당","30분"))
        _scheduleData.value = newSchedule.toList()

//        viewModelScope.launch { // other config can be passed in here
//            val session: StompSession = client.connect(endPoint).withMoshi(moshi) // optional login/passcode can be provided here
//            //연결을 위한 주소
//            val token ="asdf"
//            val newMessage:Flow<StompFrame.Message> = session.subscribe(
//                StompSubscribeHeaders(
//                    destination = "url", //for subscribe
//                    customHeaders = mapOf(
//                        "Authorization" to "${token}"
//                    )
//                )
//            )
//            newMessage.collect{
//                println(it)
//            }
//        }
    }
}