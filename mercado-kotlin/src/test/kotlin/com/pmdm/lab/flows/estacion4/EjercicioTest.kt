package com.pmdm.lab.flows.estacion4

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion4.ejercicio.contarConexionesRanking
import com.pmdm.lab.flows.estacion4.ejercicio.resumenParaColectorTardio
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EjercicioTest {

    @Test
    fun ranking_conDosColectores_elServidorVeDosConexiones() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        assertEquals(2, contarConexionesRanking(api, colectores = 2),
            "Un flujo frío abre una conexión por cada colector")
    }

    @Test
    fun ranking_conTresColectores_elServidorVeTresConexiones() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        assertEquals(3, contarConexionesRanking(api, colectores = 3))
    }

    @Test
    fun resumen_elColectorTardioRecibeYaElUltimoValor() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val resumen = resumenParaColectorTardio(api, esperaMs = 5_000)
        assertEquals(2, resumen.ofertasEmitidas, "A los 5 s ya han salido las ofertas de los 2 s y los 4 s")
        assertNotNull(resumen.ultimaOferta)
        assertEquals(5_000L, currentTime, "Un StateFlow entrega su valor al instante: no hay que esperar a la siguiente")
    }
}
