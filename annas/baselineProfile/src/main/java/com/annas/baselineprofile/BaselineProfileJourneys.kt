package com.annas.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until

private const val STARTUP_TIMEOUT_MS = 5_000L
private const val INTERACTION_TIMEOUT_MS = 2_000L

internal fun targetPackageName(): String =
    InstrumentationRegistry.getArguments().getString("targetAppId")
        ?: throw IllegalStateException("targetAppId not passed as instrumentation runner arg")

internal fun MacrobenchmarkScope.startAnnasAndWait(
    targetPackageName: String,
    pressHomeFirst: Boolean = true
) {
    if (pressHomeFirst) {
        pressHome()
    }

    startActivityAndWait()
    device.wait(Until.hasObject(By.pkg(targetPackageName)), STARTUP_TIMEOUT_MS)
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.exerciseHomeJourneys() {
    clickIfVisible(By.desc("Filtros"))
    clickIfVisible(By.text("PDF"))
    clickIfVisible(By.text("EPUB"))
    clickIfVisible(By.text("Cualquiera"))
    clickIfVisible(By.desc("Abrir chat"))
    device.wait(Until.hasObject(By.text("Annas Chat")), INTERACTION_TIMEOUT_MS)
    device.waitForIdle()
    clickIfVisible(By.text("Cerrar"))
}

private fun MacrobenchmarkScope.clickIfVisible(
    selector: BySelector,
    timeoutMillis: Long = INTERACTION_TIMEOUT_MS
) {
    device.wait(Until.hasObject(selector), timeoutMillis)
    device.findObject(selector)?.click()
    device.waitForIdle()
}
