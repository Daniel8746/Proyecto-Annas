package com.pmdm.annas.uri

import android.webkit.URLUtil
import java.net.URLDecoder
import java.net.URLEncoder

object UriUtils {
    fun encode(s: String): String = URLEncoder.encode(s, "UTF-8")
    fun decode(s: String): String = URLDecoder.decode(s, "UTF-8")
    fun getRawFileName(s: String, cd: String, mime: String): String = URLUtil.guessFileName(s, cd, mime)
}
