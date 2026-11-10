package com.pmdm.lab.flows.api

import com.pmdm.lab.flows.modelo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * El mercado de verdad: habla con el servidor de tu pareja por HTTP.
 * No hace falta leerlo para el laboratorio: es una caja negra que cumple `MercadoApi`.
 *
 * - Las peticiones únicas son llamadas HTTP normales.
 * - Los flujos llegan por SSE (*Server-Sent Events*): una conexión abierta por la que el servidor va enviando datos.
 *
 * @param scope donde viven las conexiones de los flujos calientes (`ofertas` y `resumen`).
 */
class MercadoRemoto(private val base: String, scope: CoroutineScope) : MercadoApi {

    private val cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()
    private val json = Json { ignoreUnknownKeys = true }

    // ---------- Peticiones únicas ----------

    private suspend inline fun <reified T> pedir(ruta: String): T {
        val peticion = HttpRequest.newBuilder(URI.create(base + ruta)).GET().build()
        val respuesta = try {
            cliente.sendAsync(peticion, HttpResponse.BodyHandlers.ofString()).await()
        } catch (e: IOException) {
            throw ErrorMercado("Sin conexión con $base (${e.javaClass.simpleName})")
        }
        if (respuesta.statusCode() != 200) {
            throw ErrorMercado("HTTP ${respuesta.statusCode()} en $ruta")
        }
        return json.decodeFromString(serializer<T>(), respuesta.body())
    }

    override suspend fun catalogo(): List<Juego> = pedir("/catalogo")
    override suspend fun detalle(juegoId: Int): Detalle = pedir("/juegos/$juegoId/detalle")
    override suspend fun perfil(jugadorId: Int): Perfil = pedir("/jugadores/$jugadorId/perfil")
    override suspend fun logros(jugadorId: Int): List<Logro> = pedir("/jugadores/$jugadorId/logros")
    override suspend fun amigos(jugadorId: Int): List<Amigo> = pedir("/jugadores/$jugadorId/amigos")
    override suspend fun estadisticas(): Estadisticas = pedir("/estadisticas")

    // ---------- Flujos por SSE ----------

    /** Una conexión SSE nueva por cada collect: por eso `precios` y `ranking` son flujos FRÍOS. */
    private inline fun <reified T> sse(ruta: String): Flow<T> = flow {
        val peticion = HttpRequest.newBuilder(URI.create(base + ruta))
            .header("Accept", "text/event-stream").GET().build()
        val respuesta = try {
            cliente.sendAsync(peticion, HttpResponse.BodyHandlers.ofInputStream()).await()
        } catch (e: IOException) {
            throw ErrorMercado("Sin conexión con $base (${e.javaClass.simpleName})")
        }
        if (respuesta.statusCode() != 200) throw ErrorMercado("HTTP ${respuesta.statusCode()} en $ruta")
        respuesta.body().bufferedReader().use { lector ->
            while (true) {
                val linea = try {
                    runInterruptible(Dispatchers.IO) { lector.readLine() }
                } catch (e: IOException) {
                    throw ErrorMercado("Conexión perdida con $base")
                } ?: break
                if (linea.startsWith("data:")) {
                    emit(json.decodeFromString(serializer<T>(), linea.removePrefix("data:").trim()))
                }
            }
        }
    }

    override fun precios(juegoId: Int): Flow<Precio> = sse("/sse/precios/$juegoId")
    override fun ranking(): Flow<List<Puesto>> = sse("/sse/ranking")

    /**
     * Los flujos CALIENTES se abren una sola vez y se comparten con todos (`shareIn` / `stateIn`):
     * da igual cuántos colectores haya, hay una única conexión. Si se cae, reintenta.
     */
    override val ofertas: SharedFlow<Oferta> = sse<Oferta>("/sse/ofertas")
        .retryWhen { _, _ -> delay(1_500); true }
        .shareIn(scope, SharingStarted.Eagerly)

    override val resumen: StateFlow<ResumenMercado> = sse<ResumenMercado>("/sse/resumen")
        .retryWhen { _, _ -> delay(1_500); true }
        .stateIn(scope, SharingStarted.Eagerly, ResumenMercado())
}
