package com.annas.data.utils

import com.annas.data.utils.isUnnecessaryResource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnnecessaryResourceTest {

    @Test
    fun blocksStaticAssetsEvenWhenTheyUseQueryParams() {
        assertTrue(isUnnecessaryResource("https://example.org/cover.webp?v=123"))
        assertTrue(isUnnecessaryResource("https://example.org/style.css?hash=abc"))
        assertTrue(isUnnecessaryResource("https://example.org/font.woff2#iefix"))
    }

    @Test
    fun blocksKnownAnalyticsAndAdScripts() {
        assertTrue(isUnnecessaryResource("https://www.googletagmanager.com/gtag/js?id=G-1"))
        assertTrue(isUnnecessaryResource("https://example.org/assets/analytics.js"))
    }

    @Test
    fun keepsUnknownJavascriptAvailableForPagesThatNeedIt() {
        assertFalse(isUnnecessaryResource("https://annas-archive.org/assets/app.js"))
    }
}
