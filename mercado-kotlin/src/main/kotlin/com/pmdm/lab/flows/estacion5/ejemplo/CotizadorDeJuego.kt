package com.pmdm.lab.flows.estacion5.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Precio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

/**
 * De FRÍO a CALIENTE.
 *
 * `api.precios(id)` es un flujo frío: cada colector abre su propia conexión. Si tres pantallas lo colectan,
 * son tres conexiones. Dos operadores lo convierten en caliente, **con una sola conexión para todos**:
 *
 * - `stateIn`: un `StateFlow`, que siempre tiene un valor (el último, o el inicial si aún no ha llegado nada).
 * - `shareIn`: un `SharedFlow`, que reparte lo que llega; con `replay = 1` recuerda el último valor.
 *
 * `SharingStarted.WhileSubscribed(PARADA_TRAS_MS)` decide **cuándo** se conecta: con el primer colector,
 * y se desconecta cuando el último se va y pasan `PARADA_TRAS_MS` sin nadie escuchando. Esa espera evita
 * reconectar si alguien se va un momento (por ejemplo, al girar la pantalla).
 *
 * Ojo: son dos conexiones distintas porque cada operador parte de su propia llamada a `precios()`.
 * Colectar solo uno de los dos abre solo una conexión.
 */
class CotizadorDeJuego(api: MercadoApi, juegoId: Int, scope: CoroutineScope) {

    val precioEnVivo: StateFlow<Precio?> =
        api.precios(juegoId).stateIn(scope, SharingStarted.WhileSubscribed(PARADA_TRAS_MS), initialValue = null)

    val ultimosPrecios: SharedFlow<Precio> =
        api.precios(juegoId).shareIn(scope, SharingStarted.WhileSubscribed(PARADA_TRAS_MS), replay = 1)

    companion object {
        const val PARADA_TRAS_MS = 5_000L
    }
}
