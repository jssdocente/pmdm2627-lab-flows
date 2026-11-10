package com.pmdm.lab.flows.estacion1.ejercicio

import com.pmdm.lab.flows.api.Configuracion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/** Ejecuta tu solución contra el mercado configurado y mide cuánto tarda. */
fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    val api = Configuracion.crearApi(scope)

    val tiempoFicha = measureTimeMillis {
        val ficha = cargarFicha(api, jugadorId = 1)
        println("Ficha de ${ficha.perfil.nombre}: ${ficha.logros.size} logros, ${ficha.amigos.size} amigos")
    }
    println("cargarFicha:          $tiempoFicha ms (objetivo: ~300 ms)")

    val tiempoAmigos = measureTimeMillis {
        val fichas = cargarFichasDeAmigos(api, jugadorId = 1)
        println("Fichas de amigos: ${fichas.map { it.perfil.nombre }}")
    }
    println("cargarFichasDeAmigos: $tiempoAmigos ms (objetivo: ~600 ms)")

    scope.cancel(CancellationException("fin"))
}
