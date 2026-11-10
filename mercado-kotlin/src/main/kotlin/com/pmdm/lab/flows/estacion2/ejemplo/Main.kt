package com.pmdm.lab.flows.estacion2.ejemplo

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.*

/**
 * EJEMPLO RESUELTO · Estación 2.
 *
 * Antes de ejecutarlo, PREDICE: con el servidor fallando en «detalle»,
 * ¿qué imprime la versión «todo o nada»? ¿Y la «resistente»?
 */
fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    val api = MercadoSimulado(scope, Caos(latenciaMs = 300, siempreFallan = setOf("detalle")))

    println("--- Todo o nada (coroutineScope) ---")
    try {
        cargarPortadaTodoONada(api)
        println("Portada cargada")
    } catch (e: ErrorMercado) {
        println("La portada entera ha fallado: ${e.message}")
    }

    println("--- Cada uno por su lado (supervisorScope) ---")
    val portada = cargarPortadaResistente(api)
    println("Juegos: ${portada.juegos.size}")
    println("Destacado: ${portada.destacado.fold({ "OK" }, { "no disponible (${it.message})" })}")

    println("--- Un scope propio y su cancelación ---")
    val sesion = CoroutineScope(SupervisorJob())
    val vigilante = VigilanteDeCatalogo(MercadoSimulado(scope, Caos(latenciaMs = 100)), sesion)
    vigilante.iniciar()
    delay(3_500)
    println("Hijos vivos del scope: ${sesion.coroutineContext[Job]!!.children.count()}")
    println("Refrescos tras 3,5 s: catálogo=${vigilante.refrescosDeCatalogo}, destacado=${vigilante.refrescosDeDestacado}")
    vigilante.detener()
    sesion.coroutineContext[Job]!!.join()
    println("Tras detener(): hijos vivos = ${sesion.coroutineContext[Job]!!.children.count()}")

    scope.cancel()
}
