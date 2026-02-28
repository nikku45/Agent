package com.oncytech.emilocker.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val DEFAULT_URL = "http://10.0.2.2:5000/"

    fun getService(baseUrl: String? = null): AgentApiService {
        val finalUrl = if (baseUrl.isNullOrEmpty()) DEFAULT_URL else {
            if (baseUrl.startsWith("http")) baseUrl else "http://$baseUrl/"
        }
        
        return Retrofit.Builder()
            .baseUrl(finalUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AgentApiService::class.java)
    }
}
