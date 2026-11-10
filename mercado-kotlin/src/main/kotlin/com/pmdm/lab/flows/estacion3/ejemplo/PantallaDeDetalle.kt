package com.pmdm.lab.flows.estacion3.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Detalle
import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Los estados posibles de la pantalla. Un `sealed interface`: la pantalla está en uno y solo uno. */
sealed interface EstadoDetalle {
    data object SinDatos : EstadoDetalle
    data object Cargando : EstadoDetalle
    data class Listo(val detalle: Detalle) : EstadoDetalle
    data class Error(val mensaje: String) : EstadoDetalle
}

/**
 * Una pantalla que carga el detalle de un juego.
 *
 * Dos reglas de cancelación que casi toda pantalla necesita:
 *
 * - **Al abrir otro juego**, se cancela la carga anterior: si no, una respuesta lenta del juego
 *   antiguo podría llegar después y pisar el estado del nuevo.
 * - **Al cerrar la pantalla** (`cerrar()`), se cancela la carga en curso: no tiene sentido seguir
 *   descargando datos que nadie va a ver.
 *
 * Es lo que hace `viewModelScope` por ti cuando se destruye el ViewModel.
 */
class PantallaDeDetalle(private val api: MercadoApi, private val scope: CoroutineScope) {

    @Volatile
    var estado: EstadoDetalle = EstadoDetalle.SinDatos
        private set

    private var carga: Job? = null

    fun abrir(juegoId: Int) {
        carga?.cancel()                       // la carga anterior ya no interesa
        carga = scope.launch {
            estado = EstadoDetalle.Cargando
            estado = try {
                EstadoDetalle.Listo(api.detalle(juegoId))
            } catch (e: ErrorMercado) {
                EstadoDetalle.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cerrar() {
        carga?.cancel()
    }
}
