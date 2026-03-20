package com.dpbprog.nlfreserve

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dpbprog.nlfreserve.viewmodels.AdminUsuariosViewModel
import com.dpbprog.nlfreserve.viewmodels.UsuarioDatos
import kotlinx.coroutines.launch

@Composable
fun AdminUsuariosScreen() {
    val viewModel: AdminUsuariosViewModel = viewModel()
    var textoBusqueda by remember { mutableStateOf("") }

    var showModal by remember { mutableStateOf(false) }
    var esEdicion by remember { mutableStateOf(false) }
    var usuarioAEditar by remember { mutableStateOf<UsuarioDatos?>(null) }

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var limiteReservas by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var patologiasEnfermedades by remember { mutableStateOf("") }
    var lesiones by remember { mutableStateOf("") }
    var infoAdicional by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Función auxiliar para limpiar campos
    fun limpiarCampos() {
        nombre = ""; apellidos = ""; dni = ""; password = ""
        limiteReservas = "0"; fechaNacimiento = ""; patologiasEnfermedades = ""
        lesiones = ""; infoAdicional = ""; activo = true
        usuarioAEditar = null
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        // --- BOTÓN FLOTANTE (+) ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    limpiarCampos()
                    esEdicion = false
                    showModal = true
                },
                containerColor = VerdeApp,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Usuario")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Gestión de Usuarios",
                color = VerdeApp,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                    viewModel.filtrar(it)
                },
                placeholder = { Text("Buscar por nombre o DNI...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = VerdeApp
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.usuariosFiltrados) { usuario ->
                    UsuarioFilaItem(usuario) {
                        // Cargar datos para editar
                        nombre = usuario.nombre;
                        apellidos = usuario.apellidos;
                        dni = usuario.dni
                        password = usuario.password;
                        limiteReservas = usuario.limiteReservas
                        fechaNacimiento = usuario.fechaNacimiento;
                        patologiasEnfermedades = usuario.patologiasEnfermedades
                        lesiones = usuario.lesiones;
                        infoAdicional = usuario.infoAdicional
                        activo = usuario.activo
                        usuarioAEditar = usuario
                        esEdicion = true
                        showModal = true
                    }
                }
            }
        }
    }

    if (showModal) {
        AlertDialog(
            onDismissRequest = { showModal = false },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text(
                    text = if (esEdicion) "Editar Usuario" else "Nuevo Usuario",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CampoEdicion("Nombre", nombre) { nombre = it }
                    CampoEdicion("Apellidos", apellidos) { apellidos = it }
                    CampoEdicion("DNI", dni) { dni = it }
                    CampoEdicion("Password", password) { password = it }
                    CampoEdicion("Límite Reservas Semanales", limiteReservas) { limiteReservas = it }
                    CampoEdicion("Fecha Nacimiento", fechaNacimiento) { fechaNacimiento = it }
                    CampoEdicion("Patologías", patologiasEnfermedades, isLongText = true) { patologiasEnfermedades = it }
                    CampoEdicion("Lesiones", lesiones, isLongText = true) { lesiones = it }
                    CampoEdicion("Información Adicional", infoAdicional, isLongText = true) { infoAdicional = it }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Usuario Activo", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(checked = activo, onCheckedChange = { activo = it }, colors = SwitchDefaults.colors(checkedThumbColor = VerdeApp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mapa = mapOf(
                            "nombre" to nombre, "apellidos" to apellidos, "dni" to dni,
                            "password" to password, "limiteReservas" to limiteReservas,
                            "fechaNacimiento" to fechaNacimiento, "patologiasEnfermedades" to patologiasEnfermedades,
                            "lesiones" to lesiones, "infoAdicional" to infoAdicional,
                            "activo" to activo, "admin" to false // Por defecto no es admin
                        )

                        if (esEdicion) {
                            viewModel.actualizarUsuario(usuarioAEditar!!.dni, mapa) {
                                showModal = false
                                scope.launch { snackbarHostState.showSnackbar("Usuario actualizado") }
                            }
                        } else {
                            viewModel.crearUsuario(mapa) {
                                showModal = false
                                scope.launch { snackbarHostState.showSnackbar("Usuario creado con éxito") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeApp)
                ) {
                    Text("GUARDAR", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showModal = false }) { Text("CANCELAR", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun CampoEdicion(label: String, value: String, isLongText: Boolean = false, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = VerdeApp, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        minLines = if (isLongText) 3 else 1,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF2B2B2B),
            unfocusedContainerColor = Color(0xFF2B2B2B),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = VerdeApp,
            focusedIndicatorColor = VerdeApp
        )
    )
}

@Composable
fun UsuarioFilaItem(usuario: UsuarioDatos, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (!usuario.activo) Color.Red.copy(alpha = 0.2f) else VerdeApp.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario.nombre.take(1).uppercase(),
                        color = if (!usuario.activo) Color.Red else VerdeApp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${usuario.nombre} ${usuario.apellidos}", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "DNI: ${usuario.dni}", color = Color.Gray, fontSize = 13.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.DarkGray)
        }
    }
}