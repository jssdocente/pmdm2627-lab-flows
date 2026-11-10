package com.pmdm.lab.flows.estacion5.ejemplo

import com.pmdm.lab.flows.api.Configuracion
import kotlinx.coroutines.*

/**
 * EJEMPLO RESUELTO · Estación 5.
 *
 * Antes de ejecutarlo, PREDICE:
 *  1. Tres colectores se enganchan a `precioEnVivo`: ¿cuántas conexiones abre el servidor?
 *  2. Todos se van, y uno nuevo llega 2 s después: ¿se reconecta? ¿Y si llega 6 s después?
 */
fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    val api = Configuracion.crearApi(scope)
    val cotizador = CotizadorDeJuego(api, juegoId = 1, scope = scope)

    suspend fun conexiones() = api.estadisticas().conexionesPrecios

    println("Antes de que nadie escuche: ${conexiones()} conexiones (el flujo no se conecta hasta el primer colector)")

    val tres = List(3) { n ->
        launch { cotizador.precioEnVivo.collect { it?.let { p -> println("  colector ${n + 1} ve $p") } } }
    }
    delay(1_200)
    println("Con 3 colectores: ${conexiones()} conexión(es)")

    tres.forEach { it.cancelAndJoin() }
    println("Se van todos. Espero 2 s (menos de ${CotizadorDeJuego.PARADA_TRAS_MS} ms)…")
    delay(2_000)
    val b = launch { cotizador.precioEnVivo.collect { } }
    delay(700)
    println("Llega uno nuevo: ${conexiones()} conexión(es)  → no se ha reconectado")
    b.cancelAndJoin()

    println("Se va. Espero 6 s (más de ${CotizadorDeJuego.PARADA_TRAS_MS} ms)…")
    delay(6_000)
    val c = launch { cotizador.precioEnVivo.collect { } }
    delay(700)
    println("Llega otro: ${conexiones()} conexión(es)  → ahora sí se ha vuelto a conectar")
    c.cancelAndJoin()

    scope.cancel(CancellationException("fin"))
}
