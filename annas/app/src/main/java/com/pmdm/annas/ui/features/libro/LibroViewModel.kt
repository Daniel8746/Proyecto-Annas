package com.pmdm.annas.ui.features.libro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmdm.annas.data.repositorys.LibroRepository
import com.pmdm.annas.ui.features.UIStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibroViewModel @Inject constructor(
    private val libroRepository: LibroRepository
) : ViewModel() {
    var enlacesServidor: List<String> by mutableStateOf(emptyList())
    var descripcion by mutableStateOf("")

    var uiStateEnum: UIStateEnum? by mutableStateOf(null)

    fun onLibroEvent(event: LibroEvent) {
        when (event) {
            is LibroEvent.ObtenerLinksServidor -> {
                viewModelScope.launch {
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