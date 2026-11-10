package com.pmdm.lab.flows.api

import com.pmdm.lab.flows.modelo.ErrorMercado
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MercadoSimuladoTest {

    @Test
    fun cadaPeticionTardaLaLatenciaConfigurada() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 250))
        api.catalogo()
        assertEquals(250, currentTime)
    }

    @Test
    fun unaPeticionPuedeFallarSiempre() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(siempreFallan = setOf("logros")))
        assertFailsWith<ErrorMercado> { api.logros(1) }
        api.perfil(1) // las demás siguen funcionando
    }
}
