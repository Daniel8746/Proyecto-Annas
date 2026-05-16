package com.annas.data.utils

import java.util.Locale

private val blockedExtensions = setOf(
    ".jpg",
    ".jpeg",
    ".png",
    ".gif",
    ".svg",
    ".webp",
    ".avif",
    ".ico",
    ".bmp",
    ".css",
    ".woff",
    ".woff2",
    ".ttf",
    ".otf",
    ".eot",
    ".mp4",
    ".webm",
    ".mp3",
    ".m4a",
    ".wav",
    ".ogg"
)

private val blockedKeywords = setOf(
    "google-analytics",
    "googletagmanager",
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
    "cloudflareinsights",
    "clarity.ms",
    "plausible",
    "matomo",
    "newrelic",
    "segment.io"
)

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
    "amazon-adsystem",
    "cloudflareinsights"
)

fun isUnnecessaryResource(url: String): Boolean {
    val low = url.lowercase(Locale.ROOT)
    val cleanUrl = low.substringBefore('#').substringBefore('?')

    if (blockedExtensions.any { cleanUrl.endsWith(it) }) {
        return true
    }

    if (blockedKeywords.any { low.contains(it) }) {
        return true
    }

    return cleanUrl.endsWith(".js") && blockedJsKeywords.any { low.contains(it) }
}
