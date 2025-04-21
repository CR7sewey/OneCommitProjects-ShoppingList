package com.mike.shoppinglist.api

import com.mike.shoppinglist.GeoCodingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoCodingService {

    @GET("maps/api/geocode/json")
    suspend fun getLocationName(
        @Query("latlng") latLng: String,
        @Query("key") apiKey: String
    ): Response<GeoCodingResponse>
}