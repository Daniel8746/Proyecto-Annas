package com.annas.ui.features.buscarLibro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annas.data.repositorys.BuscarLibroRepository
import com.annas.model.BuscarLibroUiState
import com.annas.ui.features.UIStateEnum
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
    private var lastSearchKey: String? = null

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
                if (event.pagina < 1 || event.pagina == uiState.pagina) return

                uiState = uiState.copy(pagina = event.pagina)
                buscarLibro()
            }
        }
    }

    private fun buscarLibro() {
        val query = uiState.buscar.trim()

        if (query.isBlank()) return

        val searchKey = buildString {
            append(query.lowercase())
            append('|')
            append(uiState.selectedExtensions.sorted().joinToString(","))
            append('|')
            append(uiState.selectedLanguage.orEmpty())
            append('|')
            append(uiState.pagina)
        }

        if (
            searchKey == lastSearchKey &&
            (uiState.uiStateEnum == UIStateEnum.CARGANDO || uiState.uiStateEnum == UIStateEnum.CARGADO)
        ) {
            return
        }

        searchJob?.cancel()
        lastSearchKey = searchKey

        searchJob = viewModelScope.launch {
            try {
                uiState =
                    uiState.copy(uiStateEnum = UIStateEnum.CARGANDO)
                val libros = buscarLibroRepository.getLibros(
                    query,
                    uiState.selectedExtensions,
                    uiState.selectedLanguage,
                    uiState.pagina
                )
                uiState = uiState.copy(
                    libros = libros,
                    uiStateEnum = if (libros.isEmpty()) UIStateEnum.ERROR
                    else UIStateEnum.CARGADO
                )
            } catch (_: Exception) {
                uiState = uiState.copy(uiStateEnum = UIStateEnum.ERROR)
            }
        }
    }
}
