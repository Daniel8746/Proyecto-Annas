package com.annas.model

import androidx.compose.runtime.Immutable

@Immutable
data class DownloadState(
    val url: String = "",
    val userAgent: String = "",
    val contentDisposition: String = "",
    val mimeType: String = "",
    val fileName: String = "",
    val length: Long = 0L,
    val referer: String? = ""
)