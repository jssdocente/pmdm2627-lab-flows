package com.pmdm.lab.flows.estacion6

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion6.ejemplo.BuscadorDeJuegos
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

/** Comprueba que lo que cuenta la guía sobre el ejemplo resuelto es cierto. */
class EjemploTest {

    private fun TestScope.avanza(ms: Long) {
        advanceTimeBy(ms); runCurrent()
    }

    private fun TestScope.scopePropio() =
        CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]))

    private fun TestScope.buscador(latenciaMs: Long, esperaMs: Long): Pair<MercadoSimulado, BuscadorDeJuegos> {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = latenciaMs))
        val buscador = BuscadorDeJuegos(api, scopePropio(), esperaMs)
        backgroundScope.launch { buscador.resultados.collect { } }
        return api to buscador
    }

    @Test
    fun debounce_variasTeclasHacenUnaSolaPeticion() = runTest {
        val (api, buscador) = buscador(latenciaMs = 100, esperaMs = 300)
        avanza(1_000)
        val antes = api.estadisticas().peticiones
        buscador.escribir("p"); avanza(100)
        buscador.escribir("pi"); avanza(100)
        buscador.escribir("pix"); avanza(1_000)
        assertEquals(1, api.estadisticas().peticiones - antes)
        assertEquals(listOf("Pixel Quest"), buscador.resultados.value.map { it.juego.titulo })
    }

    @Test
    fun flatMapLatest_laRespuestaAntiguaNoPisaALaNueva() = runTest {
        val (_, buscador) = buscador(latenciaMs = 500, esperaMs = 50)
        avanza(2_000)
        assertEquals(6, buscador.resultados.value.size)
        buscador.escribir("pix"); avanza(100)       // la petición de «pix» terminaría en t = 2550
        buscador.escribir("tur"); avanza(500)       // t = 2600: «pix» habría terminado, pero se canceló
        assertEquals(6, buscador.resultados.value.size, "La respuesta de «pix» no debe aplicarse")
        avanza(100)                                 // t = 2700: termina «tur»
        assertEquals(listOf("Turbo Kart"), buscador.resultados.value.map { it.juego.titulo })
    }

    @Test
    fun combine_marcarUnFavoritoNoRepiteLaPeticion() = runTest {
        val (api, buscador) = buscador(latenciaMs = 100, esperaMs = 300)
        avanza(1_000)
        val antes = api.estadisticas().peticiones
        buscador.alternarFavorito(1); avanza(50)
        assertEquals(antes, api.estadisticas().peticiones)
        assertTrue(buscador.resultados.value.first { it.juego.id == 1 }.favorito)
    }
}
