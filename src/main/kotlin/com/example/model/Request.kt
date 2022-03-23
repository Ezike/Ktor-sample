package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val emoji: String,
    val phrase: String
)