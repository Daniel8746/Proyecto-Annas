package com.pmdm.annas.ui.features.libro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmdm.annas.data.repositorys.LibroRepository
import com.pmdm.annas.ui.features.UIStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltViewModel
class LibroViewModel @Inject constructor(
    private val libroRepository: LibroRepository,
    val okHttpClient: OkHttpClient
) : ViewModel() {
    var enlacesServidor by mutableStateOf<List<String>>(emptyList())
        private set
    var descripcion by mutableStateOf("")
        private set
    var uiStateEnum by mutableStateOf<UIStateEnum?>(null)
        private set

    private var loadingJob: Job? = null
    private var lastEnlace: String? = null

    fun onLibroEvent(event: LibroEvent) {
        when (event) {
            is LibroEvent.ObtenerLinksServidor -> {
                if (event.enlace == lastEnlace && (uiStateEnum == UIStateEnum.CARGANDO || uiStateEnum == UIStateEnum.CARGADO)) {
                    return
                }

                lastEnlace = event.enlace
                loadingJob?.cancel()

                loadingJob = viewModelScope.launch {
                    try {
                        uiStateEnum = UIStateEnum.CARGANDO
                        val result = libroRepository.getLinksServidor(event.enlace)

                        descripcion = result.first
                        enlacesServidor = result.second
                        uiStateEnum = UIStateEnum.CARGADO
                    } catch (_: Exception) {
                        uiStateEnum = UIStateEnum.ERROR
                    }
                }
            }
        }
    }
}
