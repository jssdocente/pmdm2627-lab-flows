package com.pmdm.lab.flows.estacion2

import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.CancellationException

/**
 * Ejecuta `bloque` y devuelve su resultado dentro de un `Result`: éxito, o fallo si el mercado falla.
 *
 * Ojo: **no** usamos `runCatching`, porque también atrapa la `CancellationException` y una corrutina
 * cancelada seguiría funcionando (apartado 6.2 de los apuntes). Aquí solo se captura `ErrorMercado`.
 */
suspend fun <T> intentar(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ErrorMercado) {
        Result.failure(e)
    }
