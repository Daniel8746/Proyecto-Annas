package com.pmdm.annas.data.network

private val directExtensions = setOf(".pdf", ".epub", ".mobi", ".azw3", ".zip")

fun guessCD(url: String): String {
    val fileName = url.substringAfterLast("/").substringBefore("?")

    return if (fileName.isNotEmpty() && fileName.contains(".")) {

        "attachment; filename=\"$fileName\""

    } else ""
}

fun getMime(url: String) = when {
    url.contains(".pdf", true) -> "application/pdf"

    url.contains(".epub", true) -> "application/epub+zip"

    url.contains(".mobi", true) -> "application/x-mobipocket-ebook"

    url.contains(".azw3", true) -> "application/x-mobipocket-ebook"

    url.contains(".zip", true) -> "application/zip"

    else -> "application/octet-stream"
}

fun isDirect(url: String): Boolean {
    val low = url.lowercase()

    if (low.contains("slow_download") && low.contains("annas-archive.org")) return false

    return directExtensions.any {
        low.endsWith(it) || low.contains("$it?")
    } || low.contains("get.php") || low.contains("libgen") || low.contains("/get/") || (low.contains(
        "/download/"
    ) && !low.contains("annas-archive.org")) || low.contains(":6060")
}