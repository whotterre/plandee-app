package com.example.plandee.data.repository

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import com.example.plandee.data.db.TrafficDatabaseHelper
import com.example.plandee.data.network.RetrofitClient
import com.example.plandee.data.network.TelemetryIngestionRequest
import com.example.plandee.data.network.UsageHistoryPayload
import com.example.plandee.data.telemetry.UsagePermissionBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

data class MonthlyTimelineBar(
    val dateMillis: Long,
    val dayLabel: String,      // e.g. "Mon", "Tue"
    val dateLabel: String,     // e.g. "14 Aug", "15 Aug"
    val wifiGb: Float,
    val mobileGb: Float,
    val totalGb: Float,
    val isToday: Boolean
)

data class AppLeaderboardItem(
    val rank: String,
    val packageName: String,
    val name: String,
    val usageGb: String,
    val progress: Float,
    val sharePercentText: String,
    val isSystemApp: Boolean,
    val explanationText: String,
    val categoryText: String,
    val totalBytes: Long
)

class TrafficRepository(private val context: Context) {

    private val dbHelper = TrafficDatabaseHelper(context.applicationContext)
    private val df = DecimalFormat("#.#")
    private val dayFormat = SimpleDateFormat("EEE", Locale.US)
    private val dateFormat = SimpleDateFormat("dd MMM", Locale.US)

