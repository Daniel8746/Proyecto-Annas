package com.annas.model

data class DownloadState(
    val url: String = "",
    val userAgent: String = "",
    val contentDisposition: String = "",
    val mimeType: String = "",
    val fileName: String = "",
    val length: Long = 0L,
    val referer: String? = ""
)