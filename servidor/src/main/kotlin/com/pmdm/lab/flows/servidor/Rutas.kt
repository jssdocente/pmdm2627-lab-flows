package com.pmdm.lab.flows.servidor

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.modelo.ErrorMercado
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class Error503(val error: String)

private val json = Json

/** Responde con el valor, o con un 503 si el mercado simulado decide fallar. */
private suspend inline fun <reified T : Any> ApplicationCall.responder(bloque: () -> T) {
    try {
        respond(bloque())
    } catch (e: ErrorMercado) {
        respond(HttpStatusCode.ServiceUnavailable, Error503(e.message ?: "no disponible"))
    }
}

fun Application.configurar(mercado: MercadoSimulado) {
    install(ContentNegotiation) { json() }
    install(SSE)

    routing {
        // ---------- Peticiones únicas ----------
        get("/catalogo") { call.responder { mercado.catalogo() } }
        get("/juegos/{id}/detalle") { call.responder { mercado.detalle(call.parameters["id"]!!.toInt()) } }
        get("/jugadores/{id}/perfil") { call.responder { mercado.perfil(call.parameters["id"]!!.toInt()) } }
        get("/jugadores/{id}/logros") { call.responder { mercado.logros(call.parameters["id"]!!.toInt()) } }
        get("/jugadores/{id}/amigos") { call.responder { mercado.amigos(call.parameters["id"]!!.toInt()) } }
        get("/estadisticas") { call.respond(mercado.estadisticas()) }

        // ---------- Flujos (SSE) ----------
        sse("/sse/precios/{id}") { emitir(mercado.precios(call.parameters["id"]!!.toInt())) }
        sse("/sse/ranking") { emitir(mercado.ranking()) }
        sse("/sse/ofertas") { emitir(mercado.ofertas) }
        sse("/sse/resumen") { emitir(mercado.resumen) }

        // ---------- Panel de control ----------
        get("/") { call.respondText(panelHtml(), ContentType.Text.Html) }
        get("/admin/caos") { call.respond(mercado.caos) }
        post("/admin/caos") {
            val formulario = call.receiveParameters()
            mercado.caos = Caos(
                latenciaMs = formulario["latenciaMs"]?.toLongOrNull()?.coerceIn(0, 10_000) ?: mercado.caos.latenciaMs,
                probabilidadFallo = (formulario["fallo"]?.toDoubleOrNull() ?: (mercado.caos.probabilidadFallo * 100))
                    .coerceIn(0.0, 100.0) / 100,
                siempreFallan = formulario["siempreFallan"].orEmpty()
                    .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
            )
            call.respond(mercado.caos)
        }
    }
}

private suspend inline fun <reified T> io.ktor.server.sse.ServerSSESession.emitir(flujo: Flow<T>) {
    flujo.collect { send(ServerSentEvent(data = json.encodeToString(it))) }
}
