package com.nexus.command.model

import kotlinx.serialization.Serializable

@Serializable
data class Command(
    val target: String,
    val action: String,
    val issuedAt: Long = System.currentTimeMillis()
)

@Serializable
data class Ack(
    val target: String,
    val action: String,
    val ok: Boolean,
    val at: Long = System.currentTimeMillis()
)
