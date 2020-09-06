package com.example.arplacer.network

import com.example.arplacer.entity.AdditionalInformResponse
import com.example.arplacer.entity.LocationInformationResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LocationInformationService {
    @GET("places/radius")
    suspend fun getLocationInform(
        @Query("radius") radius: Int,
        @Query("lon") longitude: Double,
        @Query("lat") latitude: Double,
        @Query("format") format: String,
        @Query("apikey") apikey: String
    ): MutableList<LocationInformationResponse>

    @GET("places/xid/{xid}")
    suspend fun getMoreInformAboutPlace(
        @Path("xid") xid: String?,
        @Query("apikey") apikey: String
    ): AdditionalInformResponse
}