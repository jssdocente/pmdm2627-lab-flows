package com.pmdm.lab.flows.estacion1

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion1.ejemplo.cargarVitrinaParalela
import com.pmdm.lab.flows.estacion1.ejemplo.cargarVitrinaSecuencial
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Comprueba que lo que cuenta la guía sobre el ejemplo resuelto es cierto. */
class EjemploTest {

    @Test
    fun secuencialSumaTodasLasEsperas() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val vitrina = cargarVitrinaSecuencial(api)
        assertEquals(6, vitrina.detalles.size)
        assertEquals(300L + 6 * 300L, currentTime)   // catálogo + 6 detalles, uno tras otro
    }

    @Test
    fun paralelaSoloEsperaLoQueTardaLaMasLenta() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val vitrina = cargarVitrinaParalela(api)
        assertEquals(6, vitrina.detalles.size)
        assertEquals(300L + 300L, currentTime)       // catálogo + todos los detalles a la vez
    }
}
