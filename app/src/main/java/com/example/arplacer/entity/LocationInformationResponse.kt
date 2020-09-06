package com.example.arplacer.entity

data class LocationInformationResponse(
    val xid: String?,
    val name: String?,
    val dist: Double?,
    val rate: Int?,
    val kinds: String?,
    val point: PlacePoint?
)