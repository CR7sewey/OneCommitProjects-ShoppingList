package com.mike.shoppinglist

data class GeoCodingResult(
    val formatted_address: String = "",
)

data class GeoCodingResponse(
    val results: List<GeoCodingResult> = emptyList<GeoCodingResult>(),
    val status: String = "",
)