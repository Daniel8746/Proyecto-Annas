package com.annas.ui.features.libro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annas.data.network.SilentDownloader
import com.annas.data.network.getMime
import com.annas.data.notifications.NotificationHelper
import com.annas.data.repositorys.LibroRepository
import com.annas.data.repositorys.updateState
import com.annas.model.DownloadState
import com.annas.model.LibroUiState
import com.annas.ui.features.UIStateEnum
import com.annas.uri.UriUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibroViewModel @Inject constructor(
    private val libroRepository: LibroRepository,
    val silentDownloader: SilentDownloader,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibroUiState())
    val uiState = _uiState.asStateFlow()

    private var loadingJob: Job? = null

    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState = _downloadState.asStateFlow()

    private val _tiempoEspera = MutableStateFlow(0)
    val tiempoEspera = _tiempoEspera.asStateFlow()

    fun onLibroEvent(event: LibroEvent) {
        when (event) {
            is LibroEvent.ObtenerLinksServidor -> {
                loadingJob?.cancel()

                loadingJob = viewModelScope.launch {
                    try {
                        _uiState.updateState { copy(uiStateEnum = UIStateEnum.CARGANDO) }

                        val result = libroRepository.getLinksServidor(event.enlace)

                        _uiState.updateState {
                            copy(
                                descripcion = result.first,
                                enlacesServidor = result.second,
                                uiStateEnum = UIStateEnum.CARGADO
                            )
                        }
                    } catch (_: Exception) {
                        _uiState.updateState { copy(uiStateEnum = UIStateEnum.ERROR) }
                    }
                }
            }

            is LibroEvent.PrepararDescarga -> {
                _uiState.updateState { copy(uiStateEnum = UIStateEnum.CARGANDO) }
                silentDownloader.onTiempoEspera = { tiempo ->
                    _tiempoEspera.updateState { tiempo }
                }

                silentDownloader.launchSilentDownload(
                    activity = event.context,
                    url = event.url,
                    onDownloadStart = { dUrl, ua, cd, mime, len, ref ->
                        _downloadState.updateState {
                            DownloadState(
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
                    }
                )
            }

            is LibroEvent.DescargarLibro -> {
                if (event.fileUri != null) {
                    val state = _downloadState.value

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
                }

                _tiempoEspera.updateState { 0 }
                _uiState.updateState { copy(uiStateEnum = UIStateEnum.CARGADO) }
            }
        }
    }
}