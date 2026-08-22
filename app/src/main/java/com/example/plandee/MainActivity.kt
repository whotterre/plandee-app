package com.example.plandee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.plandee.data.telemetry.TelemetrySyncWorker
import com.example.plandee.data.telemetry.TrafficMonitor
import com.example.plandee.navigation.PlanDeeNavGraph
import com.example.plandee.ui.theme.PlanDeeTheme

class MainActivity : ComponentActivity() {

    private lateinit var trafficMonitor: TrafficMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        trafficMonitor = TrafficMonitor(this)
        trafficMonitor.startMonitoring()

        TelemetrySyncWorker.schedulePeriodicSync(applicationContext)

        setContent {
            PlanDeeTheme {
                PlanDeeNavGraph()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::trafficMonitor.isInitialized) {
            trafficMonitor.stopMonitoring()
        }
    }
}