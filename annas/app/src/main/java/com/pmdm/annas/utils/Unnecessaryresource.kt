package com.pmdm.annas.utils

import java.util.Locale

private val blockedExtensions = setOf(
    ".jpg",
    ".jpeg",
    ".png",
    ".gif",
    ".svg",
    ".css",
    ".woff",
    ".woff2",
    ".ttf",
    ".mp4",
    ".webm",
    ".mp3"
)

private val blockedKeywords = setOf(
    "google-analytics",
    "doubleclick",
    "facebook",
    "adsystem",
    "analytics",
    "tracker",
    "pixel",
    "fonts.googleapis",
    "fonts.gstatic",
    "disqus",
    "sentry",
    "hotjar",
    "ads.",
    "/ads/",
    "beacon",
    "stats.",
    "cloudflareinsights"
)

/**
 * JS que sabemos que NO es crítico para scraping Anna's Archive
 */
private val blockedJsKeywords = setOf(
    "analytics.js",
    "gtag",
    "googletagmanager",
    "ads.js",
    "tracker.js",
    "metrics.js",
    "collect",
    "pixel.js",
    "advertising",
    "pagead",
    "amazon-adsystem"
)

fun isUnnecessaryResource(url: String): Boolean {

    val low = url.lowercase(Locale.ROOT)

    if (blockedExtensions.any { low.endsWith(it) }) {
        return true
    }

    if (blockedKeywords.any { low.contains(it) }) {
        return true
    }

    if (
        low.endsWith(".js") &&
        blockedJsKeywords.any { low.contains(it) }
    ) {
        return true
    }

    return false
}