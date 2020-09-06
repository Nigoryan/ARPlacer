package com.example.arplacer.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object LocationInformationRepository {
    private lateinit var service: LocationInformationService

    fun getLocationInformationService(): LocationInformationService {
        if (!::service.isInitialized) {
            val url = "https://api.opentripmap.com/0.1/ru/"
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            service = retrofit.create(LocationInformationService::class.java)
        }
        return service
    }

    private fun getClient(): OkHttpClient {

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .callTimeout(3, TimeUnit.SECONDS)
        return client.build()
    }
}