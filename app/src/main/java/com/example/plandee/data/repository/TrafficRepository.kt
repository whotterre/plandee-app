package com.example.plandee.data.repository

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
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
    val packageName: String,
    val name: String,
    val usageGb: String,
    val progress: Float,
    val sharePercentText: String,
    val isSystemApp: Boolean,
    val explanationText: String,
    val categoryText: String
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

    private fun getThirtyDayStartTimestamp(): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }
        return cal.timeInMillis
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

    private fun getAppExplanation(packageName: String, appName: String, isSystemApp: Boolean): String {
        val pkgLower = packageName.lowercase()
        val nameLower = appName.lowercase()

        return when {
            pkgLower.contains("youtube") || nameLower.contains("youtube") ->
                "High-bandwidth video streaming app. Consumes ~1.5 GB/hour during HD video playback."
            pkgLower.contains("instagram") || nameLower.contains("instagram") ->
                "Social media video reels and image feed. High background and media streaming data usage."
            pkgLower.contains("tiktok") || nameLower.contains("tiktok") ->
                "Short-form HD video stream app. Consumes significant data while scrolling feed."
            pkgLower.contains("chrome") || nameLower.contains("chrome") ->
                "Web browser. High data usage from media web pages and video streaming."
            pkgLower.contains("whatsapp") || nameLower.contains("whatsapp") ->
                "Messaging & voice call app. Low background data; higher during video calls & status uploads."
            pkgLower.contains("google.android.gms") || pkgLower.contains("gsf") || nameLower.contains("google play services") ->
                "Essential Android system service powering push notifications, Google Play sync, & Location Services."
            isSystemApp ->
                "Android system process running in the background to handle core OS features."
            else ->
                "User-installed application on your device. Tap 'Restrict Background Data' to control data usage."
        }
    }

    private fun checkIfSystemApp(packageName: String): Boolean {
        val pkgLower = packageName.lowercase()

        if (pkgLower.contains("android.gms") ||
            pkgLower.contains("android.gsf") ||
            pkgLower.startsWith("com.android.providers") ||
            pkgLower.startsWith("com.android.systemui") ||
            pkgLower == "android"
        ) {
            return true
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            return false
        }

        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    private fun scanAndFetchGlassWireAppUsages(): List<AppLeaderboardItem> {
        val pm = context.packageManager
        val netStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
        val isUsageGranted = UsagePermissionBridge.isUsageAccessGranted(context)

        val startTime = getThirtyDayStartTimestamp()
        val endTime = System.currentTimeMillis()

        val rawAppBytesMap = mutableMapOf<String, Triple<String, Long, Boolean>>()

        // GlassWire Architecture: High-speed querySummary across all system UIDs
        if (isUsageGranted && netStatsManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val bucket = NetworkStats.Bucket()

                val wifiStats = netStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
                while (wifiStats.hasNextBucket()) {
                    wifiStats.getNextBucket(bucket)
                    val uid = bucket.uid
                    val bytes = bucket.rxBytes + bucket.txBytes
                    if (bytes > 0) {
                        val packages = pm.getPackagesForUid(uid)
                        if (!packages.isNullOrEmpty()) {
                            val pkg = packages[0]
                            val existing = rawAppBytesMap[pkg]?.second ?: 0L
                            val appName = try {
                                val info = pm.getApplicationInfo(pkg, 0)
                                pm.getApplicationLabel(info).toString()
                            } catch (e: Exception) {
                                pkg
                            }
                            val isSys = checkIfSystemApp(pkg)
                            rawAppBytesMap[pkg] = Triple(appName, existing + bytes, isSys)
                        }
                    }
                }
                wifiStats.close()

                val mobileStats = netStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
                while (mobileStats.hasNextBucket()) {
                    mobileStats.getNextBucket(bucket)
                    val uid = bucket.uid
                    val bytes = bucket.rxBytes + bucket.txBytes
                    if (bytes > 0) {
                        val packages = pm.getPackagesForUid(uid)
                        if (!packages.isNullOrEmpty()) {
                            val pkg = packages[0]
                            val existing = rawAppBytesMap[pkg]?.second ?: 0L
                            val appName = try {
                                val info = pm.getApplicationInfo(pkg, 0)
                                pm.getApplicationLabel(info).toString()
                            } catch (e: Exception) {
                                pkg
                            }
                            val isSys = checkIfSystemApp(pkg)
                            rawAppBytesMap[pkg] = Triple(appName, existing + bytes, isSys)
                        }
                    }
                }
                mobileStats.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Hardware TrafficStats Fallback Scanner
        val installedApps = pm.getInstalledApplications(0)
        for (appInfo in installedApps) {
            val uid = appInfo.uid
            val pkgName = appInfo.packageName
            val appName = pm.getApplicationLabel(appInfo).toString()

            val rx = TrafficStats.getUidRxBytes(uid)
            val tx = TrafficStats.getUidTxBytes(uid)

            if (rx != TrafficStats.UNSUPPORTED.toLong() && tx != TrafficStats.UNSUPPORTED.toLong()) {
                val total = (rx + tx).coerceAtLeast(0L)
                if (total > 0 && !rawAppBytesMap.containsKey(pkgName)) {
                    dbHelper.updateOrInsertAppLog(pkgName, appName, rx, tx)
                    val isSys = checkIfSystemApp(pkgName)
                    rawAppBytesMap[pkgName] = Triple(appName, total, isSys)
                }
            }
        }

        val dbLogs = dbHelper.getAllAppUsages()
        for (log in dbLogs) {
            if (!rawAppBytesMap.containsKey(log.packageName) && log.totalBytes > 0) {
                val isSys = checkIfSystemApp(log.packageName)
                rawAppBytesMap[log.packageName] = Triple(log.appName, log.totalBytes, isSys)
            }
        }

        if (rawAppBytesMap.isEmpty()) return emptyList()

        val sortedList = rawAppBytesMap.entries
            .map { (pkg, triple) -> Pair(pkg, triple) }
            .sortedByDescending { it.second.second }

        val totalSumBytes = sortedList.sumOf { it.second.second }.toDouble().coerceAtLeast(1.0)

        return sortedList.mapIndexed { index, entry ->
            val pkgName = entry.first
            val appName = entry.second.first
            val totalBytes = entry.second.second
            val isSys = entry.second.third

            val mb = totalBytes.toDouble() / (1024 * 1024)
            val gb = mb / 1024
            val usageText = if (gb >= 1.0) "${df.format(gb)} GB used" else "${mb.toInt()} MB used"

            val shareRatio = (totalBytes.toDouble() / totalSumBytes).toFloat()
            val sharePercent = (shareRatio * 100).roundToInt()
            val sharePercentText = "$sharePercent% of total app data"

            AppLeaderboardItem(
                rank = "#${index + 1}",
                packageName = pkgName,
                name = appName,
                usageGb = usageText,
                progress = shareRatio.coerceIn(0.04f, 1.0f),
                sharePercentText = sharePercentText,
                isSystemApp = isSys,
                explanationText = getAppExplanation(pkgName, appName, isSys),
                categoryText = if (isSys) "⚙️" else "📱"
            )
        }
    }

    suspend fun getAppLeaderboard(): List<AppLeaderboardItem> = withContext(Dispatchers.IO) {
        val liveApps = scanAndFetchGlassWireAppUsages()
        if (liveApps.isNotEmpty()) {
            return@withContext liveApps.take(5)
        }
        emptyList()
    }

    suspend fun getAllAppsLeaderboard(): List<AppLeaderboardItem> = withContext(Dispatchers.IO) {
        val liveApps = scanAndFetchGlassWireAppUsages()
        if (liveApps.isNotEmpty()) {
            return@withContext liveApps
        }
        emptyList()
    }
}
