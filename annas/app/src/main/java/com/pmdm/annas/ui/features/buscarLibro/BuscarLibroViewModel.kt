package com.pmdm.annas.ui.features.buscarLibro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmdm.annas.data.repositorys.BuscarLibroRepository
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuscarLibroViewModel @Inject constructor(
    private val buscarLibroRepository: BuscarLibroRepository
) : ViewModel() {
    var libros: List<Libro> by mutableStateOf(emptyList())
    var buscar by mutableStateOf("")
    var selectedExtensions = mutableStateListOf<String>()
    var selectedLanguage: String? by mutableStateOf(null)

    var uistateEnum: UIStateEnum? by mutableStateOf(null)

    fun onBuscarLibroEvent(event: BuscarLibroEvent) {
        when (event) {
            is BuscarLibroEvent.OnClickBuscar -> {
                viewModelScope.launch {
                    try {
                        uistateEnum = UIStateEnum.CARGANDO
                        libros = buscarLibroRepository.getLibros(
                            buscar,
                            selectedExtensions.toList(),
                            selectedLanguage
                        )
                        uistateEnum =
                            if (libros.isEmpty()) UIStateEnum.ERROR else UIStateEnum.CARGADO
                    } catch (e: Exception) {
                        e.printStackTrace()
                        uistateEnum = UIStateEnum.ERROR
                    }
                }
            }

            is BuscarLibroEvent.OnClickLibro -> event.onNavigateLibro()
            is BuscarLibroEvent.OnBuscarChange -> buscar = event.nombre

            is BuscarLibroEvent.OnToggleExtension -> {
                if (selectedExtensions.contains(event.ext)) {
                    selectedExtensions.remove(event.ext)
                } else {
                    selectedExtensions.add(event.ext)
                }
            }

            is BuscarLibroEvent.OnIdiomaChange -> {
                selectedLanguage = event.idioma
            }
        }
    }
}
