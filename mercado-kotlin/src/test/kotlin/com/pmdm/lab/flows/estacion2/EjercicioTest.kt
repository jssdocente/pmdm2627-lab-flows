package com.pmdm.lab.flows.estacion2

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion2.ejercicio.VigilanteDeJugador
import com.pmdm.lab.flows.estacion2.ejercicio.cargarPanelDeJugador
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
import kotlin.test.assertTrue

class EjercicioTest {

    private fun TestScope.mercado(caos: Caos) = MercadoSimulado(backgroundScope, caos)

    /**
     * Un scope propio con `SupervisorJob`, colgado de `backgroundScope`: así runTest lo cancela al terminar
     * y los bucles infinitos del vigilante no dejan el test esperando para siempre.
     */
    private fun TestScope.scopePropio() =
        CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]))

    // ---------- Parte 1: el panel ----------

    @Test
    fun panel_todoFunciona_devuelveLasTresSeccionesEnParalelo() = runTest {
        val panel = cargarPanelDeJugador(mercado(Caos(latenciaMs = 300)), 1)
        assertEquals(1, panel.perfil.id)
        assertTrue(panel.logros.isSuccess)
        assertTrue(panel.amigos.isSuccess)
        assertEquals(300L, currentTime, "Las tres peticiones deben ir a la vez")
    }

    @Test
    fun panel_siFallanLosLogros_elPanelSeDevuelveIgualConLosAmigos() = runTest {
        val panel = cargarPanelDeJugador(mercado(Caos(300, siempreFallan = setOf("logros"))), 1)
        assertTrue(panel.logros.isFailure, "Los logros fallan: su Result debe ser un fallo")
        assertTrue(panel.amigos.isSuccess, "Los amigos NO deben cancelarse porque fallen los logros")
        assertEquals(1, panel.perfil.id)
    }

    @Test
    fun panel_siFallaElPerfil_fallaTodoElPanel() = runTest {
        val api = mercado(Caos(300, siempreFallan = setOf("perfil")))
        assertFailsWith<ErrorMercado> { cargarPanelDeJugador(api, 1) }
    }

    // ---------- Parte 2: el vigilante ----------

    @Test
    fun vigilante_refrescaLogrosYAmigosCadaSegundo() = runTest {
        val vigilante = VigilanteDeJugador(mercado(Caos(latenciaMs = 100)), 1, scopePropio())
        vigilante.iniciar()
        advanceTimeBy(3_500); runCurrent()
        assertTrue(vigilante.refrescosDeLogros >= 3, "logros: ${vigilante.refrescosDeLogros}")
        assertTrue(vigilante.refrescosDeAmigos >= 3, "amigos: ${vigilante.refrescosDeAmigos}")
        assertEquals(0, vigilante.fallos)
    }

    @Test
    fun vigilante_unFalloEnLosLogrosNoParaElRefrescoDeAmigos() = runTest {
        val api = mercado(Caos(latenciaMs = 100, siempreFallan = setOf("logros")))
        val vigilante = VigilanteDeJugador(api, 1, scopePropio())
        vigilante.iniciar()
        advanceTimeBy(3_500); runCurrent()
        assertEquals(0, vigilante.refrescosDeLogros)
        assertTrue(vigilante.fallos >= 3, "Los fallos deben contarse y el bucle seguir: ${vigilante.fallos}")
        assertTrue(vigilante.refrescosDeAmigos >= 3, "Los amigos deben seguir refrescándose")
    }

    @Test
    fun vigilante_detenerCancelaLasDosCorrutinas() = runTest {
        val vigilante = VigilanteDeJugador(mercado(Caos(latenciaMs = 100)), 1, scopePropio())
        vigilante.iniciar()
        advanceTimeBy(3_500); runCurrent()
        vigilante.detener()
        val logros = vigilante.refrescosDeLogros
        val amigos = vigilante.refrescosDeAmigos
        advanceTimeBy(5_000); runCurrent()
        assertEquals(logros, vigilante.refrescosDeLogros)
        assertEquals(amigos, vigilante.refrescosDeAmigos)
    }
}
