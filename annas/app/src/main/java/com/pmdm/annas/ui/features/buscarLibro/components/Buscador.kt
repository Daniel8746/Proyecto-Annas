package com.pmdm.annas.ui.features.buscarLibro.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Buscador(
    buscarNombre: String,
    onValueChange: (String) -> Unit,
    onBuscar: () -> Unit,
    selectedExtensions: List<String>,
    onToggleExtension: (String) -> Unit,
    selectedLanguage: String?,
    onIdiomaChange: (String?) -> Unit,
    pagina: Int,
    onPaginaChange: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = buscarNombre,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar libros, autores...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (selectedExtensions.isNotEmpty() || selectedLanguage != null || pagina > 1)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onBuscar()
                        focusManager.clearFocus()
                    }
                )
            )
        }

        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Formatos",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val extensions = listOf("pdf", "epub", "mobi", "cbr", "cbz")
                    extensions.forEach { ext ->
                        FilterChip(
                            selected = selectedExtensions.contains(ext),
                            onClick = { onToggleExtension(ext) },
                            label = { Text(ext.uppercase()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Idioma",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val languages = mapOf(
                                null to "Todos",
                                "es" to "Español",
                                "en" to "Inglés",
                            )

                            languages.forEach { (code, name) ->
                                FilterChip(
                                    selected = selectedLanguage == code,
                                    onClick = { onIdiomaChange(code) },
                                    label = { Text(name) },
                                    leadingIcon = if (selectedLanguage == code) {
                                        {
                                            Icon(
                                                Icons.Default.Language,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Página",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = pagina.toString(),
                            onValueChange = {onPaginaChange(pagina)},
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (pagina < 1) {
                                        onPaginaChange(1)
                                    }
                                    focusManager.clearFocus()
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}
