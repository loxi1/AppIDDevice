package com.example.appgeneratemac

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appgeneratemac.model.Maquina
import com.example.appgeneratemac.service.RetrofitClient
import kotlinx.coroutines.launch
import kotlin.collections.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgeneratemac.viewmodel.MaquinaViewModel
import kotlinx.coroutines.Dispatchers
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.appgeneratemac.service.DeviceApiService
import android.os.PowerManager
import android.app.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        setContent {
            MaterialTheme {
                MaquinaDropdownConBusqueda(androidId)
            }
        }
        startService(Intent(this, DeviceApiService::class.java))
        solicitarIgnorarOptimizaciones(this)
        // ✅ Solo muestra advertencia si aún está optimizado
        verificarYMostrarAdvertenciaDeBateria(this)
    }
}

@Composable
fun MaquinaDropdownConBusqueda(
   deviceId: String
) {
    val context = LocalContext.current
    val viewModel: MaquinaViewModel = viewModel()

    val maquinas by viewModel.maquina.collectAsState()
    val selectedMaquina by viewModel.maquinaSeleccionada.collectAsState()

    var showDialog by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    // Verificar optimización de batería
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = context.packageName
    val estaOptimizado = !pm.isIgnoringBatteryOptimizations(packageName)

    //Cargar máquinas solo una vez
    LaunchedEffect(Unit) {
        viewModel.cargarMaquinas(deviceId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Máquina seleccionada:", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = { showDialog = true },
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedMaquina?.maquina ?: "Seleccionar máquina",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        selectedMaquina?.let {
                            if (it.id != 0) {
                                try {
                                    val response = com.example.appgeneratemac.service.RetrofitClient.api.SaveMaquina(
                                        deviceId = deviceId,
                                        maquinaId = it.id
                                    )
                                    mensaje = response.msn
                                } catch (e: Exception) {
                                    mensaje = "Error al guardar máquina"
                                }
                            } else {
                                Toast.makeText(context, "Debes seleccionar una máquina válida", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = selectedMaquina != null,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }

            mensaje?.let {
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(3000) //Espera 3 segundos
                    mensaje = null //Borras mensaje
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 🔋 Botón de advertencia de batería (solo si está optimizado)
            if (estaOptimizado) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            val activity = (context as? Activity)
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            context.startActivity(intent)

                            // ❌ Cierra completamente la app
                            activity?.finishAffinity()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Advertencia")
                        Spacer(Modifier.width(8.dp))
                        Text("Batería optimizada")
                    }
                }
            }
        }



        if (showDialog) {
            MaquinaSelectorDialog(
                maquinas = maquinas,
                onSelect = {
                    viewModel.seleccionarMaquina(it)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaquinaSelectorDialog(
    maquinas: List<Maquina>,
    onSelect: (Maquina) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val maquinasFiltradas = remember(query, maquinas) {
        if (query.isBlank()) maquinas
        else maquinas.filter { it.maquina.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {}, // No es necesario aquí
        title = { Text("Seleccionar máquina", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar máquina") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(maquinasFiltradas.size) { index ->
                        val maquina = maquinasFiltradas[index]
                        ListItem(
                            headlineText = { Text(maquina.maquina) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(maquina)
                                }
                        )
                    }
                }
            }
        }
    )
}

private fun solicitarIgnorarOptimizaciones(context: Context) {
    val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
    val packageName = context.packageName

    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
        context.startActivity(intent)
    }
}

fun verificarYMostrarAdvertenciaDeBateria(activity: Activity) {
    val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = activity.packageName

    // Solo mostrar si está bajo optimización
    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        AlertDialog.Builder(activity)
            .setTitle("⚠️ Batería optimizada")
            .setMessage(
                """
                Esta aplicación necesita ejecutarse automáticamente al encender el dispositivo.

                Para garantizar que funcione correctamente, cambia la batería a “Sin restricciones”.

                Ir a:
                Ajustes > Aplicaciones > ${activity.getString(R.string.app_name)} > Batería > Sin restricciones.
                """.trimIndent()
            )
            .setCancelable(false)
            .setPositiveButton("Ir a ajustes") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                activity.startActivity(intent)

                // Cierra completamente la app
                activity.finishAffinity()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