    private fun getTodayStartTimestamp(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getThirtyDayStartTimestamp(): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }
        return cal.timeInMillis
    }

    private fun calculateDynamicPeakWindow(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 23..24, in 0..5 -> "Night Owl (11PM-6AM)"
            in 6..11 -> "Morning Rush (6AM-12PM)"
            in 12..17 -> "Afternoon Peak (12PM-6PM)"
            else -> "Evening Prime (6PM-11PM)"
        }
    }

    suspend fun syncTelemetryToGoBackend(): Boolean = withContext(Dispatchers.IO) {
        try {
            val activeApps = scanAndFetchGlassWireAppUsages()
            if (activeApps.isEmpty()) return@withContext false

            val now = System.currentTimeMillis()
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val startTimeStr = isoFormat.format(Date(getThirtyDayStartTimestamp()))
            val endTimeStr = isoFormat.format(Date(now))

            val payloads = activeApps.map { item ->
                UsageHistoryPayload(
                    connectionType = "MOBILE",
                    networkCarrier = "MTN",
                    appName = item.name,
                    appPackageName = item.packageName,
                    bytesUsed = item.totalBytes,
                    startTime = startTimeStr,
                    endTime = endTimeStr
                )
            }

            val request = TelemetryIngestionRequest(data = payloads)
            val apiService = RetrofitClient.getApiService(context)
            val response = apiService.syncTelemetry(request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getTrafficSummary(): TrafficSummary = withContext(Dispatchers.IO) {
        var totalBytes = 0L
        var mobileBytes = 0L
        var wifiBytes = 0L

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

        val avgDailyBurn = (totalGb / 30.0).coerceAtLeast(0.1)
        val mobileCostEst = (mobileGb * 750).toInt()
        val peak = calculateDynamicPeakWindow()

        TrafficSummary(
            totalGb = df.format(totalGb).toDouble(),
            wifiGb = df.format(wifiGb).toDouble(),
            wifiPercent = wifiPercent,
            mobileGb = df.format(mobileGb).toDouble(),
            mobilePercent = mobilePercent,
            avgDailyBurnGb = df.format(avgDailyBurn).toDouble(),
            peakWindow = peak,
            mobileCostEstNaira = mobileCostEst
        )
    }

    suspend fun getDailyConsumption(days: Int = 7): List<DailyConsumptionBar> = withContext(Dispatchers.IO) {
        val netStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
        val isUsageGranted = UsagePermissionBridge.isUsageAccessGranted(context)
        val bars = mutableListOf<DailyConsumptionBar>()

        for (i in (days - 1) downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val startTime = dayCal.timeInMillis
            val endCal = dayCal.clone() as Calendar
            endCal.set(Calendar.HOUR_OF_DAY, 23)
            endCal.set(Calendar.MINUTE, 59)
            endCal.set(Calendar.SECOND, 59)
            val endTime = endCal.timeInMillis

            var dayWifiBytes = 0L
            var dayMobileBytes = 0L

            if (isUsageGranted && netStatsManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val wBucket = netStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
                    dayWifiBytes = (wBucket.rxBytes + wBucket.txBytes).coerceAtLeast(0L)

                    val mBucket = netStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
                    dayMobileBytes = (mBucket.rxBytes + mBucket.txBytes).coerceAtLeast(0L)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (dayWifiBytes == 0L && dayMobileBytes == 0L) {
                dayWifiBytes = dbHelper.getBytesByRangeAndNetwork(startTime, endTime, "WIFI")
                dayMobileBytes = dbHelper.getBytesByRangeAndNetwork(startTime, endTime, "MOBILE")
            }

            val wifiF = (dayWifiBytes.toDouble() / (1024 * 1024 * 1024)).toFloat()
            val mobileF = (dayMobileBytes.toDouble() / (1024 * 1024 * 1024)).toFloat()

            bars.add(
                DailyConsumptionBar(
                    day = dayFormat.format(dayCal.time),
                    wifiGb = wifiF,
                    mobileGb = mobileF
                )
            )
        }
        bars
    }

    suspend fun getMonthlyTimelineConsumption(days: Int = 30): List<MonthlyTimelineBar> = withContext(Dispatchers.IO) {
        val netStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
        val isUsageGranted = UsagePermissionBridge.isUsageAccessGranted(context)

        val cal = Calendar.getInstance()
        val bars = mutableListOf<MonthlyTimelineBar>()

        for (i in (days - 1) downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val startTime = dayCal.timeInMillis
            val endCal = dayCal.clone() as Calendar
            endCal.set(Calendar.HOUR_OF_DAY, 23)
            endCal.set(Calendar.MINUTE, 59)
            endCal.set(Calendar.SECOND, 59)
            val endTime = endCal.timeInMillis

            var dayWifiBytes = 0L
            var dayMobileBytes = 0L

            if (isUsageGranted && netStatsManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val wBucket = netStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
                    dayWifiBytes = (wBucket.rxBytes + wBucket.txBytes).coerceAtLeast(0L)

                    val mBucket = netStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
                    dayMobileBytes = (mBucket.rxBytes + mBucket.txBytes).coerceAtLeast(0L)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (dayWifiBytes == 0L && dayMobileBytes == 0L) {
                dayWifiBytes = dbHelper.getBytesByRangeAndNetwork(startTime, endTime, "WIFI")
                dayMobileBytes = dbHelper.getBytesByRangeAndNetwork(startTime, endTime, "MOBILE")
            }

            val wifiF = (dayWifiBytes.toDouble() / (1024 * 1024 * 1024)).toFloat()
            val mobileF = (dayMobileBytes.toDouble() / (1024 * 1024 * 1024)).toFloat()
            val totalF = wifiF + mobileF

            bars.add(
                MonthlyTimelineBar(
                    dateMillis = startTime,
                    dayLabel = dayFormat.format(dayCal.time),
                    dateLabel = dateFormat.format(dayCal.time),
                    wifiGb = wifiF,
                    mobileGb = mobileF,
                    totalGb = totalF,
                    isToday = (i == 0)
                )
            )
        }
        bars
    }

    suspend fun getAppLeaderboard(): List<AppLeaderboardItem> = withContext(Dispatchers.IO) {
        val todayStart = getTodayStartTimestamp()
        val now = System.currentTimeMillis()
        val list = getAppLeaderboardForDayRange(todayStart, now)
        list.take(5)
    }

    suspend fun getAllAppsLeaderboard(): List<AppLeaderboardItem> = withContext(Dispatchers.IO) {
        val todayStart = getTodayStartTimestamp()
        val now = System.currentTimeMillis()
        getAppLeaderboardForDayRange(todayStart, now)
    }

    suspend fun getAppLeaderboardForDayRange(startTimeMillis: Long, endTimeMillis: Long): List<AppLeaderboardItem> = withContext(Dispatchers.IO) {
        val combinedAppMap = mutableMapOf<String, GlassWireAppUsage>()

        if (UsagePermissionBridge.isUsageAccessGranted(context)) {
            val netStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
            val packageManager = context.packageManager

            if (netStatsManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val installedApps = packageManager.getInstalledApplications(0)
                val uidMap = mutableMapOf<Int, MutableList<ApplicationInfo>>()

                for (app in installedApps) {
                    uidMap.getOrPut(app.uid) { mutableListOf() }.add(app)
                }

                for ((uid, apps) in uidMap) {
                    var uidMobileBytes = 0L
                    var uidWifiBytes = 0L

                    try {
                        val mobileStats = netStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, null, startTimeMillis, endTimeMillis, uid)
                        while (mobileStats.hasNextBucket()) {
                            val bucket = NetworkStats.Bucket()
                            mobileStats.getNextBucket(bucket)
                            uidMobileBytes += (bucket.rxBytes + bucket.txBytes)
                        }
                        mobileStats.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        val wifiStats = netStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_WIFI, null, startTimeMillis, endTimeMillis, uid)
                        while (wifiStats.hasNextBucket()) {
                            val bucket = NetworkStats.Bucket()
                            wifiStats.getNextBucket(bucket)
                            uidWifiBytes += (bucket.rxBytes + bucket.txBytes)
                        }
                        wifiStats.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val totalUidBytes = (uidMobileBytes + uidWifiBytes).coerceAtLeast(0L)
                    if (totalUidBytes > 0) {
                        val primaryApp = apps.first()
                        val appName = try {
                            packageManager.getApplicationLabel(primaryApp).toString()
                        } catch (e: Exception) {
                            primaryApp.packageName
                        }

                        val isSys = (primaryApp.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        combinedAppMap[primaryApp.packageName] = GlassWireAppUsage(
                            name = appName,
                            packageName = primaryApp.packageName,
                            totalBytes = totalUidBytes,
                            isSystemApp = isSys
                        )
                    }
                }
            }
        }

        // ALWAYS MERGE SQLite Database logs (captures Telegram, YouTube, WhatsApp delta traffic recorded by TrafficMonitor)
        val dbUsages = dbHelper.getAppUsageSummaryByRange(startTimeMillis, endTimeMillis)
        for ((pkg, bytes) in dbUsages) {
            if (bytes > 0) {
                val existing = combinedAppMap[pkg]
                val appName = try {
                    val info = context.packageManager.getApplicationInfo(pkg, 0)
                    context.packageManager.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    existing?.name ?: pkg.substringAfterLast('.')
                }
                val isSys = try {
                    val info = context.packageManager.getApplicationInfo(pkg, 0)
                    (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (e: Exception) {
                    existing?.isSystemApp ?: false
                }
                val bestBytes = Math.max(bytes, existing?.totalBytes ?: 0L)
                combinedAppMap[pkg] = GlassWireAppUsage(
                    name = appName,
                    packageName = pkg,
                    totalBytes = bestBytes,
                    isSystemApp = isSys
                )
            }
        }

        val appUsages = combinedAppMap.values.toList()
        val totalAllBytes = appUsages.sumOf { it.totalBytes }.coerceAtLeast(1L)
        val maxAppBytes = appUsages.maxOfOrNull { it.totalBytes } ?: 1L

        val sortedUsages = appUsages.sortedByDescending { it.totalBytes }

        val userAppsList = mutableListOf<AppLeaderboardItem>()
        var systemAppsTotalBytes = 0L
        var systemAppsCount = 0

        sortedUsages.forEach { item ->
            if (item.isSystemApp) {
                systemAppsTotalBytes += item.totalBytes
                systemAppsCount++
            } else {
                val gbVal = item.totalBytes.toDouble() / (1024 * 1024 * 1024)
                val mbVal = item.totalBytes.toDouble() / (1024 * 1024)
                val kbVal = item.totalBytes.toDouble() / 1024

                val usageGbStr = when {
                    gbVal >= 0.1 -> "${df.format(gbVal)} GB"
                    mbVal >= 0.1 -> "${df.format(mbVal)} MB"
                    else -> "${df.format(kbVal)} KB"
                }

                val progress = (item.totalBytes.toFloat() / maxAppBytes.toFloat()).coerceIn(0.05f, 1.0f)
                val sharePct = ((item.totalBytes.toDouble() / totalAllBytes.toDouble()) * 100).roundToInt()

                userAppsList.add(
                    AppLeaderboardItem(
                        rank = "",
                        packageName = item.packageName,
                        name = item.name,
                        usageGb = usageGbStr,
                        progress = progress,
                        sharePercentText = "$sharePct%",
                        isSystemApp = false,
                        explanationText = "Active data consumption.",
                        categoryText = "Application",
                        totalBytes = item.totalBytes
                    )
                )
            }
        }

        if (systemAppsTotalBytes > 0) {
            val gbVal = systemAppsTotalBytes.toDouble() / (1024 * 1024 * 1024)
            val mbVal = systemAppsTotalBytes.toDouble() / (1024 * 1024)
            val kbVal = systemAppsTotalBytes.toDouble() / 1024

            val usageGbStr = when {
                gbVal >= 0.1 -> "${df.format(gbVal)} GB"
                mbVal >= 0.1 -> "${df.format(mbVal)} MB"
                else -> "${df.format(kbVal)} KB"
            }

            val progress = (systemAppsTotalBytes.toFloat() / maxAppBytes.toFloat()).coerceIn(0.05f, 1.0f)
            val sharePct = ((systemAppsTotalBytes.toDouble() / totalAllBytes.toDouble()) * 100).roundToInt()

            userAppsList.add(
                AppLeaderboardItem(
                    rank = "",
                    packageName = "com.android.system.grouped",
                    name = "System Apps Usage",
                    usageGb = usageGbStr,
                    progress = progress,
                    sharePercentText = "$sharePct%",
                    isSystemApp = true,
                    explanationText = "Combined total of $systemAppsCount background system services.",
                    categoryText = "System Apps Usage",
                    totalBytes = systemAppsTotalBytes
                )
            )
        }

        val finalList = userAppsList.sortedByDescending { it.totalBytes }
        finalList.mapIndexed { index, item ->
            item.copy(rank = "#${index + 1}")
        }
    }

    private fun scanAndFetchGlassWireAppUsages(): List<GlassWireAppUsage> {
        val list = mutableListOf<GlassWireAppUsage>()
        if (UsagePermissionBridge.isUsageAccessGranted(context)) {
            val netStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
            val pm = context.packageManager
            if (netStatsManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val startTime = getThirtyDayStartTimestamp()
                val endTime = System.currentTimeMillis()

                val apps = pm.getInstalledApplications(0)
                for (app in apps) {
                    var total = 0L
                    try {
                        val mStats = netStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime, app.uid)
                        while (mStats.hasNextBucket()) {
                            val b = NetworkStats.Bucket()
                            mStats.getNextBucket(b)
                            total += (b.rxBytes + b.txBytes)
                        }
                        mStats.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (total > 0) {
                        val name = try { pm.getApplicationLabel(app).toString() } catch (e: Exception) { app.packageName }
                        val isSys = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        list.add(GlassWireAppUsage(name, app.packageName, total, isSys))
                    }
                }
            }
        }
        return list
    }
}

private data class GlassWireAppUsage(
    val name: String,
    val packageName: String,
    val totalBytes: Long,
    val isSystemApp: Boolean
)
