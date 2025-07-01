package com.example.appgeneratemac.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.appgeneratemac.R
import com.example.appgeneratemac.server.DeviceServer
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import kotlin.concurrent.thread

class DeviceApiService : Service() {
    private var webServer: NanoHTTPD? = null
    private val channelId = "device_api_channel"

    override fun onCreate() {
        super.onCreate()
        // Mostrar notificación inmediatamente
        startForeground(1, crearNotificacionPersistente())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        iniciarServidorHttp()
        return START_STICKY
    }

    override fun onDestroy() {
        webServer?.stop()
        webServer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun iniciarServidorHttp() {
        if (webServer == null) {
            thread(start = true) {
                try {
                    webServer = object : NanoHTTPD(8080) {
                        override fun serve(session: IHTTPSession): Response {
                            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                            return when (session.uri) {
                                "/device" -> {
                                    val json = """{"deviceId":"$deviceId"}"""
                                    newFixedLengthResponse(Response.Status.OK, "application/json", json)
                                }
                                "/ip" -> {
                                    val ip = session.remoteIpAddress()
                                    val json = """{"ip":"$ip"}"""
                                    newFixedLengthResponse(Response.Status.OK, "application/json", json)
                                }
                                else -> {
                                    newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
                                }
                            }
                        }
                    }
                    webServer?.start()
                    Log.d("DeviceApiService", "Servidor HTTP iniciado en puerto 8080")
                } catch (e: Exception) {
                    Log.e("DeviceApiService", "Error al iniciar servidor HTTP", e)
                }
            }
        }
    }

    private fun IHTTPSession.remoteIpAddress(): String {
        return this.remoteIpAddress ?: "0.0.0.0"
    }

    private fun crearNotificacionPersistente(): Notification {
        val channelName = "API Device Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("API activa")
            .setContentText("Servidor escuchando en el puerto 8080")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }
}