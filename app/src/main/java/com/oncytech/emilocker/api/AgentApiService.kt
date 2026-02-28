package com.oncytech.emilocker.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val enrollmentToken: String,
    val imei1: String,
    val model: String,
    val manufacturer: String,
    val serialNumber: String? = null,
    val osVersion: String? = null,
    val appVersion: String? = "1.0.0",
    val gsfId: String? = null
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val deviceToken: String?,
    val config: AgentConfig?
)

data class AgentConfig(
    val checkInterval: Int,
    val lockStatus: String,
    val plan: String
)

interface AgentApiService {
    @POST("api/agent/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}
