package com.mike.shoppinglist.api

import com.mike.shoppinglist.GeoCodingResponse

class GeoCodingRepository(private val service: GeoCodingService): IGeoCodingRepository  {

    override suspend fun getLocationName(latLng: String, apiKey: String): Result<GeoCodingResponse> {
        return try {
            val response = service.getLocationName(latLng, apiKey)
            if (response.isSuccessful) {
                Result.success(response.body() ?: GeoCodingResponse())
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}