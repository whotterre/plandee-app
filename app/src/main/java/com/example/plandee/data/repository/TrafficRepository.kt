package com.example.plandee.data.repository

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import com.example.plandee.data.db.TrafficDatabaseHelper
import com.example.plandee.data.telemetry.UsagePermissionBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.roundToInt

data class TrafficSummary(
    val totalGb: Double,
    val wifiGb: Double,
    val wifiPercent: Int,
    val mobileGb: Double,
    val mobilePercent: Int,
    val avgDailyBurnGb: Double,
    val peakWindow: String,
    val mobileCostEstNaira: Int
)

data class DailyConsumptionBar(
    val day: String,
    val wifiGb: Float,
    val mobileGb: Float
)

data class AppLeaderboardItem(
    val rank: String,
    val name: String,
    val usageGb: String,
    val progress: Float
)

class TrafficRepository(private val context: Context) {

    private val dbHelper = TrafficDatabaseHelper(context.applicationContext)
    private val df = DecimalFormat("#.#")

    private fun getTodayStartTimestamp(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    suspend fun getTrafficSummary(): TrafficSummary = withContext(Dispatchers.IO) {
        var totalBytes = 0L
        var mobileBytes = 0L
        var wifiBytes = 0L

        // 1. Query NetworkStatsManager for Today's Data Transfer (12:00 AM to Now)
        if (UsagePermissionBridge.isUsageAccessGranted(context)) {
            val netStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
            if (netStatsManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val startTime = getTodayStartTimestamp()
                val endTime = System.currentTimeMillis()

                try {
                    val wifiSummary = netStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
                    val nsmWifi = (wifiSummary.rxBytes + wifiSummary.txBytes).coerceAtLeast(0L)

                    val mobileSummary = netStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
                    val nsmMobile = (mobileSummary.rxBytes + mobileSummary.txBytes).coerceAtLeast(0L)

                    if ((nsmWifi + nsmMobile) > 0) {
                        wifiBytes = nsmWifi
                        mobileBytes = nsmMobile
                        totalBytes = nsmWifi + nsmMobile
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Hardware Counter Fallback via TrafficStats
        if (totalBytes == 0L) {
            val sysTotalRx = TrafficStats.getTotalRxBytes()
            val sysTotalTx = TrafficStats.getTotalTxBytes()
            val sysMobileRx = TrafficStats.getMobileRxBytes()
            val sysMobileTx = TrafficStats.getMobileTxBytes()

            if (sysTotalRx != TrafficStats.UNSUPPORTED.toLong() && sysTotalTx != TrafficStats.UNSUPPORTED.toLong()) {
                val calcTotal = (sysTotalRx + sysTotalTx).coerceAtLeast(0L)
                if (calcTotal > 0) {
                    totalBytes = calcTotal
                    if (sysMobileRx != TrafficStats.UNSUPPORTED.toLong() && sysMobileTx != TrafficStats.UNSUPPORTED.toLong()) {
                        mobileBytes = (sysMobileRx + sysMobileTx).coerceAtLeast(0L)
                        wifiBytes = (totalBytes - mobileBytes).coerceAtLeast(0L)
                    }
                }
            }
        }

        // 3. Database Fallback
        if (totalBytes == 0L) {
            wifiBytes = dbHelper.getTotalBytesByNetwork("WIFI")
            mobileBytes = dbHelper.getTotalBytesByNetwork("MOBILE")
            totalBytes = wifiBytes + mobileBytes
        }

        val wifiGb = wifiBytes.toDouble() / (1024 * 1024 * 1024)
        val mobileGb = mobileBytes.toDouble() / (1024 * 1024 * 1024)
        val totalGb = totalBytes.toDouble() / (1024 * 1024 * 1024)

        val wifiPercent = if (totalGb > 0) ((wifiGb / totalGb) * 100).roundToInt() else 50
        val mobilePercent = 100 - wifiPercent

        val avgDailyBurn = (mobileGb / 7.0).coerceAtLeast(0.1)
        val mobileCostEst = (mobileGb * 750).toInt()

        TrafficSummary(
            totalGb = df.format(totalGb).toDouble(),
            wifiGb = df.format(wifiGb).toDouble(),
            wifiPercent = wifiPercent,
            mobileGb = df.format(mobileGb).toDouble(),
            mobilePercent = mobilePercent,
            avgDailyBurnGb = df.format(avgDailyBurn).toDouble(),
            peakWindow = "Night Owl",
            mobileCostEstNaira = mobileCostEst
        )
    }

    suspend fun getDailyConsumption(days: Int = 7): List<DailyConsumptionBar> = withContext(Dispatchers.IO) {
        val summary = getTrafficSummary()
        val mobileGb = summary.mobileGb.toFloat()
        val wifiGb = summary.wifiGb.toFloat()

        val dailyMobileShare = (mobileGb / 7f).coerceAtLeast(0.2f)
        val dailyWifiShare = (wifiGb / 7f).coerceAtLeast(0.4f)

        listOf(
            DailyConsumptionBar("Mon", (dailyWifiShare * 0.9f).coerceAtLeast(0.1f), (dailyMobileShare * 0.8f).coerceAtLeast(0.1f)),
            DailyConsumptionBar("Tue", (dailyWifiShare * 1.2f).coerceAtLeast(0.1f), (dailyMobileShare * 1.1f).coerceAtLeast(0.1f)),
            DailyConsumptionBar("Wed", (dailyWifiShare * 1.0f).coerceAtLeast(0.1f), (dailyMobileShare * 0.9f).coerceAtLeast(0.1f)),
            DailyConsumptionBar("Thu", (dailyWifiShare * 1.3f).coerceAtLeast(0.1f), (dailyMobileShare * 1.4f).coerceAtLeast(0.1f)),
            DailyConsumptionBar("Fri", (dailyWifiShare * 1.5f).coerceAtLeast(0.1f), (dailyMobileShare * 1.5f).coerceAtLeast(0.1f)),
            DailyConsumptionBar("Sat", (dailyWifiShare * 0.7f).coerceAtLeast(0.1f), (dailyMobileShare * 0.8f).coerceAtLeast(0.1f)),
            DailyConsumptionBar("Sun", (dailyWifiShare * 0.4f).coerceAtLeast(0.1f), (dailyMobileShare * 0.5f).coerceAtLeast(0.1f))
        )
    }

    suspend fun getAppLeaderboard(): List<AppLeaderboardItem> = withContext(Dispatchers.IO) {
        val logs = dbHelper.getTopAppUsages()
        if (logs.isEmpty()) {
            return@withContext emptyList()
        }

        val maxBytes = logs.maxOfOrNull { it.totalBytes }?.toFloat() ?: 1f
        logs.mapIndexed { index, appLog ->
            val mb = appLog.totalBytes.toDouble() / (1024 * 1024)
            val gb = mb / 1024
            val usageText = if (gb >= 1.0) "${df.format(gb)} GB used" else "${mb.toInt()} MB used"
            AppLeaderboardItem(
                rank = "#${index + 1}",
                name = appLog.appName,
                usageGb = usageText,
                progress = (appLog.totalBytes.toFloat() / maxBytes).coerceIn(0.15f, 0.95f)
            )
        }
    }

    suspend fun getAllAppsLeaderboard(): List<AppLeaderboardItem> = withContext(Dispatchers.IO) {
        val logs = dbHelper.getAllAppUsages()
        if (logs.isEmpty()) {
            return@withContext emptyList()
        }

        val maxBytes = logs.maxOfOrNull { it.totalBytes }?.toFloat() ?: 1f
        logs.mapIndexed { index, appLog ->
            val mb = appLog.totalBytes.toDouble() / (1024 * 1024)
            val gb = mb / 1024
            val usageText = if (gb >= 1.0) "${df.format(gb)} GB used" else "${mb.toInt()} MB used"
            AppLeaderboardItem(
                rank = "#${index + 1}",
                name = appLog.appName,
                usageGb = usageText,
                progress = (appLog.totalBytes.toFloat() / maxBytes).coerceIn(0.15f, 0.95f)
            )
        }
    }
}
