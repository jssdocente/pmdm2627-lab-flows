package com.pmdm.lab.flows.estacion4.ejemplo

import com.pmdm.lab.flows.api.Configuracion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

/**
 * EJEMPLO RESUELTO · Estación 4.
 *
 * Antes de ejecutarlo, PREDICE:
 *  1. Si 3 colectores se enganchan al flujo frío `precios`, ¿cuántas conexiones ve el servidor?
 *  2. Un colector llega 3 s tarde al flujo caliente `ofertas`: ¿verá la oferta que salió a los 2 s?
 */
fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    val api = Configuracion.crearApi(scope)

    println("--- Flujo CALIENTE: ofertas ---")
    println("Llego 3 s tarde y escucho 3,5 s…")
    val vistas = ofertasParaColectorTardio(api, esperaMs = 3_000, duracionMs = 3_500)
    vistas.forEach { println("  Oferta vista: ${it.titulo} (-${it.descuento} %)") }
    println("Ofertas emitidas en total: ${api.resumen.value.ofertasEmitidas}  ·  yo he visto: ${vistas.size}")

    println("--- Flujo FRÍO: precios ---")
    val una = contarConexionesPrecios(api, juegoId = 1, colectores = 1)
    println("Con 1 colector, conexiones abiertas en total: $una")
    val tres = contarConexionesPrecios(api, juegoId = 1, colectores = 3)
    println("Con 3 colectores más, conexiones abiertas en total: $tres")

    scope.cancel(CancellationException("fin"))
}
