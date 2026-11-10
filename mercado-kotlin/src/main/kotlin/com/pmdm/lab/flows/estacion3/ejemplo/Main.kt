package com.pmdm.lab.flows.estacion3.ejemplo

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion3.MercadoInestable
import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

/**
 * EJEMPLO RESUELTO · Estación 3.
 *
 * Antes de ejecutarlo, PREDICE: con 2 fallos antes de acertar, 100 ms por petición y 200 ms de pausa,
 * ¿cuánto tarda en conseguir el detalle?
 */
fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())

    println("--- Reintentos ---")
    val inestable = MercadoInestable(MercadoSimulado(scope, Caos(latenciaMs = 100)), fallosAntesDeAcertar = 2)
    val tiempo = measureTimeMillis {
        val detalle = cargarDetalleResiliente(inestable, juegoId = 1)
        println("Detalle conseguido: ${detalle.genero}")
    }
    println("Tardó $tiempo ms (3 intentos de 100 ms + 2 pausas de 200 ms)")

    println("--- Límite de tiempo ---")
    val lento = MercadoSimulado(scope, Caos(latenciaMs = 5_000))
    val sinRespuesta = measureTimeMillis {
        try {
            cargarDetalleResiliente(lento, juegoId = 1, intentos = 2, limiteMs = 500)
        } catch (e: ErrorMercado) {
            println("Rendido: ${e.message}")
        }
    }
    println("Tardó $sinRespuesta ms (2 intentos de 500 ms + 1 pausa de 200 ms)")

    println("--- Cancelar al cerrar la pantalla ---")
    val pantalla = PantallaDeDetalle(MercadoSimulado(scope, Caos(latenciaMs = 300)), scope)
    pantalla.abrir(1)
    delay(100)
    println("Estado a los 100 ms: ${pantalla.estado}")
    pantalla.cerrar()
    delay(500)
    println("Estado 500 ms después de cerrar: ${pantalla.estado}   (la respuesta nunca se aplicó)")

    scope.cancel()
}
