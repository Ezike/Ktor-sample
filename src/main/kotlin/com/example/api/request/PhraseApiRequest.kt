package com.example.api.request

import kotlinx.serialization.Serializable

@Serializable
data class PhraseApiRequest(
    val emoji: String,
    val phrase: String
)
