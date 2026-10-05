package com.nexus.command.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val model: String,
    val ipv6: String,
    val battery: Int,
    val charging: Boolean,
    val lastSeen: Long,
    val online: Boolean = true
)
