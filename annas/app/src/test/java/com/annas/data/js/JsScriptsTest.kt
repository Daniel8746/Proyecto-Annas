package com.annas.data.js

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsScriptsTest {

    @Test
    fun htmlCaptureScriptRendersSelectorAndTimeout() {
        val script = JsEngine.render(
            JsScripts.HTML_CAPTURE_AND_SEND,
            mapOf(
                "SELECTOR" to "\"main\"",
                "TIMEOUT" to 1234
            )
        )

        assertTrue(script.contains("const selector = \"main\";"))
        assertTrue(script.contains("}, 1234);"))
        assertFalse(script.contains("__SELECTOR__"))
        assertFalse(script.contains("__TIMEOUT__"))
    }

    @Test
    fun autoExtractScriptChecksCurrentDownloadPatterns() {
        val script = JsScripts.AUTO_EXTRACT_AND_REDIRECT_SCRIPT

        assertTrue(script.contains("span.break-all"))
        assertTrue(script.contains("data-clipboard-text"))
        assertTrue(script.contains("js-download-link"))
        assertTrue(script.contains("window.location.href"))
    }
}
