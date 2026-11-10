package com.pmdm.lab.flows.estacion3

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion3.ejercicio.EstadoJugador
import com.pmdm.lab.flows.estacion3.ejercicio.PantallaDeJugador
import com.pmdm.lab.flows.estacion3.ejercicio.cargarPerfilResiliente
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

class EjercicioTest {

    /** Avanza el reloj virtual y ejecuta lo pendiente (también lo de los scopes colgados de backgroundScope). */
    private fun TestScope.avanza(ms: Long) {
        advanceTimeBy(ms); runCurrent()
    }

    private fun TestScope.scopePropio() =
        CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]))

    private fun TestScope.pantalla(caos: Caos) = PantallaDeJugador(MercadoSimulado(backgroundScope, caos), scopePropio())

    // ---------- Parte 1: reintentos y límite de tiempo ----------

    @Test
    fun perfil_reintentaHastaAcertar() = runTest {
        val api = MercadoInestable(MercadoSimulado(backgroundScope, Caos(latenciaMs = 100)), fallosAntesDeAcertar = 2)
        val perfil = cargarPerfilResiliente(api, 1)
        assertEquals(1, perfil.id)
        assertEquals(700L, currentTime, "3 intentos de 100 ms + 2 pausas de 200 ms; sin pausa tras el último")
    }

    @Test
    fun perfil_siSeAgotanLosIntentos_lanzaErrorMercado() = runTest {
        val api = MercadoInestable(MercadoSimulado(backgroundScope, Caos(latenciaMs = 100)), fallosAntesDeAcertar = 5)
        assertFailsWith<ErrorMercado> { cargarPerfilResiliente(api, 1, intentos = 3) }
        assertEquals(700L, currentTime)
    }

    @Test
    fun perfil_siTardaMasDelLimite_cuentaComoIntentoFallido() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 5_000))
        assertFailsWith<ErrorMercado> { cargarPerfilResiliente(api, 1, intentos = 2, limiteMs = 1_000) }
        assertEquals(1_000L + 200L + 1_000L, currentTime, "Cada intento se corta a 1000 ms")
    }

    // ---------- Parte 2: la pantalla ----------

    @Test
    fun pantalla_pasaPorCargandoYListo() = runTest {
        val pantalla = pantalla(Caos(latenciaMs = 100))
        assertIs<EstadoJugador.SinDatos>(pantalla.estado)
        pantalla.abrir(1)
        runCurrent()
        assertIs<EstadoJugador.Cargando>(pantalla.estado)
        advanceTimeBy(101); runCurrent()
        val listo = assertIs<EstadoJugador.Listo>(pantalla.estado)
        assertEquals(1, listo.perfil.id)
    }

    @Test
    fun pantalla_siFallaElMercado_pasaAError() = runTest {
        val pantalla = pantalla(Caos(latenciaMs = 100, siempreFallan = setOf("perfil")))
        pantalla.abrir(1)
        avanza(2_000)
        assertIs<EstadoJugador.Error>(pantalla.estado)
    }

    @Test
    fun pantalla_cerrarCancelaLaCarga_laRespuestaNoSeAplica() = runTest {
        val pantalla = pantalla(Caos(latenciaMs = 100))
        pantalla.abrir(1)
        advanceTimeBy(50); runCurrent()
        pantalla.cerrar()
        avanza(2_000)
        assertIs<EstadoJugador.Cargando>(pantalla.estado, "Tras cerrar, la respuesta no debe llegar a aplicarse")
    }

    @Test
    fun pantalla_alAbrirOtroJugador_laCargaAnteriorSeCancela() = runTest {
        val pantalla = pantalla(Caos(latenciaMs = 100))
        pantalla.abrir(1)
        advanceTimeBy(50); runCurrent()
        pantalla.abrir(2)                       // el jugador 1 habría terminado en t = 100
        advanceTimeBy(70); runCurrent()         // t = 120
        assertIs<EstadoJugador.Cargando>(pantalla.estado, "La respuesta del jugador 1 no debe pisar al 2")
        advanceTimeBy(40); runCurrent()         // t = 160: termina el jugador 2
        val listo = assertIs<EstadoJugador.Listo>(pantalla.estado)
        assertEquals(2, listo.perfil.id)
    }
}
