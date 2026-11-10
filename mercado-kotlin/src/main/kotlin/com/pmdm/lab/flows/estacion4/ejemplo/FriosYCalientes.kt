package com.pmdm.lab.flows.estacion4.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Oferta
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * FLUJO FRÍO · `precios(juegoId)`.
 *
 * Un flujo frío **no hace nada hasta que alguien lo colecta**, y cada `collect` lo ejecuta DESDE EL PRINCIPIO,
 * con su propia conexión. Aquí lanzamos `colectores` colectores sobre el mismo flujo y le preguntamos al
 * servidor cuántas conexiones ha visto abrir.
 *
 * Resultado: tantas conexiones como colectores. Cada uno recibe su propia secuencia de precios completa.
 */
suspend fun contarConexionesPrecios(api: MercadoApi, juegoId: Int, colectores: Int): Int = coroutineScope {
    List(colectores) {
        launch { api.precios(juegoId).take(3).collect { } }     // cada colector pide 3 precios y se va
    }.joinAll()
    api.estadisticas().conexionesPrecios
}

/**
 * FLUJO CALIENTE · `ofertas`.
 *
 * Un flujo caliente **ya está emitiendo** aunque nadie escuche: quien llega tarde solo ve lo que ocurre
 * a partir de que llega. Aquí el colector espera `esperaMs`, se engancha y escucha durante `duracionMs`.
 *
 * Resultado: solo las ofertas emitidas DESPUÉS de engancharse. Las anteriores se han perdido
 * (`SharedFlow` sin `replay` no recuerda nada).
 */
suspend fun ofertasParaColectorTardio(api: MercadoApi, esperaMs: Long, duracionMs: Long): List<Oferta> {
    delay(esperaMs)
    val vistas = mutableListOf<Oferta>()
    withTimeoutOrNull(duracionMs) {          // un SharedFlow nunca termina: cortamos nosotros
        api.ofertas.collect { vistas += it }
    }
    return vistas
}
