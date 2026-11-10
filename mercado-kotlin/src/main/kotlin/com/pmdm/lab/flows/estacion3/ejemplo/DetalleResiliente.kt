package com.pmdm.lab.flows.estacion3.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Detalle
import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Carga el detalle de un juego sin rendirse a la primera.
 *
 * Cada intento tiene dos formas de salir mal, y las dos se tratan igual (se reintenta):
 *
 * - **Falla** el mercado → `ErrorMercado`, que capturamos con `try-catch`.
 * - **Tarda demasiado** → `withTimeoutOrNull(limiteMs)` cancela la petición y devuelve `null`.
 *
 * Entre un intento y el siguiente espera `pausaMs`. Tras el último intento no hace falta esperar:
 * se lanza el error.
 *
 * Ojo: solo se captura `ErrorMercado`. Si la corrutina que llama a esta función se cancela,
 * la `CancellationException` debe seguir su camino (apartado 6.2 de los apuntes).
 */
suspend fun cargarDetalleResiliente(
    api: MercadoApi,
    juegoId: Int,
    intentos: Int = 3,
    limiteMs: Long = 1_000,
    pausaMs: Long = 200,
): Detalle {
    repeat(intentos) { intento ->
        val detalle = try {
            withTimeoutOrNull(limiteMs) { api.detalle(juegoId) }
        } catch (e: ErrorMercado) {
            null
        }
        if (detalle != null) return detalle
        if (intento < intentos - 1) delay(pausaMs)
    }
    throw ErrorMercado("No se pudo cargar el detalle del juego $juegoId tras $intentos intentos")
}
