package com.pmdm.annas.utils

fun isUnnecessaryResource(url: String): Boolean =
    url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".jpeg") ||
            url.endsWith(".gif") || url.endsWith(".svg") || url.endsWith(".css") ||
            url.endsWith(".woff") || url.endsWith(".woff2") || url.endsWith(".ttf") ||
            url.contains("google-analytics") || url.contains("doubleclick") ||
            url.contains("facebook") || url.contains("adsystem") ||
            url.contains("analytics") || url.contains("tracker") || url.contains("pixel") ||
            url.contains("fonts.googleapis") || url.contains("fonts.gstatic")