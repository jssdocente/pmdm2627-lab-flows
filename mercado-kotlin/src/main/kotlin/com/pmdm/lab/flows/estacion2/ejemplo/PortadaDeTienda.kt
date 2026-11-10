package com.pmdm.lab.flows.estacion2.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.estacion2.intentar
import com.pmdm.lab.flows.modelo.Detalle
import com.pmdm.lab.flows.modelo.Juego
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope

/**
 * La portada de la tienda: la lista de juegos (imprescindible) y la ficha del juego destacado
 * (un extra: si falla, la portada se ve igualmente).
 */
class PortadaDeTienda(val juegos: List<Juego>, val destacado: Result<Detalle>)

/**
 * VERSIÓN 1 · TODO O NADA (`coroutineScope`).
 *
 * Si CUALQUIERA de las dos peticiones falla, el scope cancela a la otra y relanza el error:
 * la portada entera se pierde aunque el catálogo haya llegado bien.
 */
suspend fun cargarPortadaTodoONada(api: MercadoApi): PortadaDeTienda = coroutineScope {
    val juegos = async { api.catalogo() }
    val destacado = async { api.detalle(1) }
    PortadaDeTienda(juegos.await(), Result.success(destacado.await()))
}

/**
 * VERSIÓN 2 · CADA UNO POR SU LADO (`supervisorScope`).
 *
 * Un `async` que falla NO cancela a su hermano: el error queda guardado y salta en su `await()`.
 * Por eso decidimos DÓNDE lo recogemos:
 *
 * - el catálogo es imprescindible → `juegos.await()` sin protección: si falla, falla la portada.
 * - el destacado es opcional → `intentar { destacado.await() }`: si falla, es un `Result` fallido.
 */
suspend fun cargarPortadaResistente(api: MercadoApi): PortadaDeTienda = supervisorScope {
    val juegos = async { api.catalogo() }
    val destacado = async { api.detalle(1) }
    PortadaDeTienda(juegos.await(), intentar { destacado.await() })
}
