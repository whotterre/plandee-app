package com.example.plandee.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class NetworkLogRecord(
    val id: Long = 0,
    val timestamp: Long,
    val networkType: String,
    val rxBytes: Long,
    val txBytes: Long,
    val totalBytes: Long,
    val sessionDeltaBytes: Long
)

data class AppLogRecord(
    val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val rxBytes: Long,
    val txBytes: Long,
    val totalBytes: Long
)

class TrafficDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "plandee_traffic.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_NETWORK = "network_usage_logs"
        const val TABLE_APP = "app_usage_logs"

        const val COL_ID = "id"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_NETWORK_TYPE = "network_type"
        const val COL_RX_BYTES = "rx_bytes"
        const val COL_TX_BYTES = "tx_bytes"
        const val COL_TOTAL_BYTES = "total_bytes"
        const val COL_DELTA_BYTES = "session_delta_bytes"

        const val COL_PKG_NAME = "package_name"
        const val COL_APP_NAME = "app_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createNetworkTable = """
            CREATE TABLE $TABLE_NETWORK (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_NETWORK_TYPE TEXT NOT NULL,
                $COL_RX_BYTES INTEGER NOT NULL,
                $COL_TX_BYTES INTEGER NOT NULL,
                $COL_TOTAL_BYTES INTEGER NOT NULL,
                $COL_DELTA_BYTES INTEGER NOT NULL
            )
        """.trimIndent()

        val createAppTable = """
            CREATE TABLE $TABLE_APP (
                $COL_PKG_NAME TEXT PRIMARY KEY,
                $COL_APP_NAME TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_RX_BYTES INTEGER NOT NULL,
                $COL_TX_BYTES INTEGER NOT NULL,
                $COL_TOTAL_BYTES INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createNetworkTable)
        db.execSQL(createAppTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NETWORK")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_APP")
        onCreate(db)
    }

    fun clearAllData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NETWORK")
        db.execSQL("DELETE FROM $TABLE_APP")
    }

    fun insertNetworkLog(
        networkType: String,
        rxBytes: Long,
        txBytes: Long,
        sessionDeltaBytes: Long
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TIMESTAMP, System.currentTimeMillis())
            put(COL_NETWORK_TYPE, networkType)
            put(COL_RX_BYTES, rxBytes)
            put(COL_TX_BYTES, txBytes)
            put(COL_TOTAL_BYTES, rxBytes + txBytes)
            put(COL_DELTA_BYTES, sessionDeltaBytes)
        }
        return db.insert(TABLE_NETWORK, null, values)
    }

    fun updateOrInsertAppLog(
        packageName: String,
        appName: String,
        rxBytes: Long,
        txBytes: Long
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PKG_NAME, packageName)
            put(COL_APP_NAME, appName)
            put(COL_TIMESTAMP, System.currentTimeMillis())
            put(COL_RX_BYTES, rxBytes)
            put(COL_TX_BYTES, txBytes)
            put(COL_TOTAL_BYTES, rxBytes + txBytes)
        }
        return db.insertWithOnConflict(TABLE_APP, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getRecentNetworkLogs(limit: Int = 50): List<NetworkLogRecord> {
        val list = mutableListOf<NetworkLogRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NETWORK,
            null, null, null, null, null,
            "$COL_TIMESTAMP DESC",
            limit.toString()
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    NetworkLogRecord(
                        id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                        timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP)),
                        networkType = c.getString(c.getColumnIndexOrThrow(COL_NETWORK_TYPE)),
                        rxBytes = c.getLong(c.getColumnIndexOrThrow(COL_RX_BYTES)),
                        txBytes = c.getLong(c.getColumnIndexOrThrow(COL_TX_BYTES)),
                        totalBytes = c.getLong(c.getColumnIndexOrThrow(COL_TOTAL_BYTES)),
                        sessionDeltaBytes = c.getLong(c.getColumnIndexOrThrow(COL_DELTA_BYTES))
                    )
                )
            }
        }
        return list
    }

    fun getTotalBytesByNetwork(networkType: String): Long {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_DELTA_BYTES) FROM $TABLE_NETWORK WHERE $COL_NETWORK_TYPE = ?",
            arrayOf(networkType)
        )
        var total = 0L
        cursor.use { c ->
            if (c.moveToFirst()) {
                total = c.getLong(0)
            }
        }
        return total
    }

    fun getBytesByRangeAndNetwork(startTime: Long, endTime: Long, networkType: String): Long {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_DELTA_BYTES) FROM $TABLE_NETWORK WHERE $COL_NETWORK_TYPE = ? AND $COL_TIMESTAMP >= ? AND $COL_TIMESTAMP <= ?",
            arrayOf(networkType, startTime.toString(), endTime.toString())
        )
        var total = 0L
        cursor.use { c ->
            if (c.moveToFirst()) {
                total = c.getLong(0)
            }
        }
        return total
    }

    fun hasNetworkLogs(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NETWORK", null)
        var count = 0
        cursor.use { c ->
            if (c.moveToFirst()) {
                count = c.getInt(0)
            }
        }
        return count > 0
    }

    fun getTopAppUsages(): List<AppLogRecord> {
        val list = mutableListOf<AppLogRecord>()
        val db = readableDatabase
        val query = """
            SELECT $COL_PKG_NAME, $COL_APP_NAME, $COL_RX_BYTES, $COL_TX_BYTES, $COL_TOTAL_BYTES
            FROM $TABLE_APP
            WHERE $COL_TOTAL_BYTES > 0
            ORDER BY $COL_TOTAL_BYTES DESC
            LIMIT 5
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val total = c.getLong(c.getColumnIndexOrThrow(COL_TOTAL_BYTES))
                list.add(
                    AppLogRecord(
                        timestamp = System.currentTimeMillis(),
                        packageName = c.getString(c.getColumnIndexOrThrow(COL_PKG_NAME)),
                        appName = c.getString(c.getColumnIndexOrThrow(COL_APP_NAME)),
                        rxBytes = c.getLong(c.getColumnIndexOrThrow(COL_RX_BYTES)),
                        txBytes = c.getLong(c.getColumnIndexOrThrow(COL_TX_BYTES)),
                        totalBytes = total
                    )
                )
            }
        }
        return list
    }

    fun getAllAppUsages(): List<AppLogRecord> {
        val list = mutableListOf<AppLogRecord>()
        val db = readableDatabase
        val query = """
            SELECT $COL_PKG_NAME, $COL_APP_NAME, $COL_RX_BYTES, $COL_TX_BYTES, $COL_TOTAL_BYTES
            FROM $TABLE_APP
            WHERE $COL_TOTAL_BYTES > 0
            ORDER BY $COL_TOTAL_BYTES DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val total = c.getLong(c.getColumnIndexOrThrow(COL_TOTAL_BYTES))
                list.add(
                    AppLogRecord(
                        timestamp = System.currentTimeMillis(),
                        packageName = c.getString(c.getColumnIndexOrThrow(COL_PKG_NAME)),
                        appName = c.getString(c.getColumnIndexOrThrow(COL_APP_NAME)),
                        rxBytes = c.getLong(c.getColumnIndexOrThrow(COL_RX_BYTES)),
                        txBytes = c.getLong(c.getColumnIndexOrThrow(COL_TX_BYTES)),
                        totalBytes = total
                    )
                )
            }
        }
        return list
    }

    fun getAppUsageSummaryByRange(startTime: Long, endTime: Long): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_APP,
            arrayOf(COL_PKG_NAME, COL_TOTAL_BYTES),
            "$COL_TIMESTAMP >= ? AND $COL_TIMESTAMP <= ?",
            arrayOf(startTime.toString(), endTime.toString()),
            null, null, null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val pkg = c.getString(0)
                val bytes = c.getLong(1)
                map[pkg] = bytes
            }
        }
        return map
    }
}
