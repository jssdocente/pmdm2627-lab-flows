package com.pmdm.lab.flows.api

import com.pmdm.lab.flows.modelo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

/**
 * Cómo se porta el «servidor»: cuánto tarda y cuándo falla.
 *
 * @property latenciaMs lo que tarda cada petición.
 * @property probabilidadFallo de 0.0 (nunca falla) a 1.0 (siempre falla).
 * @property siempreFallan peticiones concretas que fallan siempre: `"logros"`, `"amigos"`, `"detalle"`…
 */
@Serializable
data class Caos(
    val latenciaMs: Long = 300,
    val probabilidadFallo: Double = 0.0,
    val siempreFallan: Set<String> = emptySet(),
)

/**
 * El mercado, simulado dentro de tu propio programa. No usa red, así que funciona en cualquier aula.
 * También es el que usa el servidor por dentro para contestar a las peticiones.
 *
 * @param scope donde vive la corrutina que va generando las ofertas (el flujo caliente).
 */
class MercadoSimulado(
    scope: CoroutineScope,
    caos: Caos = Caos(),
    private val azar: Random = Random(7),
) : MercadoApi {

    @Volatile
    var caos: Caos = caos

    private val peticiones = AtomicInteger()
    private val conexionesPrecios = AtomicInteger()
    private val conexionesRanking = AtomicInteger()

    // ---------- Datos de mentira (siempre los mismos) ----------

    private val juegos = listOf(
        Juego(1, "Pixel Quest", 19.99),
        Juego(2, "Turbo Kart", 29.99),
        Juego(3, "Dungeon Cook", 14.99),
        Juego(4, "Space Farm", 24.99),
        Juego(5, "Ninja Bakery", 9.99),
        Juego(6, "Robo Chess", 34.99),
    )
    private val generos = listOf("Aventura", "Carreras", "Rol", "Simulación", "Puzle", "Estrategia")
    private val nombres = listOf("Ana", "Beto", "Carla", "Dani", "Eli", "Fran", "Gema", "Hugo")

    private fun nombreDe(id: Int) = nombres[id % nombres.size]

    // ---------- Peticiones únicas ----------

    /** Lo que tarda cualquier petición: espera, puede fallar y, si no, devuelve el valor. */
    private suspend fun <T> responder(peticion: String, valor: () -> T): T {
        peticiones.incrementAndGet()
        delay(caos.latenciaMs)
        val falla = peticion in caos.siempreFallan ||
            synchronized(azar) { azar.nextDouble() } < caos.probabilidadFallo
        if (falla) throw ErrorMercado("503: «$peticion» no disponible")
        return valor()
    }

    override suspend fun catalogo() = responder("catalogo") { juegos }

    override suspend fun detalle(juegoId: Int) = responder("detalle") {
        Detalle(juegoId, generos[juegoId % generos.size], 3.0 + (juegoId % 3))
    }

    override suspend fun perfil(jugadorId: Int) = responder("perfil") {
        Perfil(jugadorId, nombreDe(jugadorId), nivel = 10 + jugadorId)
    }

    override suspend fun logros(jugadorId: Int) = responder("logros") {
        List(2 + jugadorId % 3) { Logro(it + 1, "Logro ${it + 1} de ${nombreDe(jugadorId)}") }
    }

    /** Los amigos de `n` son siempre `n+1`, `n+2` y `n+3`: así se pueden consultar después sus fichas. */
    override suspend fun amigos(jugadorId: Int) = responder("amigos") {
        List(3) { Amigo(jugadorId + it + 1, nombreDe(jugadorId + it + 1)) }
    }

    // ---------- Flujos fríos: cada collect es una conexión nueva ----------

    override fun precios(juegoId: Int): Flow<Precio> = flow {
        conexionesPrecios.incrementAndGet()
        val base = juegos.first { it.id == juegoId }.precio
        var numero = 0
        while (true) {
            delay(PERIODO_PRECIO_MS)
            numero++
            emit(Precio(juegoId, redondear(base * (1 + 0.1 * sin(numero * 0.7))), numero))
        }
    }

    override fun ranking(): Flow<List<Puesto>> = flow {
        conexionesRanking.incrementAndGet()
        var ronda = 0
        while (true) {
            delay(PERIODO_RANKING_MS)
            ronda++
            val lista = nombres.mapIndexed { i, n -> n to (100 * (nombres.size - i) + ronda * (i + 1) * 7) }
                .sortedByDescending { it.second }
                .mapIndexed { i, (n, puntos) -> Puesto(i + 1, n, puntos) }
            emit(lista)
        }
    }

    // ---------- Flujos calientes: existen aunque nadie escuche ----------

    private val _ofertas = MutableSharedFlow<Oferta>(extraBufferCapacity = 8)
    private val _resumen = MutableStateFlow(ResumenMercado())

    override val ofertas: SharedFlow<Oferta> = _ofertas.asSharedFlow()
    override val resumen: StateFlow<ResumenMercado> = _resumen.asStateFlow()

    init {
        scope.launch {
            var i = 0
            while (true) {
                delay(PERIODO_OFERTA_MS)
                val juego = juegos[i % juegos.size]
                val oferta = Oferta(juego.id, juego.titulo, descuento = 10 + (i % 4) * 10)
                _ofertas.emit(oferta)
                _resumen.update { it.copy(ofertasEmitidas = it.ofertasEmitidas + 1, ultimaOferta = oferta.titulo) }
                i++
            }
        }
    }

    override suspend fun estadisticas() =
        Estadisticas(peticiones.get(), conexionesPrecios.get(), conexionesRanking.get())

    private fun redondear(valor: Double) = round(valor * 100) / 100

    companion object {
        const val PERIODO_PRECIO_MS = 500L
        const val PERIODO_RANKING_MS = 1_000L
        const val PERIODO_OFERTA_MS = 2_000L
    }
}
