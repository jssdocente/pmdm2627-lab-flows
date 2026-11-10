package com.pmdm.lab.flows.estacion2.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.estacion2.intentar
import com.pmdm.lab.flows.modelo.Amigo
import com.pmdm.lab.flows.modelo.Logro
import com.pmdm.lab.flows.modelo.Perfil
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

/**
 * El panel de un jugador. El perfil es imprescindible; los logros y los amigos son extras:
 * si fallan, el panel se ve igualmente y esa sección muestra un error.
 */
class PanelDeJugador(
    val perfil: Perfil,
    val logros: Result<List<Logro>>,
    val amigos: Result<List<Amigo>>,
)

/**
 * TU TURNO · parte 1.
 *
 * Carga el perfil, los logros y los amigos **a la vez**, con estas reglas:
 *
 * - Si falla el **perfil**, falla todo el panel (la excepción sale de la función).
 * - Si fallan los **logros** o los **amigos**, el panel se devuelve igualmente con un `Result` fallido
 *   en esa sección, y la otra sección NO se cancela.
 *
 * Con 300 ms por petición debe tardar 300 ms, falle lo que falle.
 *
 * Pista: es `cargarPortadaResistente`, con una sección opcional más.
 */
suspend fun cargarPanelDeJugador(api: MercadoApi, jugadorId: Int): PanelDeJugador = supervisorScope {
    val perfil = async { api.perfil(jugadorId) }
    val logros = async { api.logros(jugadorId) }
    val amigos = async { api.amigos(jugadorId) }
    PanelDeJugador(perfil.await(), intentar { logros.await() }, intentar { amigos.await() })
}
