package com.pmdm.annas.ui.features.libro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmdm.annas.data.network.SilentDownloader
import com.pmdm.annas.data.network.getMime
import com.pmdm.annas.data.notifications.NotificationHelper
import com.pmdm.annas.data.repositorys.LibroRepository
import com.pmdm.annas.model.DownloadState
import com.pmdm.annas.model.LibroUiState
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.uri.UriUtils
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
                viewModelScope.launch {
                    silentDownloader.downloadFileWithNotification(
                        url = downloadState.url,
                        ua = downloadState.userAgent,
                        cd = downloadState.contentDisposition,
                        mime = downloadState.mimeType,
                        dest = event.fileUri,
                        fileName = downloadState.fileName,
                        helper = notificationHelper,
                        len = downloadState.length,
                        ref = downloadState.referer
                    )
                }
            }
        }
    }
}