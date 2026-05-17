package com.annas.ui.features.buscarLibro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annas.data.repositorys.BuscarLibroRepository
import com.annas.data.repositorys.updateState
import com.annas.model.BuscarLibroUiState
import com.annas.ui.features.UIStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuscarLibroViewModel @Inject constructor(
    private val buscarLibroRepository: BuscarLibroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuscarLibroUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var lastSearchKey: String? = null

    fun onBuscarLibroEvent(event: BuscarLibroEvent) {
        when (event) {
            is BuscarLibroEvent.OnClickBuscar -> {
                if (_uiState.value.buscar.isBlank()) return
                buscarLibro()
            }

            is BuscarLibroEvent.OnClickLibro -> event.onNavigateLibro()

            is BuscarLibroEvent.OnBuscarChange -> {
                _uiState.updateState { copy(buscar = event.nombre) }
            }

            is BuscarLibroEvent.OnToggleExtension -> {
                val newExtensions = _uiState.value.selectedExtensions.toMutableList()
                if (!newExtensions.remove(event.ext)) newExtensions.add(event.ext)
                _uiState.updateState { copy(selectedExtensions = newExtensions) }
            }

            is BuscarLibroEvent.OnIdiomaChange -> {
                _uiState.updateState { copy(selectedLanguage = event.idioma) }
            }

            is BuscarLibroEvent.OnPaginaChange -> {
                if (event.pagina < 1 || event.pagina == _uiState.value.pagina) return

                _uiState.updateState { copy(pagina = event.pagina) }
                buscarLibro()
            }
        }
    }

    private fun buscarLibro() {
        val query = _uiState.value.buscar.trim()

        if (query.isBlank()) return

        val searchKey = buildString {
            append(query.lowercase())
            append('|')
            append(_uiState.value.selectedExtensions.sorted().joinToString(","))
            append('|')
            append(_uiState.value.selectedLanguage.orEmpty())
            append('|')
            append(_uiState.value.pagina)
        }

        if (
            searchKey == lastSearchKey
        ) {
            return
        }

        searchJob?.cancel()
        lastSearchKey = searchKey

        searchJob = viewModelScope.launch {
            try {
                _uiState.updateState { copy(uiStateEnum = UIStateEnum.CARGANDO) }

                val libros = buscarLibroRepository.getLibros(
                    query,
                    _uiState.value.selectedExtensions,
                    _uiState.value.selectedLanguage,
                    _uiState.value.pagina
                )

                _uiState.updateState {
                    copy(
                        libros = libros,
                        uiStateEnum = if (libros.isEmpty()) UIStateEnum.ERROR
                        else UIStateEnum.CARGADO
                    )
                }
            } catch (_: Exception) {
                _uiState.updateState { copy(uiStateEnum = UIStateEnum.ERROR) }
            }
        }
    }
}
