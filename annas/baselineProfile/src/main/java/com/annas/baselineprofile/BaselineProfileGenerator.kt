package com.annas.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateStartup() {
        val targetPackageName = targetPackageName()

        rule.collect(
            packageName = targetPackageName,
            includeInStartupProfile = true
        ) {
            startAnnasAndWait(targetPackageName)
        }
    }

    @Test
    fun generateHomeJourneys() {
        val targetPackageName = targetPackageName()

        rule.collect(
            packageName = targetPackageName,
            includeInStartupProfile = false
        ) {
            startAnnasAndWait(targetPackageName)
            exerciseHomeJourneys()
        }
    }
}
