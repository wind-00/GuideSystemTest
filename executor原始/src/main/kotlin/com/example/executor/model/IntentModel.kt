package com.example.executor.model

import kotlinx.serialization.Serializable

/**
 * Intent Model data class representing the intents of the app
 */
@Serializable
data class IntentModel(
    val intents: List<Intent>
)

/**
 * Intent data class representing a single intent in the app
 */
@Serializable
data class Intent(
    val intentId: String,
    val type: String,
    val expectedNextStateIds: List<String>
)
