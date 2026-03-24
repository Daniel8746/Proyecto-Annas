package com.pmdm.annas.ui.features.buscarLibro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmdm.annas.data.repositorys.BuscarLibroRepository
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuscarLibroViewModel @Inject constructor(
    private val buscarLibroRepository: BuscarLibroRepository
) : ViewModel() {
    var libros by mutableStateOf<List<Libro>>(emptyList())
        private set
    var buscar by mutableStateOf("")
        private set
    val selectedExtensions = mutableStateListOf<String>()
    var selectedLanguage: String? by mutableStateOf(null)
        private set
    var pagina by mutableIntStateOf(1)
        private set

    var uiStateEnum by mutableStateOf<UIStateEnum?>(null)
        private set

    private var searchJob: Job? = null

    fun onBuscarLibroEvent(event: BuscarLibroEvent) {
        when (event) {
            is BuscarLibroEvent.OnClickBuscar -> {
                if (buscar.isBlank()) return

                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    try {
                        uiStateEnum = UIStateEnum.CARGANDO
                        libros = buscarLibroRepository.getLibros(
                            buscar,
                            selectedExtensions.toList(),
                            selectedLanguage,
                            pagina
                        )
                        uiStateEnum =
                            if (libros.isEmpty()) UIStateEnum.ERROR else UIStateEnum.CARGADO
                    } catch (_: Exception) {
                        uiStateEnum = UIStateEnum.ERROR
                    }
                }
            }

            is BuscarLibroEvent.OnClickLibro -> event.onNavigateLibro()
            is BuscarLibroEvent.OnBuscarChange -> buscar = event.nombre

            is BuscarLibroEvent.OnToggleExtension -> {
                if (!selectedExtensions.remove(event.ext)) {
                    selectedExtensions.add(event.ext)
                }
            }

            is BuscarLibroEvent.OnIdiomaChange -> selectedLanguage = event.idioma
            is BuscarLibroEvent.OnPaginaChange -> {
                pagina = event.pagina
            }
        }
    }
}
