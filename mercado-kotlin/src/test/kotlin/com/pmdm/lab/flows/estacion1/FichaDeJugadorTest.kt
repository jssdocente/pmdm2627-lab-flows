package com.pmdm.lab.flows.estacion1

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion1.ejercicio.cargarFicha
import com.pmdm.lab.flows.estacion1.ejercicio.cargarFichasDeAmigos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FichaDeJugadorTest {

    private fun crearApi(scope: CoroutineScope) = MercadoSimulado(scope, Caos(latenciaMs = 300))

    @Test
    fun cargarFicha_devuelveLosTresDatosDelJugador() = runTest {
        val ficha = cargarFicha(crearApi(backgroundScope), jugadorId = 1)
        assertEquals(1, ficha.perfil.id)
        assertTrue(ficha.logros.isNotEmpty())
        assertEquals(3, ficha.amigos.size)
    }

    @Test
    fun cargarFicha_vaEnParalelo_tardaLoQueLaMasLenta() = runTest {
        cargarFicha(crearApi(backgroundScope), jugadorId = 1)
        assertEquals(300L, currentTime, "Si tarda 900 ms, estás esperando a cada llamada antes de lanzar la siguiente")
    }

    @Test
    fun cargarFichasDeAmigos_devuelveUnaFichaPorAmigo() = runTest {
        val fichas = cargarFichasDeAmigos(crearApi(backgroundScope), jugadorId = 1)
        assertEquals(listOf(2, 3, 4), fichas.map { it.perfil.id })
    }

    @Test
    fun cargarFichasDeAmigos_sePidenTodasAlaVez() = runTest {
        cargarFichasDeAmigos(crearApi(backgroundScope), jugadorId = 1)
        assertEquals(600L, currentTime, "Esperado: amigos (300) + todas las fichas a la vez (300)")
    }
}
