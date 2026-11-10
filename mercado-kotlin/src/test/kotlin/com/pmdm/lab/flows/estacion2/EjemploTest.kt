package com.pmdm.lab.flows.estacion2

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion2.ejemplo.VigilanteDeCatalogo
import com.pmdm.lab.flows.estacion2.ejemplo.cargarPortadaResistente
import com.pmdm.lab.flows.estacion2.ejemplo.cargarPortadaTodoONada
import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Comprueba que lo que cuenta la guía sobre el ejemplo resuelto es cierto. */
class EjemploTest {

    private val detalleCaido = Caos(latenciaMs = 300, siempreFallan = setOf("detalle"))

    @Test
    fun todoONada_siFallaUnaSeccionFallaLaPortadaEntera() = runTest {
        val api = MercadoSimulado(backgroundScope, detalleCaido)
        assertFailsWith<ErrorMercado> { cargarPortadaTodoONada(api) }
    }

    @Test
    fun resistente_laPortadaSeVeAunqueFalleElDestacado() = runTest {
        val api = MercadoSimulado(backgroundScope, detalleCaido)
        val portada = cargarPortadaResistente(api)
        assertEquals(6, portada.juegos.size)
        assertTrue(portada.destacado.isFailure)
        assertEquals(300L, currentTime)
    }

    @Test
    fun vigilante_seDetieneAlCancelarElScope() = runTest {
        val scope = CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]))
        val vigilante = VigilanteDeCatalogo(MercadoSimulado(backgroundScope, Caos(latenciaMs = 100)), scope)
        vigilante.iniciar()
        advanceTimeBy(3_500); runCurrent()
        val antes = vigilante.refrescosDeCatalogo
        assertTrue(antes >= 3)
        vigilante.detener()
        advanceTimeBy(5_000); runCurrent()
        assertEquals(antes, vigilante.refrescosDeCatalogo)
    }
}
