package com.nexus.command.registry

import com.nexus.command.model.Device
import io.ktor.server.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

object DeviceRegistry {

    private val devices = ConcurrentHashMap<String, Device>()
    private val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val lock = Mutex()

    suspend fun register(device: Device, session: DefaultWebSocketServerSession) = lock.withLock {
        devices[device.id] = device
        sessions[device.id] = session
    }

    suspend fun unregister(id: String) = lock.withLock {
        sessions.remove(id)
        devices[id]?.let { devices[id] = it.copy(online = false, lastSeen = System.currentTimeMillis()) }
    }

    suspend fun update(device: Device) = lock.withLock {
        devices[device.id] = device
    }

    fun snapshot(): List<Device> = devices.values.sortedByDescending { it.lastSeen }

    fun sessionOf(id: String): DefaultWebSocketServerSession? = sessions[id]
}
