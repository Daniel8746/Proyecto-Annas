package com.annas.ui.features.libro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annas.data.network.SilentDownloader
import com.annas.data.network.getMime
import com.annas.data.notifications.NotificationHelper
import com.annas.data.repositorys.LibroRepository
import com.annas.model.DownloadState
import com.annas.model.LibroUiState
import com.annas.ui.features.UIStateEnum
import com.annas.uri.UriUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibroViewModel @Inject constructor(
    private val libroRepository: LibroRepository,
    val silentDownloader: SilentDownloader,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    var uiState by mutableStateOf(LibroUiState())
        private set

    private var loadingJob: Job? = null
    private var lastEnlace: String? = null

    var downloadState by mutableStateOf(DownloadState())
        private set

    fun onLibroEvent(event: LibroEvent) {
        when (event) {
            is LibroEvent.ObtenerLinksServidor -> {
                if (event.enlace == lastEnlace && (uiState.uiStateEnum == UIStateEnum.CARGANDO || uiState.uiStateEnum == UIStateEnum.CARGADO)) {
                    return
                }

                lastEnlace = event.enlace
                loadingJob?.cancel()

                loadingJob = viewModelScope.launch {
                    try {
                        uiState = uiState.copy(uiStateEnum = UIStateEnum.CARGANDO)

                        val result = libroRepository.getLinksServidor(event.enlace)

                        uiState = uiState.copy(
                            descripcion = result.first,
                            enlacesServidor = result.second,
                            uiStateEnum = UIStateEnum.CARGADO
                        )
                    } catch (_: Exception) {
                        uiState = uiState.copy(uiStateEnum = UIStateEnum.ERROR)
                    }
                }
            }

            is LibroEvent.PrepararDescarga -> {
                uiState = uiState.copy(uiStateEnum = UIStateEnum.CARGANDO)
                downloadState = DownloadState()
                silentDownloader.launchSilentDownload(
                    activity = event.context,
                    url = event.url,
                    onDownloadStart = { dUrl, ua, cd, mime, len, ref ->
                        downloadState = DownloadState(
                            url = dUrl,
                            userAgent = ua,
                            contentDisposition = cd,
                            mimeType = if (mime.isBlank() || mime == "application/octet-stream") getMime(
                                dUrl
                            ) else mime,
                            fileName = UriUtils.decode(
                                UriUtils.getRawFileName(
                                    dUrl,
                                    cd,
                                    mime
                                )
                            ),
                            length = len,
                            referer = ref
                        )
                    }
                )
            }

            is LibroEvent.DescargarLibro -> {
                val state = downloadState
                viewModelScope.launch {
                    silentDownloader.downloadFileWithNotification(
                        url = state.url,
                        ua = state.userAgent,
                        cd = state.contentDisposition,
                        mime = state.mimeType,
                        dest = event.fileUri,
                        fileName = state.fileName,
                        helper = notificationHelper,
                        len = state.length,
                        ref = state.referer
                    )
                }

                uiState = uiState.copy(uiStateEnum = UIStateEnum.CARGADO)
            }
        }
    }
}