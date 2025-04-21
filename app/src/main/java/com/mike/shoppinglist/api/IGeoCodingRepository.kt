package com.mike.shoppinglist.api

import com.mike.shoppinglist.GeoCodingResponse

interface IGeoCodingRepository {
    suspend fun getLocationName(latLng: String, apiKey: String): Result<GeoCodingResponse>
}