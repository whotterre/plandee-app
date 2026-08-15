package com.example.plandee.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class TelemetrySyncRequest(
    val userId: String,
    val totalMobileGb: Double,
    val totalWifiGb: Double,
    val dailyBurnGb: Double,
    val topApps: List<AppTelemetryPayload>
)

data class AppTelemetryPayload(
    val packageName: String,
    val appName: String,
    val bytesUsed: Long,
    val sharePercentage: Double
)

data class TelemetrySyncResponse(
    val success: Boolean,
    val message: String,
    val syncedAtMillis: Long
)

data class TariffPlanResponse(
    val carrier: String,
    val planName: String,
    val priceNaira: Int,
    val dataGb: Double,
    val durationDays: Int,
    val ussdCode: String
)

interface ApiService {

    @POST("api/v1/telemetry/sync")
    suspend fun syncTelemetry(
        @Body payload: TelemetrySyncRequest
    ): Response<TelemetrySyncResponse>

    @GET("api/v1/tariffs/recommendations")
    suspend fun getTariffRecommendations(
        @Query("carrier") carrier: String,
        @Query("duration") duration: String,
        @Query("budget") budget: Int
    ): Response<List<TariffPlanResponse>>
}
