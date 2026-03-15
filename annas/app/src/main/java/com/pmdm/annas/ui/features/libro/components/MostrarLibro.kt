package com.pmdm.annas.ui.features.libro.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun MostrarLibro(
    portada: String,
    titulo: String,
    autor: String,
    descripcion: String,
    enlacesServidor: List<String>
) {
    Column {
        AsyncImage(
            model = portada,
            contentDescription = "Portada libro"
        )

        HorizontalDivider()

        Text(
            text = """
                    Titulo: $titulo
                    Autor: $autor
                    Descripción: $descripcion
                    """.trimIndent(),
            fontSize = 18.sp
        )

        enlacesServidor.forEachIndexed { index, enlaceServer ->
            Text(
                text = "Servidor ${index + 1}: $enlaceServer",
                fontSize = 18.sp
            )
        }
    }
}