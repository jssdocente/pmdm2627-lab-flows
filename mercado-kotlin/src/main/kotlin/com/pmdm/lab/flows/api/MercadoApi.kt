package com.pmdm.lab.flows.api

import com.pmdm.lab.flows.modelo.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Lo único que necesitas saber del mercado. Da igual que los datos vengan del servidor de tu pareja
 * o de una simulación dentro de tu programa: tu código solo usa esta interfaz.
 *
 * Fíjate en los tipos, que son la clave del laboratorio:
 *
 * - `suspend fun` → una petición que tarda y puede fallar (una sola respuesta).
 * - `Flow` → flujo **frío**: cada `collect` abre su propia conexión.
 * - `SharedFlow` y `StateFlow` → flujos **calientes**: ya están emitiendo, tú solo te enganchas.
 */
interface MercadoApi {

    // --- Peticiones únicas (suspend) ---
    suspend fun catalogo(): List<Juego>
    suspend fun detalle(juegoId: Int): Detalle
    suspend fun perfil(jugadorId: Int): Perfil
    suspend fun logros(jugadorId: Int): List<Logro>
    suspend fun amigos(jugadorId: Int): List<Amigo>

    // --- Flujo frío: una conexión nueva por cada collect ---
    fun precios(juegoId: Int): Flow<Precio>
    fun ranking(): Flow<List<Puesto>>

    // --- Flujos calientes: uno solo para todos ---
    val ofertas: SharedFlow<Oferta>
    val resumen: StateFlow<ResumenMercado>

    // --- Para comprobar cuántas conexiones has abierto ---
    suspend fun estadisticas(): Estadisticas
}
