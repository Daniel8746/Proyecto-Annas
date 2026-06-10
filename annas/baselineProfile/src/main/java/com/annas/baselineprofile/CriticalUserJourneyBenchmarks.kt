package com.annas.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CriticalUserJourneyBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun homeJourneysBaselineProfiles() {
        benchmark(
            CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require,
                warmupIterations = 3
            )
        )
    }

    private fun benchmark(compilationMode: CompilationMode) {
        val targetPackageName = targetPackageName()

        rule.measureRepeated(
            packageName = targetPackageName,
            metrics = persistentListOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 8,
            setupBlock = {
                pressHome()
            },
            measureBlock = {
                startAnnasAndWait(
                    targetPackageName = targetPackageName,
                    pressHomeFirst = false
                )
                exerciseHomeJourneys()
            }
        )
    }
}
