package com.pmdm.annas.ui.features.buscarLibro.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pmdm.annas.model.Libro

@Composable
fun MostrarLibros(
    modifier: Modifier = Modifier,
    libros: List<Libro>,
    setLibroSeleccionado: (Libro) -> Unit,
    onClickLibro: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(libros) { libro ->
            Row(
                modifier = Modifier.clickable {
                    onClickLibro()
                    setLibroSeleccionado(libro)
                }
            ) {
                AsyncImage(
                    model = libro.portada,
                    contentDescription = "Portada del libro"
                )

                VerticalDivider()

                Column {
                    Text(
                        text = """
                            Titulo: ${libro.titulo}
                            Autor: ${libro.autor}
                            """.trimIndent(),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}