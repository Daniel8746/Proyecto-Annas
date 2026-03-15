package com.pmdm.annas.ui.features

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pmdm.annas.model.Libro
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibroCompartidoViewModel @Inject constructor() : ViewModel() {
    var libroSeleccionado by mutableStateOf(Libro())
}