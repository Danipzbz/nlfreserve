package com.dpbprog.nlfreserve

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dpbprog.nlfreserve.viewmodels.MisReservasViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// Colores globales para el botón de cancelar
val rojoBorde = Color(0xFFFF8A80)
val rojoPulsado = Color(0xFFFF5252)

@Composable
fun MisReservasScreen(dniUsuario: String) {
    val viewModel: MisReservasViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. Cargamos los datos
    LaunchedEffect(dniUsuario) {
        viewModel.cargarReservas(dniUsuario)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Mis Reservas Actuales",
                color = VerdeApp,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )

            // 2. Usamos directamente viewModel.listaReservas
            if (viewModel.listaReservas.isEmpty()) {
                Text(
                    text = "No tienes reservas activas",
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(top = 50.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = viewModel.listaReservas,
                        key = { it }
                    ) { reserva ->
                        ReservaItem(
                            reserva = reserva,
                            dniUsuario = dniUsuario,
                            viewModel = viewModel,
                            onMensaje = { msg ->
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        )
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ReservaItem(
    reserva: String,
    dniUsuario: String,
    viewModel: MisReservasViewModel,
    onMensaje: (String) -> Unit
) {
    val partes = reserva.split("|")
    val fechaTexto = partes.getOrNull(0) ?: ""
    val horaTexto = partes.getOrNull(1) ?: ""

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = VerdeApp.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fechaTexto,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = horaTexto,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { showDeleteConfirmation = true },
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = if (isPressed) rojoPulsado else rojoBorde
                ),
                border = BorderStroke(1.dp, if (isPressed) rojoPulsado else rojoBorde),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "Cancelar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("¿Eliminar reserva?", color = Color.White) },
            text = { Text("Se liberará tu plaza:\nDía: $fechaTexto \nHora: $horaTexto.", color = Color.LightGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // LLAMADA AL VIEWMODEL PARA CANCELAR
                        viewModel.cancelarReserva(dniUsuario, fechaTexto, horaTexto) { exito, mensaje ->
                            onMensaje(mensaje)
                            if (exito) {
                                showDeleteConfirmation = false
                            }
                        }
                    }
                ) {
                    Text("Eliminar", color = rojoPulsado, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Volver", color = VerdeApp)
                }
            }
        )
    }
}