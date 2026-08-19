package com.example.matchmate.data.model

data class ApiResponse(
    val results: List<ApiUser>
)

data class ApiUser(
    val email: String,
    val gender: String,
    val name: Name,
    val dob: Dob,
    val location: Location,
    val picture: Picture
)

data class Name(
    val first: String,
    val last: String
)

data class Dob(
    val age: Int
)

data class Location(
    val city: String,
    val country: String
)

data class Picture(
    val large: String
)
