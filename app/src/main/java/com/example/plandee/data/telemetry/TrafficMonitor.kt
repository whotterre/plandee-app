package com.example.plandee.data.telemetry

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.plandee.R
import com.example.plandee.data.db.TrafficDatabaseHelper
import com.example.plandee.data.security.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.DecimalFormat
import java.util.Calendar

sealed class NetworkEvent {
    data class Connected(val source: String) : NetworkEvent()
    data class Disconnected(val source: String, val sessionMb: Double) : NetworkEvent()
}

class TrafficMonitor(private val context: Context) {

    private val appContext = context.applicationContext
    private val dbHelper = TrafficDatabaseHelper(appContext)
    private val sessionManager = SessionManager.getInstance(appContext)
    private val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val networkStatsManager = appContext.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager

    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var sessionStartTotalBytes = 0L
    private var activeSessionSource = "Mobile Data"

    private var sessionMobileBytesSpent = 0L
    private var lastNotifiedMbMilestone = 0

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val _networkEventFlow = MutableSharedFlow<NetworkEvent>(extraBufferCapacity = 10)
    val networkEventFlow: SharedFlow<NetworkEvent> = _networkEventFlow.asSharedFlow()

    var onDataUpdatedListener: (() -> Unit)? = null
    private val df = DecimalFormat("#.#")

    companion object {
        private const val TAG = "TrafficMonitor"
        private const val CHANNEL_ID = "plandee_telemetry_channel"
        private const val NOTIFICATION_ID_CONNECT = 1001
        private const val NOTIFICATION_ID_DISCONNECT = 1002

        var instance: TrafficMonitor? = null
            private set

        fun getInstance(context: Context): TrafficMonitor {
            return instance ?: synchronized(this) {
                instance ?: TrafficMonitor(context).also { instance = it }
            }
        }
    }

    init {
        instance = this
        createNotificationChannel()
        DataUsageNotificationService.createNotificationChannel(appContext)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Plan Dee Data Telemetry Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when internet data connects or disconnects with session metrics."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun postStatusBarNotification(title: String, body: String, notificationId: Int) {
        try {
            val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error posting notification", e)
        }
    }

    fun startMonitoring() {
        if (isDataOnAnySource()) {
            val netType = determineActiveNetworkType()
            activeSessionSource = if (netType == "WIFI") "Wi-Fi" else "Mobile Data"
            sessionStartTotalBytes = getCurrentTotalBytes()
            _networkEventFlow.tryEmit(NetworkEvent.Connected(activeSessionSource))
            postStatusBarNotification("Plan Dee: Data Turned ON", "Connected via $activeSessionSource. Real-time telemetry active.", NOTIFICATION_ID_CONNECT)
            scanInstalledAppsTraffic()
            sampleCurrentTraffic(netType)
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!isDataOnAnySource()) return

                val caps = connectivityManager.getNetworkCapabilities(network)
                val netType = when {
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WIFI"
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "MOBILE"
                    else -> "WIFI"
                }

                activeSessionSource = if (netType == "WIFI") "Wi-Fi" else "Mobile Data"
                sessionStartTotalBytes = getCurrentTotalBytes()

                Log.d(TAG, "Network AVAILABLE: $activeSessionSource")
                _networkEventFlow.tryEmit(NetworkEvent.Connected(activeSessionSource))
                postStatusBarNotification("Plan Dee: Data Turned ON", "Connected via $activeSessionSource. Real-time telemetry active.", NOTIFICATION_ID_CONNECT)

                sampleCurrentTraffic(netType)
                scanInstalledAppsTraffic()
            }

