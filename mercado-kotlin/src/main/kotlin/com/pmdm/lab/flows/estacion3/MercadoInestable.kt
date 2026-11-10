package com.pmdm.lab.flows.estacion3

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Detalle
import com.pmdm.lab.flows.modelo.ErrorMercado
import com.pmdm.lab.flows.modelo.Perfil
import java.util.concurrent.atomic.AtomicInteger

/**
 * Un mercado que **falla las primeras veces y luego acierta**, para probar los reintentos.
 *
 * Es un *decorador*: envuelve a otro mercado (`destino`) y delega en él todo lo que no cambia
 * (`MercadoApi by destino`). Solo `detalle` y `perfil` se comportan distinto:
 * hacen la petición completa (tardan lo mismo) y, mientras queden fallos pendientes, lanzan un 503.
 */
class MercadoInestable(
    private val destino: MercadoApi,
    fallosAntesDeAcertar: Int,
) : MercadoApi by destino {

    private val fallosDetalle = AtomicInteger(fallosAntesDeAcertar)
    private val fallosPerfil = AtomicInteger(fallosAntesDeAcertar)

    override suspend fun detalle(juegoId: Int): Detalle {
        val detalle = destino.detalle(juegoId)
        if (fallosDetalle.getAndDecrement() > 0) throw ErrorMercado("503: «detalle» inestable")
        return detalle
    }

    override suspend fun perfil(jugadorId: Int): Perfil {
        val perfil = destino.perfil(jugadorId)
        if (fallosPerfil.getAndDecrement() > 0) throw ErrorMercado("503: «perfil» inestable")
        return perfil
    }
}
