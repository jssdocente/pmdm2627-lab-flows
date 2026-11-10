package com.pmdm.lab.flows.servidor

import com.pmdm.lab.flows.api.MercadoSimulado
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Arranca el servidor del mercado. Tu pareja se conectará a la IP que se imprime aquí.
 *
 *     ./gradlew :servidor:run
 */
fun main(args: Array<String>) {
    val puerto = args.firstOrNull()?.toIntOrNull() ?: 8099
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val mercado = MercadoSimulado(scope)

    imprimirDirecciones(puerto)
    embeddedServer(Netty, port = puerto, host = "0.0.0.0") { configurar(mercado) }.start(wait = true)
}

private fun imprimirDirecciones(puerto: Int) {
    val ips = NetworkInterface.getNetworkInterfaces().asSequence()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.asSequence() }
        .filterIsInstance<Inet4Address>()
        .map { it.hostAddress }
        .toList()

    println()
    println("==========================================================")
    println("  Servidor del mercado en marcha (puerto $puerto)")
    println("  Dile a tu pareja que use una de estas IPs:")
    ips.forEach { println("      $it") }
    println("  Panel de control (caos y estadísticas): http://localhost:$puerto")
    println("==========================================================")
    println()
}
