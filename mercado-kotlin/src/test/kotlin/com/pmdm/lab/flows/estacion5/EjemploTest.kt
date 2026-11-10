package com.pmdm.lab.flows.estacion5

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion5.ejemplo.CotizadorDeJuego
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Comprueba que lo que cuenta la guía sobre el ejemplo resuelto es cierto. */
class EjemploTest {

    private fun TestScope.avanza(ms: Long) {
        advanceTimeBy(ms); runCurrent()
    }

    private fun TestScope.scopePropio() =
        CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]))

    @Test
    fun noSeConectaHastaQueHayaUnColector() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        CotizadorDeJuego(api, 1, scopePropio())
        avanza(2_000)
        assertEquals(0, api.estadisticas().conexionesPrecios)
    }

    @Test
    fun tresColectores_unaSolaConexion() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val cotizador = CotizadorDeJuego(api, 1, scopePropio())
        repeat(3) { backgroundScope.launch { cotizador.precioEnVivo.collect { } } }
        avanza(1_200)
        assertEquals(1, api.estadisticas().conexionesPrecios)
        assertNotNull(cotizador.precioEnVivo.value)
    }

    @Test
    fun whileSubscribed_sinReconectarSiVuelvenAntesDeLaEspera_yReconectandoSiTardan() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val cotizador = CotizadorDeJuego(api, 1, scopePropio())

        val a = backgroundScope.launch { cotizador.precioEnVivo.collect { } }
        avanza(2_000); a.cancel()
        avanza(2_000)                                      // menos de 5 s sin nadie
        val b = backgroundScope.launch { cotizador.precioEnVivo.collect { } }
        avanza(700); b.cancel()
        assertEquals(1, api.estadisticas().conexionesPrecios, "No debe reconectar")

        avanza(6_000)                                      // más de 5 s sin nadie
        backgroundScope.launch { cotizador.precioEnVivo.collect { } }
        avanza(700)
        assertEquals(2, api.estadisticas().conexionesPrecios, "Ahora sí reconecta")
    }
}
