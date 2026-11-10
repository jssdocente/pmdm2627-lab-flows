plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

tasks.test {
    useJUnitPlatform()
}

// Atajo para comprobar desde la terminal: ./gradlew :mercado-kotlin:ejecuta -Pclase=com.pmdm.lab.flows.estacion1.ejemplo.MainKt
tasks.register<JavaExec>("ejecuta") {
    group = "laboratorio"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(providers.gradleProperty("clase"))
}
