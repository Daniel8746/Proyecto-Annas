package com.pmdm.annas.download

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DownloadEvents {
    // Aumentamos extraBufferCapacity para asegurar que el evento de cancelación se capture siempre
    private val _cancelFlow = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 5)
    val cancelFlow = _cancelFlow.asSharedFlow()

    fun cancelDownload() {
        _cancelFlow.tryEmit(Unit)
    }
}
