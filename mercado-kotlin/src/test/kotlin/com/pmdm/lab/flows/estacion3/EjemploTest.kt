package com.pmdm.lab.flows.estacion3

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion3.ejemplo.EstadoDetalle
import com.pmdm.lab.flows.estacion3.ejemplo.PantallaDeDetalle
import com.pmdm.lab.flows.estacion3.ejemplo.cargarDetalleResiliente
import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/** Comprueba que lo que cuenta la guía sobre el ejemplo resuelto es cierto. */
class EjemploTest {

    /** Avanza el reloj virtual y ejecuta lo pendiente (también lo de los scopes colgados de backgroundScope). */
    private fun TestScope.avanza(ms: Long) {
        advanceTimeBy(ms); runCurrent()
    }

    private fun TestScope.scopePropio() =
        CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]))

    @Test
    fun reintenta_hastaAcertar() = runTest {
        val api = MercadoInestable(MercadoSimulado(backgroundScope, Caos(latenciaMs = 100)), fallosAntesDeAcertar = 2)
        val detalle = cargarDetalleResiliente(api, 1)
        assertEquals(1, detalle.juegoId)
        assertEquals(100L * 3 + 200L * 2, currentTime)
    }

    @Test
    fun siTardaDemasiado_seRindeTrasLosIntentos() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 5_000))
        assertFailsWith<ErrorMercado> { cargarDetalleResiliente(api, 1, intentos = 2, limiteMs = 500) }
        assertEquals(500L + 200L + 500L, currentTime)
    }

    @Test
    fun cerrarCancelaLaCarga() = runTest {
        val pantalla = PantallaDeDetalle(MercadoSimulado(backgroundScope, Caos(latenciaMs = 300)), scopePropio())
        pantalla.abrir(1)
        advanceTimeBy(100); runCurrent()
        assertIs<EstadoDetalle.Cargando>(pantalla.estado)
        pantalla.cerrar()
        avanza(2_000)
        assertIs<EstadoDetalle.Cargando>(pantalla.estado)
    }
}
