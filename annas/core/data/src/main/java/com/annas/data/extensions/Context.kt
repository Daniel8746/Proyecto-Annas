package com.annas.data.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.VibrationEffect
import android.os.VibratorManager

fun Context.findActivity(): Activity {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    throw IllegalStateException("No Activity found")
}

fun Context.vibrateClick() {
    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    vibratorManager.defaultVibrator.vibrate(
        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
    )
}

fun Context.loadPrompt(fileName: String): String {
    return assets.open(fileName)
        .bufferedReader()
        .use { it.readText() }
}