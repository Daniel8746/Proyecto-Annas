package com.pmdm.annas.ui.features.buscarLibro

import androidx.compose.runtime.getValue
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

    var uistateEnum: UIStateEnum? by mutableStateOf(null)

    fun onBuscarLibroEvent(event: BuscarLibroEvent) {
        when (event) {
            is BuscarLibroEvent.OnClickBuscar -> {
                viewModelScope.launch {
                    try {
                        uistateEnum = UIStateEnum.CARGANDO

                        libros = buscarLibroRepository.getLibros(buscar)

                        uistateEnum =
                            if (libros.isEmpty()) UIStateEnum.ERROR else UIStateEnum.CARGADO

                    } catch (_: Exception) {
                        uistateEnum = UIStateEnum.ERROR
                    }
                }
            }

            is BuscarLibroEvent.OnClickLibro -> event.onNavigateLibro()
            is BuscarLibroEvent.OnBuscarChange -> buscar = event.nombre
        }
    }
}