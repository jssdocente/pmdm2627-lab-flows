package com.pmdm.lab.flows.estacion5

import com.pmdm.lab.flows.api.Caos
import com.pmdm.lab.flows.api.MercadoSimulado
import com.pmdm.lab.flows.estacion5.ejercicio.TablaDeClasificacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
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

    @Test
    fun clasificacion_empiezaVacia_yDespuesSeActualiza() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val tabla = TablaDeClasificacion(api, scopePropio())
        assertTrue(tabla.clasificacion.value.isEmpty(), "El valor inicial debe ser una lista vacía")
        backgroundScope.launch { tabla.clasificacion.collect { } }
        avanza(1_100)
        assertTrue(tabla.clasificacion.value.isNotEmpty(), "Tras la primera ronda debe haber datos")
    }

    @Test
    fun clasificacion_tresColectores_unaSolaConexion() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val tabla = TablaDeClasificacion(api, scopePropio())
        repeat(3) { backgroundScope.launch { tabla.clasificacion.collect { } } }
        avanza(1_500)
        assertEquals(1, api.estadisticas().conexionesRanking, "Tres pantallas deben compartir UNA conexión")
    }

    @Test
    fun clasificacion_noReconectaSiVuelvenAntesDeLaEspera_peroSiTrasLaEspera() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val tabla = TablaDeClasificacion(api, scopePropio())

        val a = backgroundScope.launch { tabla.clasificacion.collect { } }
        avanza(2_000); a.cancel()
        avanza(2_000)
        val b = backgroundScope.launch { tabla.clasificacion.collect { } }
        avanza(700); b.cancel()
        assertEquals(1, api.estadisticas().conexionesRanking, "Menos de 5 s sin nadie: no debe reconectar")

        avanza(6_000)
        backgroundScope.launch { tabla.clasificacion.collect { } }
        avanza(700)
        assertEquals(2, api.estadisticas().conexionesRanking, "Más de 5 s sin nadie: debe volver a conectar")
    }

    @Test
    fun ultimasClasificaciones_quienLlegaTardeRecibeYaElUltimoValor() = runTest {
        val api = MercadoSimulado(backgroundScope, Caos(latenciaMs = 300))
        val tabla = TablaDeClasificacion(api, scopePropio())
        backgroundScope.launch { tabla.ultimasClasificaciones.collect { } }
        avanza(2_500)
        val antes = currentTime
        val ultima = tabla.ultimasClasificaciones.first()
        assertTrue(ultima.isNotEmpty())
        assertEquals(antes, currentTime, "Con replay = 1 el colector tardío recibe el último valor al instante")
    }
}
