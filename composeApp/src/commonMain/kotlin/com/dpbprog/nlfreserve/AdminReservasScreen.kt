package com.dpbprog.nlfreserve

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dpbprog.nlfreserve.models.Reservas
import com.dpbprog.nlfreserve.viewmodels.AdminReservasViewModel
import com.dpbprog.nlfreserve.viewmodels.UsuarioApuntado
import kotlinx.coroutines.launch

@Composable
fun AdminReservasScreen() {
    val viewModel: AdminReservasViewModel = viewModel()
    val sesiones = viewModel.sesionesPorDia

    var showModal by remember { mutableStateOf(false) }
    var sesionSeleccionada by remember { mutableStateOf<Reservas?>(null) }

    var mensajeExitoModal by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Control de Sesiones",
            color = VerdeApp,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sesiones.forEach { (fecha, listaDeHoras) ->
                item {
                    DiaExpandibleItem(
                        fecha = fecha,
                        horas = listaDeHoras,
                        onHoraClick = { sesion ->
                            sesionSeleccionada = sesion
                            viewModel.cargarAlumnos(sesion.fecha, sesion.hora)
                            showModal = true
                        }
                    )
                }
            }
        }
    }

    // --- EL MODAL (DIALOG) ---
    if (showModal && sesionSeleccionada != null) {
        AlertDialog(
            onDismissRequest = {
                showModal = false
                mensajeExitoModal = null // Limpiamos el mensaje al cerrar
            },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Column {
                    Text("Alumnos Apuntados", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${sesionSeleccionada?.fecha} | ${sesionSeleccionada?.hora}", color = VerdeApp, fontSize = 14.sp)

                    // --- MENSAJE DE ÉXITO VISIBLE Y CLARO ---
                    mensajeExitoModal?.let { texto ->
                        Surface(
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            color = VerdeApp.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = texto,
                                color = VerdeApp,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (viewModel.alumnosEnSesion.isEmpty()) {
                        Text("No hay alumnos apuntados aún.", color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(viewModel.alumnosEnSesion) { alumno ->
                                FilaAlumnoAdmin(alumno) {
                                    viewModel.eliminarReservaDeAlumno(
                                        alumno = alumno,
                                        fecha = sesionSeleccionada?.fecha ?: ""
                                    ) {
                                        // Mostramos el mensaje en el modal
                                        mensajeExitoModal = "Usuario eliminado correctamente"

                                        // Lo quitamos automáticamente a los 3 segundos
                                        scope.launch {
                                            kotlinx.coroutines.delay(3000)
                                            mensajeExitoModal = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showModal = false
                    mensajeExitoModal = null
                }) {
                    Text("Cerrar", color = VerdeApp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun FilaAlumnoAdmin(alumno: UsuarioApuntado, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${alumno.nombre} ${alumno.apellidos}", color = Color.White, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF8A80))
            }
        }
    }
}

@Composable
fun DiaExpandibleItem(fecha: String, horas: List<Reservas>, onHoraClick: (Reservas) -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expandido = !expandido },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = VerdeApp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = fecha, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = if(expandido) "▲" else "▼", color = Color.Gray)
            }

            if (expandido) {
                Divider(color = Color.DarkGray)
                horas.sortedBy { it.hora }.forEach { sesion ->
                    ListItem(
                        headlineContent = { Text("Hora: ${sesion.hora}", color = Color.White) },
                        supportingContent = { Text("Plazas libres: ${sesion.plazas}", color = VerdeApp) },
                        trailingContent = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
                        modifier = Modifier.clickable { onHoraClick(sesion) }, // <--- LLAMADA AQUÍ
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun UsuarioReservadoItem(reserva: UsuarioApuntado, onEliminar: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${reserva.nombre} ${reserva.apellidos}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Hora: ${reserva.hora} | DNI: ${reserva.dni}",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }

            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF8A80))
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Cancelar Reserva", color = Color.White) },
            text = { Text("¿Eliminar a ${reserva.nombre} de esta sesión?", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar()
                    showConfirm = false
                }) {
                    Text("Eliminar", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Volver", color = VerdeApp)
                }
            }
        )
    }
}