package com.nexus.command.ws

import com.nexus.command.model.Ack
import com.nexus.command.model.Device
import com.nexus.command.registry.DeviceRegistry
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

suspend fun DefaultWebSocketServerSession.handleClient() {
    var deviceId: String? = null
    try {
        for (frame in incoming) {
            if (frame !is Frame.Text) continue
            val root = json.parseToJsonElement(frame.readText()).jsonObject
            when (root["type"]?.jsonPrimitive?.contentOrNull) {
                "register" -> {
                    val d = json.decodeFromJsonElement(Device.serializer(), root["device"]!!)
                    deviceId = d.id
                    DeviceRegistry.register(d, this)
                }
                "ack" -> {
                    json.decodeFromJsonElement(Ack.serializer(), root["ack"]!!)
                }
                "heartbeat" -> {
                    deviceId?.let { id ->
                        DeviceRegistry.snapshot().find { it.id == id }?.let { d ->
                            DeviceRegistry.update(d.copy(lastSeen = System.currentTimeMillis(), online = true))
                        }
                    }
                }
            }
        }
    } catch (_: ClosedReceiveChannelException) {
    } finally {
        deviceId?.let { DeviceRegistry.unregister(it) }
    }
}
