package com.example.appgeneratemac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgeneratemac.model.Maquina
import com.example.appgeneratemac.model.MaquinaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import android.util.Log
import com.example.appgeneratemac.service.RetrofitClient

class MaquinaViewModel: ViewModel() {
    private val _maquinas = MutableStateFlow<List<Maquina>>(emptyList())
    val maquina: StateFlow<List<Maquina>> = _maquinas

    private val _maquinaSeleccionada = MutableStateFlow<Maquina?>(null)
    val maquinaSeleccionada: StateFlow<Maquina?> = _maquinaSeleccionada

    fun cargarMaquinas(deviceId: String) {
        viewModelScope.launch {
            try {
                var response = RetrofitClient.api.getMaquinas(deviceId)
                if (response.code == 200) {
                    val lista = response.data
                    _maquinas.value = lista

                    //Buscar la máquina con mac === 1
                    val seleccionada = lista.find { it.mac == 1 }

                    _maquinaSeleccionada.value = seleccionada
                } else {
                    Log.e("MaquinaViewModel", "Error al obtener datos: ${response.msn}")
                }
            } catch (e: Exception) {
                Log.e("MaquinaViewModel", "Error al obtener datos", e)
            }
        }
    }

    fun seleccionarMaquina(maquina: Maquina) {
        _maquinaSeleccionada.value = maquina
    }
}