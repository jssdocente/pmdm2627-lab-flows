package com.pmdm.lab.flows.estacion1.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Detalle
import com.pmdm.lab.flows.modelo.Juego
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Lo que muestra la pantalla de la tienda: los juegos y la ficha de cada uno. */
class VitrinaDeJuegos(val juegos: List<Juego>, val detalles: List<Detalle>)

/**
 * VERSIÓN 1 · SECUENCIAL.
 *
 * Cada llamada `suspend` espera a que termine la anterior. No se bloquea ningún hilo
 * (eso lo hace la corrutina), pero el tiempo total es la SUMA de todas las esperas.
 */
suspend fun cargarVitrinaSecuencial(api: MercadoApi): VitrinaDeJuegos {
    val juegos = api.catalogo()                       // 1 espera
    val detalles = juegos.map { api.detalle(it.id) }  // y otra espera por cada juego, una tras otra
    return VitrinaDeJuegos(juegos, detalles)
}

/**
 * VERSIÓN 2 · EN PARALELO.
 *
 * Hay una dependencia: los detalles se piden por id, así que primero hace falta el catálogo.
 * Pero los detalles NO dependen unos de otros: se lanzan todos a la vez con `async`.
 *
 * Patrón: «lo dependiente, en orden; lo independiente, a la vez».
 */
suspend fun cargarVitrinaParalela(api: MercadoApi): VitrinaDeJuegos = coroutineScope {
    val juegos = api.catalogo()                                           // primero esto…
    val detalles = juegos.map { juego -> async { api.detalle(juego.id) } } // …y luego todos a la vez
        .awaitAll()                                                       // espera a todos
    VitrinaDeJuegos(juegos, detalles)
}
