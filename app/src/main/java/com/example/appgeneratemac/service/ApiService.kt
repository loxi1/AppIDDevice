package com.example.appgeneratemac.service

import com.example.appgeneratemac.model.Maquina
import com.example.appgeneratemac.model.MaquinaResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("scriptcase/app/eCorporativoM/get_maquina/")
    suspend fun getMaquinas(@Query("deviceid") deviceId: String): MaquinaResponse

    @FormUrlEncoded
    @POST("scriptcase/app/eCorporativoM/save_maquina/?nmgp_outra_jan=true")
    suspend fun SaveMaquina(
        @Field("id") deviceId: String,
        @Field("maquina") maquinaId: Int
    ): MaquinaResponse
}
