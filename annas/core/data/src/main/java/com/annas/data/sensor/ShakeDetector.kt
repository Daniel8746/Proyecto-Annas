package com.annas.data.sensor

import kotlinx.coroutines.flow.SharedFlow

interface ShakeDetector {
    val onShakeEvent: SharedFlow<Unit>
    fun processSensorData(values: FloatArray?)
    fun resetShakeGesture()
}