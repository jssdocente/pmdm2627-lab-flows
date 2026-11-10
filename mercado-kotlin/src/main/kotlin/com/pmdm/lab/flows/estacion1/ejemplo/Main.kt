package com.pmdm.lab.flows.estacion1.ejemplo

import com.pmdm.lab.flows.api.Configuracion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * EJEMPLO RESUELTO · Estación 1.
 *
 * Antes de ejecutarlo, PREDICE: con el catálogo de 6 juegos y 300 ms por petición,
 * ¿cuánto tardará la versión secuencial? ¿Y la paralela?
 */
fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    val api = Configuracion.crearApi(scope)

    val secuencial = measureTimeMillis { cargarVitrinaSecuencial(api) }
    println("Secuencial: $secuencial ms")

    val paralela = measureTimeMillis { cargarVitrinaParalela(api) }
    println("Paralela:   $paralela ms")

    scope.cancel(CancellationException("fin del ejemplo"))
}
