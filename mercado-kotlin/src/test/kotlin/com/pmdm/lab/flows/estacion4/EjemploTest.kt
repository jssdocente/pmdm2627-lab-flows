package com.pmdm.lab.flows.estacion4

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion4.ejemplo.contarConexionesPrecios
import com.pmdm.lab.flows.estacion4.ejemplo.ofertasParaColectorTardio
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Comprueba que lo que cuenta la guía sobre el ejemplo resuelto es cierto. */
class EjemploTest {

    @Test
    fun frio_cadaColectorAbreSuPropiaConexion() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        assertEquals(3, contarConexionesPrecios(api, juegoId = 1, colectores = 3))
    }

    @Test
    fun caliente_elColectorTardioSoloVeLoQueOcurreDespuesDeLlegar() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        // Ofertas a los 2 s, 4 s, 6 s y 8 s. Llega a los 5 s y escucha hasta los 8,5 s.
        val vistas = ofertasParaColectorTardio(api, esperaMs = 5_000, duracionMs = 3_500)
        assertEquals(listOf(3, 4), vistas.map { it.juegoId }, "Se pierde las de los 2 s y los 4 s")
    }
}
