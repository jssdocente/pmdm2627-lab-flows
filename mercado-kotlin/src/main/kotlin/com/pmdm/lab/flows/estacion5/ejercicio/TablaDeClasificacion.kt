package com.pmdm.lab.flows.estacion5.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Puesto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * TU TURNO.
 *
 * `api.ranking()` es un flujo frío: cada pantalla que lo colecte abriría una conexión. Conviértelo en
 * caliente de dos formas, siempre con `SharingStarted.WhileSubscribed(PARADA_TRAS_MS)`:
 *
 * - `clasificacion`: un `StateFlow` con `emptyList()` como valor inicial (`stateIn`).
 * - `ultimasClasificaciones`: un `SharedFlow` que recuerda el último valor para quien llega tarde (`shareIn`, `replay = 1`).
 *
 * Con tres pantallas escuchando `clasificacion`, el servidor debe ver **una sola** conexión.
 *
 * Pista: es `CotizadorDeJuego`, con otro flujo.
 */
class TablaDeClasificacion(api: MercadoApi, scope: CoroutineScope) {

    val clasificacion: StateFlow<List<Puesto>> = TODO("Pendiente de implementar")

    val ultimasClasificaciones: SharedFlow<List<Puesto>> = TODO("Pendiente de implementar")

    companion object {
        const val PARADA_TRAS_MS = 5_000L
    }
}
