package com.pmdm.annas.ui.features.buscarLibro.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Buscador(
    buscarNombre: String,
    onValueChange: (String) -> Unit,
    onBuscar: () -> Unit
) {
    TopAppBar(
        title = {},
        actions = {
            Row {
                OutlinedTextField(
                    value = buscarNombre,
                    onValueChange = onValueChange
                )

                TextButton(
                    onClick = onBuscar
                ) {
                    Text(
                        text = "Buscar",
                        fontSize = 18.sp
                    )
                }
            }
        }
    )
}