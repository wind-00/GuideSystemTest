package com.example.executor.model

import kotlinx.serialization.Serializable

/**
 * State Model data class representing the state management of the app
 */
@Serializable
data class StateModel(
    val states: List<State>,
    val initialStateId: String
)

/**
 * State data class representing a single state in the app
 */
@Serializable
data class State(
    val stateId: String,
    val name: String,
    val description: String,
    val signals: List<Signal>,
    val relatedPageIds: List<String>
)

/**
 * Signal data class representing a condition for a state
 */
@Serializable
data class Signal(
    val type: String,
    val target: String,
    val expectedValue: String? = null,
    val matcher: String
)
