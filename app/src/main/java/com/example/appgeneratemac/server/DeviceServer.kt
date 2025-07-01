package com.example.appgeneratemac.server

import android.content.Context
import android.provider.Settings
import fi.iki.elonen.NanoHTTPD

class DeviceServer(port: Int, private val context: Context) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession?): Response {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val json = """{ "deviceId": "$deviceId" }"""
        return newFixedLengthResponse(Response.Status.OK, "application/json", json)
    }
}