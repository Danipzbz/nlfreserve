package com.dpbprog.nlfreserve

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dpbprog.nlfreserve.models.Reservas
import com.dpbprog.nlfreserve.models.ReservasViewModel
import kotlinx.coroutines.launch

@Composable
fun ReservarScreen(dniUsuario: String, viewModel: ReservasViewModel = viewModel()) {
    val reservas by viewModel.reservas.collectAsState()
    val limitePermitido by viewModel.limiteUsuario.collectAsState()
    val fechasOcupadas by viewModel.fechasReservadasPorUsuario.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var reservaElegida by remember { mutableStateOf<Reservas?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lanzarToast: (String) -> Unit = { mensaje ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    // Cargamos los límites y el estado del usuario al iniciar
    LaunchedEffect(dniUsuario) {
        viewModel.cargarDatosValidacion(dniUsuario)
    }

    val fechasUnicas = reservas.map { it.fecha }.distinct().sortedBy { fecha ->
        val partes = fecha.split("-")
        if (partes.size == 3) "${partes[2]}${partes[1].padStart(2, '0')}${partes[0].padStart(2, '0')}" else fecha
    }

    // Usamos el tamaño de la lista de fechas ocupadas para saber cuántas reservas tiene
    val misReservasActuales = fechasOcupadas.size
    val limiteAlcanzado = misReservasActuales >= limitePermitido && limitePermitido > 0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona una sesión",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = VerdeApp
            )

            // Aviso superior si ha llegado al límite total
            if (limiteAlcanzado) {
                Text(
                    text = "Has alcanzado tu límite de $limitePermitido reservas",
                    color = Color(0xFFFF8A80),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            for (i in fechasUnicas.indices step 2) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

                    // --- CARD 1 ---
                    val fecha1 = fechasUnicas[i]
                    val yaTieneEseDia1 = fechasOcupadas.contains(fecha1)

                    DiaCard(fecha = fecha1, habilitado = !yaTieneEseDia1 && !limiteAlcanzado) {
                        when {
                            yaTieneEseDia1 -> lanzarToast("Ya tienes una reserva para el día $fecha1")
                            limiteAlcanzado -> lanzarToast("Has alcanzado tu límite de $limitePermitido reservas")
                            else -> {
                                fechaSeleccionada = fecha1
                                reservaElegida = null
                                showDialog = true
                            }
                        }
                    }

                    // --- CARD 2 ---
                    if (i + 1 < fechasUnicas.size) {
                        val fecha2 = fechasUnicas[i+1]
                        val yaTieneEseDia2 = fechasOcupadas.contains(fecha2)

                        DiaCard(fecha = fecha2, habilitado = !yaTieneEseDia2 && !limiteAlcanzado) {
                            when {
                                yaTieneEseDia2 -> lanzarToast("Ya tienes una reserva para el día $fecha2")
                                limiteAlcanzado -> lanzarToast("Has alcanzado tu límite de $limitePermitido reservas")
                                else -> {
                                    fechaSeleccionada = fecha2
                                    reservaElegida = null
                                    showDialog = true
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(140.dp))
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (showDialog) {
        val horasDisponibles = reservas
            .filter { it.fecha == fechaSeleccionada }
            .sortedBy { it.hora }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Horas disponibles para el \n$fechaSeleccionada", color = VerdeApp) },
            text = {
                Column {
                    horasDisponibles.forEach { reserva ->
                        val hayPlazas = reserva.plazas > 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = hayPlazas) { reservaElegida = reserva }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (reserva == reservaElegida),
                                onClick = { if (hayPlazas) reservaElegida = reserva },
                                enabled = hayPlazas
                            )
                            Text(
                                text = "${reserva.hora} (${reserva.plazas} plazas)",
                                color = if (hayPlazas) Color.White else Color.Gray,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reservaElegida?.let {
                            viewModel.reservarSesion(dniUsuario, it) { exito, mensaje ->
                                scope.launch { snackbarHostState.showSnackbar(mensaje) }
                                if (exito) showDialog = false
                            }
                        }
                    },
                    enabled = reservaElegida != null,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeApp)
                ) {
                    Text("Confirmar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = VerdeApp)
                }
            }
        )
    }
}

@Composable
fun DiaCard(fecha: String, habilitado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .border(2.dp, if (habilitado) VerdeApp else Color.DarkGray, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .graphicsLayer(alpha = if (habilitado) 1f else 0.4f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = obtenerNombreDia(fecha),
                style = TextStyle(color = if (habilitado) VerdeApp else Color.Gray, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            )
            Text(
                text = fecha,
                style = TextStyle(color = Color.White, fontSize = 14.sp)
            )
        }
    }
}

fun obtenerNombreDia(fechaString: String): String {
    return try {
        val partes = fechaString.trim().split("-")
        if (partes.size != 3) return "DÍA"
        val dia = partes[0].padStart(2, '0')
        val mes = partes[1].padStart(2, '0')
        val anio = if (partes[2].length == 2) "20${partes[2]}" else partes[2]
        val fechaIso = "$anio-$mes-$dia"
        val fecha = kotlinx.datetime.LocalDate.parse(fechaIso)
        when (fecha.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "LUNES"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "MARTES"
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "MIÉRCOLES"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "JUEVES"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "VIERNES"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "SÁBADO"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "DOMINGO"
            else -> ""
        }
    } catch (e: Exception) { "DÍA" }
}