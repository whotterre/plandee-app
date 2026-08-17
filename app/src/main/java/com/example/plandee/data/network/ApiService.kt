package com.example.plandee.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Auth DTOs
data class RegisterRequest(
    val email: String,
    val password: String,
    val country: String = "Nigeria"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val status: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    val token: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    val message: String? = null
) {
    fun getTokenOrFallback(): String? {
        return if (!accessToken.isNullOrEmpty()) accessToken else token
    }
}

// Pro Subscription DTOs
data class ProStatusResponse(
    @SerializedName("is_pro") val isPro: Boolean,
    @SerializedName("tokens_remaining") val tokensRemaining: Int?
)

data class ProUpgradeResponse(
    val status: String,
    val message: String?,
    @SerializedName("is_pro") val isPro: Boolean?
)

// Telemetry Ingestion DTOs (matching Go backend TelemetryIngestionDto)
data class UsageHistoryPayload(
    @SerializedName("connectionType") val connectionType: String,
    @SerializedName("networkCarrier") val networkCarrier: String,
    @SerializedName("appName") val appName: String,
    @SerializedName("appPackageName") val appPackageName: String,
    @SerializedName("bytesUsed") val bytesUsed: Long,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)

data class TelemetryIngestionRequest(
    val data: List<UsageHistoryPayload>
)

data class TelemetrySyncResponse(
    val message: String?
)

// Telemetry Summary DTOs
data class TotalStatsDto(
    @SerializedName("totalBytes") val totalBytes: Long,
    @SerializedName("totalGB") val totalGB: Double,
    @SerializedName("mobileBytes") val mobileBytes: Long,
    @SerializedName("mobileGB") val mobileGB: Double,
    @SerializedName("wifiBytes") val wifiBytes: Long,
    @SerializedName("wifiGB") val wifiGB: Double,
    @SerializedName("mobilePercentage") val mobilePercentage: Double,
    @SerializedName("wifiPercentage") val wifiPercentage: Double
)

data class GetSummaryResponseDto(
    val period: String,
    val totals: List<TotalStatsDto>?
)

// Leaderboard DTOs
data class LeaderboardAppEntryDto(
    @SerializedName("app_name") val appName: String,
    @SerializedName("app_package_name") val appPackageName: String,
    @SerializedName("total_bytes") val totalBytes: Long,
    @SerializedName("total_mb") val totalMb: Double,
    @SerializedName("total_gb") val totalGb: Double,
    val percentage: Double
)

data class LeaderboardResponseDto(
    val status: String,
    val data: List<LeaderboardAppEntryDto>?
)

// ML Recommendation DTOs (matching Go backend MatchRecommendationRequestDto & MatchRecommendationResponseDto)
data class MatchRecommendationRequest(
    @SerializedName("active_sims") val activeSims: List<String>,
    @SerializedName("monthly_budget_ngn") val monthlyBudgetNgn: Double,
    @SerializedName("total_30day_bytes") val total30DayBytes: Long,
    @SerializedName("night_usage_percentage") val nightUsagePercentage: Double,
    @SerializedName("top_app_categories") val topAppCategories: List<String>
)

data class MatchedPlanResultDto(
    @SerializedName("plan_id") val planId: String,
    val carrier: String,
    @SerializedName("plan_name") val planName: String,
    val price: Double,
    @SerializedName("validity_days") val validityDays: Int,
    @SerializedName("data_allowance_gb") val dataAllowanceGb: Double,
    @SerializedName("match_score_percentage") val matchScorePercentage: Int,
    @SerializedName("ussd_code") val ussdCode: String,
    val tags: List<String>?,
    @SerializedName("estimated_monthly_savings") val estimatedMonthlySavings: Double
)

data class MatchRecommendationResponse(
    @SerializedName("analysis_summary") val analysisSummary: String,
    @SerializedName("tokens_remaining") val tokensRemaining: Int,
    @SerializedName("is_pro") val isPro: Boolean,
    @SerializedName("featured_plan") val featuredPlan: MatchedPlanResultDto?,
    @SerializedName("alternative_plans") val alternativePlans: List<MatchedPlanResultDto>?
)

// Ad Reward DTOs
data class AdRewardRequest(
    @SerializedName("ad_unit_id") val adUnitId: String,
    val token: String
)

data class AdRewardResponse(
    val status: String,
    val message: String?,
    @SerializedName("remaining_tokens") val remainingTokens: Int?
)

interface ApiService {

    @POST("v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("v1/pro/status")
    suspend fun getProStatus(): Response<ProStatusResponse>

    @POST("v1/pro/upgrade")
    suspend fun upgradePro(): Response<ProUpgradeResponse>

    @POST("v1/telemetry/sync")
    suspend fun syncTelemetry(
        @Body request: TelemetryIngestionRequest
    ): Response<TelemetrySyncResponse>

    @GET("v1/telemetry/summary")
    suspend fun getTelemetrySummary(
        @Query("period") period: String = "30d"
    ): Response<GetSummaryResponseDto>

    @GET("v1/telemetry/leaderboard")
    suspend fun getLeaderboard(): Response<LeaderboardResponseDto>

    @POST("v1/recommendations/match")
    suspend fun matchRecommendation(
        @Body request: MatchRecommendationRequest
    ): Response<MatchRecommendationResponse>

    @POST("v1/rewards/ad-reward")
    suspend fun rewardAdToken(
        @Body request: AdRewardRequest
    ): Response<AdRewardResponse>
}
