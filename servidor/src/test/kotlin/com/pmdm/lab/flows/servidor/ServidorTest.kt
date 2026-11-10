package com.pmdm.lab.flows.servidor

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoRemoto
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.modelo.ErrorMercado
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Prueba de punta a punta: el cliente remoto contra el servidor real, en el mismo proceso. */
class ServidorTest {

    private fun conServidor(bloque: suspend CoroutineScope.(MercadoRemoto, MercadoSimulado) -> Unit) = runBlocking {
        val puerto = ServerSocket(0).use { it.localPort }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val mercado = MercadoSimulado(scope, Caos(latenciaMs = 20))
        val servidor = embeddedServer(Netty, port = puerto, host = "127.0.0.1") { configurar(mercado) }.start()
        try {
            bloque(MercadoRemoto("http://127.0.0.1:$puerto", scope), mercado)
        } finally {
            servidor.stop(0, 0)
            scope.cancel()
        }
    }

    @Test
    fun laspeticionesUnicasFuncionanPorHttp() = conServidor { api, _ ->
        assertEquals(6, api.catalogo().size)
        assertEquals("Ana", api.perfil(8).nombre)
        assertEquals(3, api.amigos(1).size)
    }

    @Test
    fun unFalloDelServidorLlegaComoErrorMercado() = conServidor { api, mercado ->
        mercado.caos = Caos(latenciaMs = 20, siempreFallan = setOf("logros"))
        assertFailsWith<ErrorMercado> { api.logros(1) }
        api.perfil(1)
    }

    @Test
    fun cadaCollectDeUnFlujoFrioAbreUnaConexionNueva() = conServidor { api, _ ->
        api.precios(1).take(2).toList()
        api.precios(1).take(2).toList()
        assertEquals(2, api.estadisticas().conexionesPrecios)
    }

    @Test
    fun elFlujoCalienteLlegaAlosColectoresSinAbrirConexionesNuevas() = conServidor { api, _ ->
        val oferta = withTimeout(5_000) { api.ofertas.first() }
        assertTrue(oferta.descuento >= 10)
        val resumen = withTimeout(5_000) { api.resumen.first { it.ofertasEmitidas > 0 } }
        assertTrue(resumen.ultimaOferta != null)
    }
}
