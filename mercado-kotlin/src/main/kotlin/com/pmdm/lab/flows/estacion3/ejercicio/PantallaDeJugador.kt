package com.pmdm.lab.flows.estacion3.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.ErrorMercado
import com.pmdm.lab.flows.modelo.Perfil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Los estados posibles de la pantalla del jugador. */
sealed interface EstadoJugador {
    data object SinDatos : EstadoJugador
    data object Cargando : EstadoJugador
    data class Listo(val perfil: Perfil) : EstadoJugador
    data class Error(val mensaje: String) : EstadoJugador
}

/**
 * TU TURNO · parte 2.
 *
 * Una pantalla que carga el perfil de un jugador. Escribe `abrir` y `cerrar`:
 *
 * - `abrir(jugadorId)`: cancela la carga anterior (si la hay) y lanza una nueva con `scope.launch`.
 *   Pasa a `Cargando`, y cuando llega el perfil, a `Listo`. Si el mercado falla, a `Error`.
 * - `cerrar()`: cancela la carga en curso. **No cambia el estado**: la respuesta simplemente no se aplica.
 *
 * Pista: es `PantallaDeDetalle`, con otro dato.
 */
class PantallaDeJugador(private val api: MercadoApi, private val scope: CoroutineScope) {

    @Volatile
    var estado: EstadoJugador = EstadoJugador.SinDatos
        private set

    private var carga: Job? = null

    fun abrir(jugadorId: Int) {
        carga?.cancel()
        carga = scope.launch {
            estado = EstadoJugador.Cargando
            estado = try {
                EstadoJugador.Listo(api.perfil(jugadorId))
            } catch (e: ErrorMercado) {
                EstadoJugador.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cerrar() {
        carga?.cancel()
    }
}
