package com.example.appgeneratemac.model

data class MaquinaResponse(
    val code: Int,
    val msn: String,
    val data: List<Maquina>
)
