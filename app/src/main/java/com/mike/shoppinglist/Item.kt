package com.mike.shoppinglist

data class Item(
    val id: String = "",
    var name: String = "",
    var quantity: String = "1",
    var isEditing: Boolean = false,
    var location: Location = Location(40.0, -8.0),
)
