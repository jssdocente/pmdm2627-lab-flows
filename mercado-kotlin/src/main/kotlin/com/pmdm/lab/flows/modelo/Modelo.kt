package com.pmdm.lab.flows.modelo

import kotlinx.serialization.Serializable
import java.io.IOException

/** Un juego del catálogo de la tienda. */
@Serializable
data class Juego(val id: Int, val titulo: String, val precio: Double)

/** Información extra de un juego: hace falta una segunda llamada para obtenerla. */
@Serializable
data class Detalle(val juegoId: Int, val genero: String, val valoracion: Double)

/** Una cotización del precio de un juego. `numero` crece con cada cotización nueva. */
@Serializable
data class Precio(val juegoId: Int, val valor: Double, val numero: Int)

/** Una oferta relámpago: ocurre una vez y la ven todos los que estén escuchando. */
@Serializable
data class Oferta(val juegoId: Int, val titulo: String, val descuento: Int)

/** Foto del mercado: siempre tiene un valor, aunque aún no haya pasado nada. */
@Serializable
data class ResumenMercado(val ofertasEmitidas: Int = 0, val ultimaOferta: String? = null)

@Serializable
data class Perfil(val id: Int, val nombre: String, val nivel: Int)

@Serializable
data class Logro(val id: Int, val titulo: String)

@Serializable
data class Amigo(val id: Int, val nombre: String)

/** Una fila del ranking de jugadores. */
@Serializable
data class Puesto(val posicion: Int, val jugador: String, val puntos: Int)

/** Lo que cuenta el servidor sobre ti: sirve para comprobar cuántas conexiones abre tu código. */
@Serializable
data class Estadisticas(
    val peticiones: Int,
    val conexionesPrecios: Int,
    val conexionesRanking: Int,
)

/** Fallo de comunicación con el mercado (el equivalente a un «503» o a una caída de red). */
class ErrorMercado(mensaje: String) : IOException(mensaje)
