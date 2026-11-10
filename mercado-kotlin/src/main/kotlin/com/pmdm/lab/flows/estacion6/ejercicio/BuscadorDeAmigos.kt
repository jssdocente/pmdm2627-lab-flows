@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.pmdm.lab.flows.estacion6.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Amigo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Un amigo de los resultados, con una marca de si es favorito. */
data class AmigoMarcado(val amigo: Amigo, val favorito: Boolean)

/**
 * TU TURNO.
 *
 * Un buscador de amigos de un jugador. Las entradas ya están hechas (`consulta` y `favoritos`, con `escribir` y
 * `alternarFavorito`). Tú escribes `resultados`: una cadena de operadores que:
 *
 * 1. espera `esperaMs` a que el usuario deje de teclear (`debounce`) y no repite si el texto no cambia
 *    (`distinctUntilChanged`),
 * 2. por cada consulta pide `api.amigos(jugadorId)` y se queda solo con los amigos cuyo nombre contiene el texto,
 *    sin distinguir mayúsculas (`flatMapLatest` con un `flow { emit(...) }` dentro; así se cancela la búsqueda anterior),
 * 3. lo junta con los favoritos (`combine`) para devolver una lista de `AmigoMarcado`,
 * 4. y lo expone como `StateFlow` con `emptyList()` como valor inicial y `SharingStarted.WhileSubscribed(5_000)` (`stateIn`).
 *
 * Marcar un favorito **no** debe repetir la petición al servidor.
 *
 * Pista: es `BuscadorDeJuegos`, con las peticiones de amigos en lugar de las del catálogo.
 */
class BuscadorDeAmigos(
    private val api: MercadoApi,
    private val jugadorId: Int,
    private val scope: CoroutineScope,
    private val esperaMs: Long = 300,
) {
    private val consulta = MutableStateFlow("")
    private val favoritos = MutableStateFlow<Set<Int>>(emptySet())

    fun escribir(texto: String) {
        consulta.value = texto
    }

    fun alternarFavorito(amigoId: Int) {
        favoritos.update { if (amigoId in it) it - amigoId else it + amigoId }
    }

    val resultados: StateFlow<List<AmigoMarcado>> = TODO("Pendiente de implementar")
}
