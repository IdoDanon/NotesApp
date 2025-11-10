package me.idodanon.locationbasednotes.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val userEmail: String,
    val title: String,
    val body: String,
    val date: Long = System.currentTimeMillis(),
    val location: String,
    val imageUri: String? = null
)