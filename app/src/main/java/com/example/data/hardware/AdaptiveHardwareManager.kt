package com.example.data.hardware

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HardwareState(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val isLowRamDevice: Boolean = false,
    val enableLiveGraphPhysics: Boolean = true,
    val targetFps: Int = 60,
    val maxSimulationStepsPerFrame: Int = 2,
    val description: String = "Full Performance (60 FPS Physics)"
)

object AdaptiveHardwareManager {

    private val _hardwareState = MutableStateFlow(HardwareState())
    val hardwareState: StateFlow<HardwareState> = _hardwareState.asStateFlow()

    fun init(context: Context) {
        refreshHardwareState(context)
    }

    fun refreshHardwareState(context: Context) {
        try {
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, batteryFilter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPercent = if (level >= 0 && scale > 0) (level * 100) / scale else 85

            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isPowerSaveMode = powerManager?.isPowerSaveMode ?: false

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val isLowRamDevice = activityManager?.isLowRamDevice ?: false

            // Adaptive decision
            val (enableLivePhysics, fps, steps, desc) = when {
                isPowerSaveMode -> {
                    Quadruple(true, 30, 1, "Battery Saver Active (30 FPS Throttled)")
                }
                batteryPercent < 15 && !isCharging -> {
                    Quadruple(false, 15, 1, "Critical Battery (<15%): Static Physics Layout")
                }
                isLowRamDevice -> {
                    Quadruple(true, 30, 1, "Low RAM Device: Optimized 30 FPS Physics")
                }
                else -> {
                    Quadruple(true, 60, 2, "High Performance: 60 FPS Continuous Simulation")
                }
            }

            _hardwareState.value = HardwareState(
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                isPowerSaveMode = isPowerSaveMode,
                isLowRamDevice = isLowRamDevice,
                enableLiveGraphPhysics = enableLivePhysics,
                targetFps = fps,
                maxSimulationStepsPerFrame = steps,
                description = desc
            )
        } catch (_: Exception) {
            _hardwareState.value = HardwareState()
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
