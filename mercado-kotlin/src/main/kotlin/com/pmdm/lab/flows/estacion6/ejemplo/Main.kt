package com.pmdm.lab.flows.estacion6.ejemplo

import com.pmdm.lab.flows.api.Configuracion
import kotlinx.coroutines.*

/**
 * EJEMPLO RESUELTO · Estación 6.
 *
 * Antes de ejecutarlo, PREDICE: escribes «pix» letra a letra con 100 ms entre teclas y el buscador
 * espera 300 ms. ¿Cuántas peticiones al catálogo se hacen por esa búsqueda?
 */
fun main() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    val api = Configuracion.crearApi(scope)
    val buscador = BuscadorDeJuegos(api, scope)

    val observador = launch {
        buscador.resultados.collect { lista ->
            println("  resultados → ${lista.joinToString { (if (it.favorito) "★" else "") + it.juego.titulo }}")
        }
    }
    suspend fun peticiones() = api.estadisticas().peticiones

    delay(1_500)
    val antes = peticiones()
    println("Escribo «pix» tecla a tecla (100 ms entre teclas)…")
    buscador.escribir("p"); delay(100)
    buscador.escribir("pi"); delay(100)
    buscador.escribir("pix"); delay(1_500)
    println("Peticiones al catálogo por esta búsqueda: ${peticiones() - antes}")

    println("Marco el favorito (id 1)…")
    val antesFav = peticiones()
    buscador.alternarFavorito(1); delay(300)
    println("Peticiones nuevas al marcar un favorito: ${peticiones() - antesFav}")

    observador.cancelAndJoin()
    scope.cancel(CancellationException("fin"))
}
