package com.pmdm.annas.ui.features.buscarLibro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmdm.annas.data.repositorys.BuscarLibroRepository
import com.pmdm.annas.model.BuscarLibroUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuscarLibroViewModel @Inject constructor(
    private val buscarLibroRepository: BuscarLibroRepository
) : ViewModel() {

    var uiState by mutableStateOf(BuscarLibroUiState())
        private set

    private var searchJob: Job? = null

    fun onBuscarLibroEvent(event: BuscarLibroEvent) {
        when (event) {
            is BuscarLibroEvent.OnClickBuscar -> {
                if (uiState.buscar.isBlank()) return
                buscarLibro()
            }

            is BuscarLibroEvent.OnClickLibro -> event.onNavigateLibro()

            is BuscarLibroEvent.OnBuscarChange -> {
                uiState = uiState.copy(buscar = event.nombre)
            }

            is BuscarLibroEvent.OnToggleExtension -> {
                val newExtensions = uiState.selectedExtensions.toMutableList()
                if (!newExtensions.remove(event.ext)) newExtensions.add(event.ext)
                uiState = uiState.copy(selectedExtensions = newExtensions)
            }

            is BuscarLibroEvent.OnIdiomaChange -> {
                uiState = uiState.copy(selectedLanguage = event.idioma)
            }

            is BuscarLibroEvent.OnPaginaChange -> {
                uiState = uiState.copy(pagina = event.pagina)
                buscarLibro()
            }
        }
    }

    private fun buscarLibro() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                uiState =
                    uiState.copy(uiStateEnum = com.pmdm.annas.ui.features.UIStateEnum.CARGANDO)
                val libros = buscarLibroRepository.getLibros(
                    uiState.buscar,
                    uiState.selectedExtensions,
                    uiState.selectedLanguage,
                    uiState.pagina
                )
                uiState = uiState.copy(
                    libros = libros,
                    uiStateEnum = if (libros.isEmpty()) com.pmdm.annas.ui.features.UIStateEnum.ERROR
                    else com.pmdm.annas.ui.features.UIStateEnum.CARGADO
                )
            } catch (_: Exception) {
                uiState = uiState.copy(uiStateEnum = com.pmdm.annas.ui.features.UIStateEnum.ERROR)
            }
        }
    }
}