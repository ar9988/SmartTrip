package com.neungi.moyeo.views.plan.tripviewmodel

import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("trips/{id}")
    fun getTrip(@Path("id") id: Int): Call<Text>
}