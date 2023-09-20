package com.example.broadcastation.entity.http

import com.example.broadcastation.entity.Remote
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

interface RetrofitAPI {
    @POST("remote")
    fun postRemoteContent(@Body content: Remote?): Call<Remote?>?
}