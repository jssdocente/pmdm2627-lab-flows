@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.pmdm.lab.flows.estacion6.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Juego
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Un juego de los resultados, con una marca de si el usuario lo ha puesto como favorito. */
data class JuegoMarcado(val juego: Juego, val favorito: Boolean)

/**
 * Un buscador de juegos: la pantalla escribe una consulta, marca favoritos y observa `resultados`.
 *
 * Todo el trabajo es una **cadena de operadores** sobre dos flujos de entrada (lo que se escribe y los favoritos):
 *
 * 1. `debounce(esperaMs)`: espera a que el usuario deje de teclear. Sin esto, cada tecla («p», «pi», «pix»)
 *    sería una petición al servidor.
 *
 * 2. `distinctUntilChanged()`: no repite la búsqueda si el texto final es igual que el anterior.
 *
 * 3. `flatMapLatest { … }`: por cada consulta lanza una búsqueda, y **cancela la anterior si aún no ha terminado**.
 *    Así una respuesta lenta de «pix» nunca pisa la de «tur».
 *
 * 4. `combine(buscar, favoritos)`: junta los dos flujos. Cuando cambia **cualquiera** de los dos, recalcula la lista,
 *    **sin repetir la petición** si solo cambian los favoritos.
 *
 * 5. `stateIn`: el resultado se expone como `StateFlow`, que es lo que consume una pantalla Compose.
 */
class BuscadorDeJuegos(private val api: MercadoApi, scope: CoroutineScope, esperaMs: Long = 300) {

    private val consulta = MutableStateFlow("")
    private val favoritos = MutableStateFlow<Set<Int>>(emptySet())

    fun escribir(texto: String) {
        consulta.value = texto
    }

    fun alternarFavorito(juegoId: Int) {
        favoritos.update { if (juegoId in it) it - juegoId else it + juegoId }
    }

    val resultados: StateFlow<List<JuegoMarcado>> = combine(
        consulta
            .debounce(esperaMs)
            .distinctUntilChanged()
            .flatMapLatest { texto ->
                flow { emit(api.catalogo().filter { it.titulo.contains(texto, ignoreCase = true) }) }
            },
        favoritos,
    ) { juegos, marcados ->
        juegos.map { JuegoMarcado(it, it.id in marcados) }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
