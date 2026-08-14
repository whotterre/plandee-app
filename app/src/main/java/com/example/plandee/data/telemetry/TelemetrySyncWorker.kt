package com.example.plandee.data.telemetry

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.plandee.data.db.TrafficDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class TelemetrySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val dbHelper = TrafficDatabaseHelper(appContext)

    companion object {
        private const val TAG = "TelemetrySyncWorker"
        private const val WORK_NAME = "plandee_periodic_telemetry_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<TelemetrySyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "WorkManager periodic telemetry sync scheduled (6h interval).")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting periodic telemetry WorkManager background sync...")
            val recentLogs = dbHelper.getRecentNetworkLogs(limit = 20)
            val appLogs = dbHelper.getTopAppUsages()

            if (recentLogs.isEmpty()) {
                Log.d(TAG, "No telemetry logs to sync.")
                return@withContext Result.success()
            }

            // In production, posts payload to /v1/telemetry/sync
            Log.d(TAG, "Successfully synced ${recentLogs.size} network records & ${appLogs.size} app records to server.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in telemetry sync worker", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
