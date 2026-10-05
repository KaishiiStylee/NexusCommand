package com.nexus.command.routes

import com.nexus.command.model.Command
import com.nexus.command.registry.DeviceRegistry
import com.nexus.command.ws.handleClient
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private val json = Json { encodeDefaults = true }

fun Route.apiRoutes() {

    webSocket("/ws/client") { handleClient() }

    route("/api") {

        get("/devices") {
            call.respondText(
                json.encodeToString(DeviceRegistry.snapshot()),
                ContentType.Application.Json
            )
        }

        post("/command") {
            val cmd = json.decodeFromString(Command.serializer(), call.receiveText())
            val session = DeviceRegistry.sessionOf(cmd.target)
            if (session == null) {
                call.respond(HttpStatusCode.NotFound, "device offline")
                return@post
            }
            val payload = buildJsonObject {
                put("type", "command")
                putJsonObject("command") {
                    put("target", cmd.target)
                    put("action", cmd.action)
                    put("issuedAt", cmd.issuedAt)
                }
            }.toString()
            session.send(Frame.Text(payload))
            call.respondText("""{"ok":true}""", ContentType.Application.Json)
        }
    }
}
