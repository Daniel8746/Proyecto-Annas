package com.pmdm.annas.utils

fun isUnnecessaryResource(url: String): Boolean {
    val low = url.lowercase()
    return low.endsWith(".jpg") || low.endsWith(".png") || low.endsWith(".jpeg") ||
            low.endsWith(".gif") || low.endsWith(".svg") || low.endsWith(".css") ||
            low.endsWith(".woff") || low.endsWith(".woff2") || low.endsWith(".ttf") ||
            low.endsWith(".mp4") || low.endsWith(".webm") || low.endsWith(".mp3") ||
            low.contains("google-analytics") || low.contains("doubleclick") ||
            low.contains("facebook") || low.contains("adsystem") ||
            low.contains("analytics") || low.contains("tracker") || low.contains("pixel") ||
            low.contains("fonts.googleapis") || low.contains("fonts.gstatic") ||
            low.contains("disqus") || low.contains("sentry") || low.contains("hotjar") ||
            low.contains("ads.") || low.contains("/ads/")
}
