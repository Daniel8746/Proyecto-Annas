package com.pmdm.annas.ui.features.libro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmdm.annas.data.repositorys.LibroRepository
import com.pmdm.annas.download.SilentDownloader
import com.pmdm.annas.model.LibroUiState
import com.pmdm.annas.ui.features.UIStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibroViewModel @Inject constructor(
    private val libroRepository: LibroRepository,
    val silentDownloader: SilentDownloader
) : ViewModel() {
    // Estado unificado para el UI
    var uiState by mutableStateOf(LibroUiState())
        private set

    private var loadingJob: Job? = null
    private var lastEnlace: String? = null

    fun onLibroEvent(event: LibroEvent) {
        when (event) {
            is LibroEvent.ObtenerLinksServidor -> {
                // Evitar recarga innecesaria
                if (event.enlace == lastEnlace && (uiState.uiStateEnum == UIStateEnum.CARGANDO || uiState.uiStateEnum == UIStateEnum.CARGADO)) {
                    return
                }

                lastEnlace = event.enlace
                loadingJob?.cancel()

                loadingJob = viewModelScope.launch {
                    try {
                        // Mostrar carga
                        uiState = uiState.copy(uiStateEnum = UIStateEnum.CARGANDO)

                        val result = libroRepository.getLinksServidor(event.enlace)

                        // Actualizar estado con la descripción y enlaces
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
        }
    }
}