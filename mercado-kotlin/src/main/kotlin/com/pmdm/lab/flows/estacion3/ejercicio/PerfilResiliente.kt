package com.pmdm.lab.flows.estacion3.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.ErrorMercado
import com.pmdm.lab.flows.modelo.Perfil
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * TU TURNO · parte 1.
 *
 * Carga el perfil de un jugador sin rendirte a la primera, igual que `cargarDetalleResiliente`:
 *
 * - Cada intento se hace con un límite de `limiteMs` (si lo supera, cuenta como intento fallido).
 * - Si el mercado falla (`ErrorMercado`), también cuenta como intento fallido.
 * - Entre un intento y el siguiente espera `pausaMs`; **después del último intento no se espera**.
 * - Si se agotan los `intentos`, lanza `ErrorMercado`.
 *
 * Con 100 ms por petición y dos fallos antes de acertar, debe tardar 700 ms
 * (3 intentos de 100 ms + 2 pausas de 200 ms).
 *
 * Pista: `withTimeoutOrNull` devuelve `null` cuando se pasa el tiempo.
 */
suspend fun cargarPerfilResiliente(
    api: MercadoApi,
    jugadorId: Int,
    intentos: Int = 3,
    limiteMs: Long = 1_000,
    pausaMs: Long = 200,
): Perfil {
    repeat(intentos) { intento ->
        val perfil = try {
            withTimeoutOrNull(limiteMs) { api.perfil(jugadorId) }
        } catch (e: ErrorMercado) {
            null
        }
        if (perfil != null) return perfil
        if (intento < intentos - 1) delay(pausaMs)
    }
    throw ErrorMercado("No se pudo cargar el perfil del jugador $jugadorId tras $intentos intentos")
}
