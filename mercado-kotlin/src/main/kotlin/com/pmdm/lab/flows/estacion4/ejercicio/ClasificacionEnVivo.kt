package com.pmdm.lab.flows.estacion4.ejercicio

import com.pmdm.lab.flows.api.MercadoApi
import com.pmdm.lab.flows.modelo.ResumenMercado

/**
 * TU TURNO · parte 1 (flujo FRÍO).
 *
 * Lanza `colectores` colectores sobre `api.ranking()`; cada uno recoge **3 rondas** y termina.
 * Cuando todos hayan acabado, devuelve cuántas conexiones de ranking ha visto abrir el servidor
 * (`api.estadisticas().conexionesRanking`).
 *
 * Antes de escribirlo, predice: ¿cuántas conexiones esperas para 2 colectores? ¿Y para 3?
 *
 * Pista: es `contarConexionesPrecios`, con otro flujo.
 */
suspend fun contarConexionesRanking(api: MercadoApi, colectores: Int): Int =
    TODO("Pendiente de implementar")

/**
 * TU TURNO · parte 2 (flujo CALIENTE).
 *
 * Un colector llega tarde: espera `esperaMs` y entonces pide el **resumen del mercado** (`api.resumen`,
 * un `StateFlow`). Devuelve el primer valor que reciba.
 *
 * Con la simulación, el mercado emite una oferta cada 2 s y el resumen las va contando. Si el colector llega
 * a los 5 s, ¿cuántas ofertas debería ver ya en el resumen? ¿Tiene que esperar a la siguiente?
 *
 * Pista: un `StateFlow` **siempre tiene un valor**, aunque llegues tarde. Y `first()` toma solo el primero.
 */
suspend fun resumenParaColectorTardio(api: MercadoApi, esperaMs: Long): ResumenMercado =
    TODO("Pendiente de implementar")
