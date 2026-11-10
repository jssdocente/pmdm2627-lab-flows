package com.pmdm.lab.flows.estacion2.ejemplo

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.estacion2.intentar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Un SCOPE PROPIO: refresca el catálogo y la ficha destacada cada segundo, cada uno en su corrutina.
 *
 * Se le pasa el `scope` desde fuera (como hace `viewModelScope` con un ViewModel). Dos ideas:
 *
 * 1. **Aislamiento**: el scope se crea con `SupervisorJob`, y cada `launch` captura sus errores DENTRO
 *    (con `intentar`). Si el catálogo falla, el refresco de la ficha sigue.
 * 2. **Cancelación**: `detener()` cancela el scope y, con él, a todos sus hijos a la vez.
 */
class VigilanteDeCatalogo(private val api: MercadoApi, private val scope: CoroutineScope) {

    private val catalogos = AtomicInteger()
    private val destacados = AtomicInteger()
    private val errores = AtomicInteger()

    val refrescosDeCatalogo get() = catalogos.get()
    val refrescosDeDestacado get() = destacados.get()
    val fallos get() = errores.get()

    fun iniciar() {
        scope.launch {
            while (true) {
                if (intentar { api.catalogo() }.isSuccess) catalogos.incrementAndGet() else errores.incrementAndGet()
                delay(PERIODO_MS)
            }
        }
        scope.launch {
            while (true) {
                if (intentar { api.detalle(1) }.isSuccess) destacados.incrementAndGet() else errores.incrementAndGet()
                delay(PERIODO_MS)
            }
        }
    }

    fun detener() {
        scope.cancel()
    }

    companion object {
        const val PERIODO_MS = 1_000L
    }
}
