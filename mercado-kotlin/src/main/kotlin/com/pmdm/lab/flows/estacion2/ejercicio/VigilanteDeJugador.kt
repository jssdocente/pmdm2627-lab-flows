package com.pmdm.lab.flows.estacion2.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.estacion2.intentar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * TU TURNO · parte 2.
 *
 * Refresca los logros y los amigos de un jugador cada segundo, **cada uno en su propia corrutina**
 * dentro del `scope` que recibe. Los contadores ya están hechos: tú escribes `iniciar` y `detener`.
 *
 * - `iniciar()`: lanza dos corrutinas con `scope.launch`. Cada una repite «pedir, contar, esperar
 *   `PERIODO_MS`». Si la petición falla, suma 1 a `fallos` y **sigue**: un fallo en los logros no puede
 *   detener el refresco de los amigos (¿dónde va el control del error?).
 * - `detener()`: cancela el scope y, con él, a las dos corrutinas.
 *
 * Pista: es `VigilanteDeCatalogo`, con otras dos peticiones.
 */
class VigilanteDeJugador(
    private val api: MercadoApi,
    private val jugadorId: Int,
    private val scope: CoroutineScope,
) {
    private val logros = AtomicInteger()
    private val amigos = AtomicInteger()
    private val errores = AtomicInteger()

    val refrescosDeLogros get() = logros.get()
    val refrescosDeAmigos get() = amigos.get()
    val fallos get() = errores.get()

    fun iniciar() {
        scope.launch {
            while (true) {
                if (intentar { api.logros(jugadorId) }.isSuccess) logros.incrementAndGet() else errores.incrementAndGet()
                delay(PERIODO_MS)
            }
        }
        scope.launch {
            while (true) {
                if (intentar { api.amigos(jugadorId) }.isSuccess) amigos.incrementAndGet() else errores.incrementAndGet()
                delay(PERIODO_MS)
            }
        }
    }

    fun detener() {
        scope.cancel()
    }

    companion object {
        const val PERIODO_MS = 1_000L
    }
}
