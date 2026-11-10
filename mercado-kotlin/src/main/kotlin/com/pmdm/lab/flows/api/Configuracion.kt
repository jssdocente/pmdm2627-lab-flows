package com.pmdm.lab.flows.api

import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.Properties

/**
 * Decide con qué mercado trabajas leyendo `mercado.properties`.
 * Si el fichero no existe, o dice `simulado`, usa la simulación local (no necesita red).
 */
object Configuracion {

    fun crearApi(scope: CoroutineScope): MercadoApi {
        val propiedades = cargar()
        val host = propiedades.getProperty("servidor.host", "simulado").trim()
        val puerto = propiedades.getProperty("servidor.puerto", "8080").trim()
        return if (host == "simulado") {
            println("Mercado: SIMULADO (sin red)")
            MercadoSimulado(scope)
        } else {
            println("Mercado: REMOTO en http://$host:$puerto")
            MercadoRemoto("http://$host:$puerto", scope)
        }
    }

    /** Busca `mercado.properties` en la carpeta actual y en las de arriba (según desde dónde lance el IDE). */
    private fun cargar(): Properties {
        val propiedades = Properties()
        var carpeta: File? = File("").absoluteFile
        repeat(4) {
            val fichero = carpeta?.resolve("mercado.properties")
            if (fichero != null && fichero.exists()) {
                fichero.reader().use { propiedades.load(it) }
                return propiedades
            }
            carpeta = carpeta?.parentFile
        }
        return propiedades
    }
}
