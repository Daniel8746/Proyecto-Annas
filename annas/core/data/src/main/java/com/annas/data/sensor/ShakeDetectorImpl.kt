package com.annas.data.sensor

import android.os.SystemClock
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class ShakeDetectorImpl @Inject constructor() : ShakeDetector {
    private val _onShakeEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val onShakeEvent: SharedFlow<Unit> = _onShakeEvent.asSharedFlow()

    private var hasBaseline = false
    private var gravityX = 0f

    private var lastUpdate: Long = 0
    private var lastDirectionChangeAt: Long = 0
    private var lastEventAt: Long = 0
    private var lastDirection = 0
    private var directionChanges = 0

    override fun processSensorData(values: FloatArray?) {
        if (values == null || values.isEmpty()) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastUpdate < MIN_SAMPLE_INTERVAL_MS) return
        lastUpdate = now

        val x = values[0]
        if (!hasBaseline) {
            gravityX = x
            hasBaseline = true
            return
        }

        gravityX = GRAVITY_FILTER_ALPHA * gravityX + (1f - GRAVITY_FILTER_ALPHA) * x
        val linearX = x - gravityX

        val direction = when {
            linearX > AXIS_THRESHOLD -> 1
            linearX < -AXIS_THRESHOLD -> -1
            else -> {
                resetIfGestureExpired(now)
                return
            }
        }

        if (now - lastEventAt < EVENT_COOLDOWN_MS) return

        if (lastDirection == 0) {
            lastDirection = direction
            lastDirectionChangeAt = now
            directionChanges = 1
            return
        }

        if (direction == lastDirection) {
            resetIfGestureExpired(now)
            return
        }

        val changeInterval = now - lastDirectionChangeAt
        if (changeInterval < MIN_DIRECTION_INTERVAL_MS) return

        directionChanges = if (changeInterval <= MAX_DIRECTION_INTERVAL_MS) {
            directionChanges + 1
        } else {
            1
        }
        lastDirection = direction
        lastDirectionChangeAt = now

        if (directionChanges >= REQUIRED_DIRECTION_CHANGES) {
            lastEventAt = now
            _onShakeEvent.tryEmit(Unit)
            resetShakeGesture()
        }
    }

    override fun resetShakeGesture() {
        lastDirection = 0
        lastDirectionChangeAt = 0
        directionChanges = 0
    }

    private fun resetIfGestureExpired(now: Long) {
        if (lastDirectionChangeAt != 0L && now - lastDirectionChangeAt > GESTURE_TIMEOUT_MS) {
            resetShakeGesture()
        }
    }

    private companion object {
        const val MIN_SAMPLE_INTERVAL_MS = 16L
        const val MIN_DIRECTION_INTERVAL_MS = 85L
        const val MAX_DIRECTION_INTERVAL_MS = 520L
        const val GESTURE_TIMEOUT_MS = 1_100L
        const val EVENT_COOLDOWN_MS = 1_400L
        const val REQUIRED_DIRECTION_CHANGES = 4
        const val AXIS_THRESHOLD = 2.4f
        const val GRAVITY_FILTER_ALPHA = 0.82f
    }
}
