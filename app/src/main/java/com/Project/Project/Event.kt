package com.Project.Project

data class Event(
    val id: Int = 0,
    val name: String,
    val location: String,
    val date: String,
    val time: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)
