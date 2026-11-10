package com.pmdm.lab.flows.estacion6

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion6.ejercicio.BuscadorDeAmigos
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
import kotlin.test.assertTrue

class EjercicioTest {

    private fun TestScope.avanza(ms: Long) {
        advanceTimeBy(ms); runCurrent()
    }

    private fun TestScope.scopePropio() =
        CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]))

    /** Los amigos del jugador 1 son Carla (2), Dani (3) y Eli (4). */
    private fun TestScope.buscador(latenciaMs: Long, esperaMs: Long): Pair<MercadoSimulado, BuscadorDeAmigos> {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = latenciaMs))
        val buscador = BuscadorDeAmigos(api, jugadorId = 1, scope = scopePropio(), esperaMs = esperaMs)
        backgroundScope.launch { buscador.resultados.collect { } }
        return api to buscador
    }

    @Test
    fun alEmpezar_muestraTodosLosAmigos() = runTest {
        val (_, buscador) = buscador(latenciaMs = 100, esperaMs = 300)
        assertTrue(buscador.resultados.value.isEmpty(), "El valor inicial debe ser una lista vacía")
        avanza(1_000)
        assertEquals(listOf("Carla", "Dani", "Eli"), buscador.resultados.value.map { it.amigo.nombre })
    }

    @Test
    fun filtraPorNombre_sinDistinguirMayusculas() = runTest {
        val (_, buscador) = buscador(latenciaMs = 100, esperaMs = 300)
        avanza(1_000)
        buscador.escribir("DAN"); avanza(1_000)
        assertEquals(listOf("Dani"), buscador.resultados.value.map { it.amigo.nombre })
    }

    @Test
    fun debounce_variasTeclasHacenUnaSolaPeticion() = runTest {
        val (api, buscador) = buscador(latenciaMs = 100, esperaMs = 300)
        avanza(1_000)
        val antes = api.estadisticas().peticiones
        buscador.escribir("c"); avanza(100)
        buscador.escribir("ca"); avanza(100)
        buscador.escribir("car"); avanza(1_000)
        assertEquals(1, api.estadisticas().peticiones - antes, "Debes esperar a que termine de teclear (debounce)")
    }

    @Test
    fun flatMapLatest_laRespuestaAntiguaNoPisaALaNueva() = runTest {
        val (_, buscador) = buscador(latenciaMs = 500, esperaMs = 50)
        avanza(2_000)
        buscador.escribir("car"); avanza(100)        // su petición terminaría en t = 2550
        buscador.escribir("dan"); avanza(500)        // t = 2600
        assertEquals(3, buscador.resultados.value.size, "La respuesta de «car» no debe aplicarse")
        avanza(100)                                  // t = 2700
        assertEquals(listOf("Dani"), buscador.resultados.value.map { it.amigo.nombre })
    }

    @Test
    fun combine_marcarUnFavoritoNoRepiteLaPeticion() = runTest {
        val (api, buscador) = buscador(latenciaMs = 100, esperaMs = 300)
        avanza(1_000)
        val antes = api.estadisticas().peticiones
        buscador.alternarFavorito(3); avanza(50)
        assertEquals(antes, api.estadisticas().peticiones, "Un favorito no debe volver a pedir los amigos")
        assertEquals(listOf(false, true, false), buscador.resultados.value.map { it.favorito })
    }
}
