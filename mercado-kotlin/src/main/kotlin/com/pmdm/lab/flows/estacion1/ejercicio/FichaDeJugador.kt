package com.pmdm.lab.flows.estacion1.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.Amigo
import com.pmdm.lab.flows.modelo.Logro
import com.pmdm.lab.flows.modelo.Perfil

/** Lo que muestra la pantalla de un jugador: su perfil, sus logros y sus amigos. */
class FichaDeJugador(val perfil: Perfil, val logros: List<Logro>, val amigos: List<Amigo>)

/**
 * TU TURNO · parte 1.
 *
 * Carga el perfil, los logros y los amigos de `jugadorId` **a la vez**.
 * Las tres peticiones son independientes, así que el tiempo total debe ser el de la más lenta
 * (300 ms con la configuración de los tests), no la suma (900 ms).
 *
 * Pista: mira cómo `cargarVitrinaParalela` lanza los detalles con `async`.
 */
suspend fun cargarFicha(api: MercadoApi, jugadorId: Int): FichaDeJugador =
    TODO("Pendiente de implementar")

/**
 * TU TURNO · parte 2.
 *
 * Devuelve la ficha completa de **cada amigo** de `jugadorId`.
 * Hay una dependencia (primero hay que saber quiénes son los amigos) y después cosas independientes
 * (las fichas de los amigos, que se cargan todas a la vez).
 *
 * Con 300 ms por petición debe tardar 600 ms: 300 de los amigos + 300 de todas las fichas juntas.
 *
 * Pista: es el mismo patrón «lo dependiente, en orden; lo independiente, a la vez».
 */
suspend fun cargarFichasDeAmigos(api: MercadoApi, jugadorId: Int): List<FichaDeJugador> =
    TODO("Pendiente de implementar")