            override fun onLost(network: Network) {
                val currentTotal = getCurrentTotalBytes()
                val sessionBytes = (currentTotal - sessionStartTotalBytes).coerceAtLeast(0L)
                val sessionMb = sessionBytes.toDouble() / (1024 * 1024)

                Log.d(TAG, "Network LOST ($activeSessionSource). Session MB used: $sessionMb")
                _networkEventFlow.tryEmit(NetworkEvent.Disconnected(activeSessionSource, sessionMb))
                postStatusBarNotification("Plan Dee: Data Turned OFF", "Session ended on $activeSessionSource: ${df.format(sessionMb)} MB transferred.", NOTIFICATION_ID_DISCONNECT)

                if (isDataOnAnySource()) {
                    sampleCurrentTraffic("MOBILE")
                }
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister network callback", e)
            }
        }
    }

    fun isDataOnAnySource(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val hasWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        return hasInternet && (hasWifi || hasCellular)
    }

    fun forceSampling(networkType: String? = null): Long {
        val type = networkType ?: determineActiveNetworkType()
        val delta = sampleCurrentTraffic(type, isManualTrigger = true)
        scanInstalledAppsTraffic()
        onDataUpdatedListener?.invoke()
        return delta
    }

    private fun getThirtyDayStartTimestamp(): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }
        return cal.timeInMillis
    }

    fun scanInstalledAppsTraffic() {
        try {
            val pm = appContext.packageManager
            val isUsageGranted = UsagePermissionBridge.isUsageAccessGranted(appContext)

            val startTime = getThirtyDayStartTimestamp()
            val endTime = System.currentTimeMillis()

            val uidTrafficMap = mutableMapOf<Int, Long>()

            if (isUsageGranted && networkStatsManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val bucket = NetworkStats.Bucket()

                    val wifiStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
                    while (wifiStats.hasNextBucket()) {
                        wifiStats.getNextBucket(bucket)
                        val uid = bucket.uid
                        val bytes = bucket.rxBytes + bucket.txBytes
                        if (bytes > 0) {
                            uidTrafficMap[uid] = (uidTrafficMap[uid] ?: 0L) + bytes
                        }
                    }
                    wifiStats.close()

                    val mobileStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
                    while (mobileStats.hasNextBucket()) {
                        mobileStats.getNextBucket(bucket)
                        val uid = bucket.uid
                        val bytes = bucket.rxBytes + bucket.txBytes
                        if (bytes > 0) {
                            uidTrafficMap[uid] = (uidTrafficMap[uid] ?: 0L) + bytes
                        }
                    }
                    mobileStats.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error running querySummary across UIDs", e)
                }
            }

            for ((uid, totalBytes) in uidTrafficMap) {
                val packages = pm.getPackagesForUid(uid)
                if (!packages.isNullOrEmpty()) {
                    val pkgName = packages[0]
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(pkgName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        pkgName
                    }
                    dbHelper.updateOrInsertAppLog(pkgName, appName, totalBytes / 2, totalBytes / 2)
                }
            }

            val installedApps = pm.getInstalledApplications(0)
            for (appInfo in installedApps) {
                val uid = appInfo.uid
                val pkgName = appInfo.packageName
                val appName = pm.getApplicationLabel(appInfo).toString()

                val rx = TrafficStats.getUidRxBytes(uid)
                val tx = TrafficStats.getUidTxBytes(uid)

                if (rx != TrafficStats.UNSUPPORTED.toLong() && tx != TrafficStats.UNSUPPORTED.toLong() && (rx + tx) > 0) {
                    dbHelper.updateOrInsertAppLog(pkgName, appName, rx, tx)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning app traffic", e)
        }
    }

    fun sampleCurrentTraffic(detectedNetworkType: String, isManualTrigger: Boolean = false): Long {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()

        val rx = if (currentRx != TrafficStats.UNSUPPORTED.toLong()) currentRx else 0L
        val tx = if (currentTx != TrafficStats.UNSUPPORTED.toLong()) currentTx else 0L

        var deltaBytes = 0L
        if (lastTotalRxBytes > 0 && lastTotalTxBytes > 0) {
            val deltaRx = (rx - lastTotalRxBytes).coerceAtLeast(0L)
            val deltaTx = (tx - lastTotalTxBytes).coerceAtLeast(0L)
            deltaBytes = deltaRx + deltaTx
        }

        lastTotalRxBytes = if (rx > 0) rx else 0L
        lastTotalTxBytes = if (tx > 0) tx else 0L

        val activeType = if (detectedNetworkType == "INITIAL_CONNECT" || detectedNetworkType == "DISCONNECTED") {
            determineActiveNetworkType()
        } else {
            detectedNetworkType
        }

        if (deltaBytes > 0) {
            dbHelper.insertNetworkLog(
                networkType = activeType,
                rxBytes = rx,
                txBytes = tx,
                sessionDeltaBytes = deltaBytes
            )

            if (activeType == "MOBILE") {
                sessionMobileBytesSpent += deltaBytes
                val mbSpent = sessionMobileBytesSpent.toDouble() / (1024 * 1024)
                val targetAlertMb = sessionManager.getCustomDataAlertMb().toDouble()
                if (targetAlertMb > 0) {
                    val milestone = (mbSpent / targetAlertMb).toInt()
                    if (milestone > lastNotifiedMbMilestone && milestone > 0) {
                        lastNotifiedMbMilestone = milestone
                        DataUsageNotificationService.send500MbSpendNotification(appContext, milestone * targetAlertMb)
                    }
                }
            }

            Log.d(TAG, "Real-time Traffic Stats -> Type: $activeType, Delta: ${deltaBytes / 1024} KB")
            onDataUpdatedListener?.invoke()
        }

        return deltaBytes
    }

    private fun getCurrentTotalBytes(): Long {
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        val totalRx = if (rx != TrafficStats.UNSUPPORTED.toLong()) rx else 0L
        val totalTx = if (tx != TrafficStats.UNSUPPORTED.toLong()) tx else 0L
        return totalRx + totalTx
    }

    private fun determineActiveNetworkType(): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return "WIFI"
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "WIFI"
        return if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) "MOBILE" else "WIFI"
    }
}
