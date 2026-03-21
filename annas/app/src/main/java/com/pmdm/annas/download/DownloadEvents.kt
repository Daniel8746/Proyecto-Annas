package com.pmdm.annas.download

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DownloadEvents {
    private val _cancelFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val cancelFlow = _cancelFlow.asSharedFlow()

    fun cancelDownload() {
        _cancelFlow.tryEmit(Unit)
    }
}
