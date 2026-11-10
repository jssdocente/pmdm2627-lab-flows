# Mercado en vivo · laboratorio de corrutinas y flows

Laboratorio guiado de la **clase extra de la UT2** de PMDM (2º DAM, curso 26/27). Se trabaja en parejas: uno de los dos levanta un servidor y el otro se conecta a su IP.

La guía paso a paso está en los apuntes: <https://jssdocente.github.io/pmdm2627d>

## Qué hay aquí

| Módulo | Qué es |
| :--- | :--- |
| `mercado-kotlin` | Lo que programas tú: cada estación tiene un **ejemplo resuelto** y un **ejercicio gemelo** con `TODO` y tests. |
| `servidor` | El servidor del mercado (Ktor). Tiene un panel para provocar latencia y fallos. |

## Preparación

Necesitas JDK 17 o superior y IntelliJ IDEA.

```bash
git clone https://github.com/jssdocente/pmdm2627-lab-flows.git lab-flows
cd lab-flows
git checkout estacion-1        # una rama por estación
```

Por defecto todo funciona **sin red**, con un mercado simulado dentro de tu programa. Para conectarte al servidor de tu pareja:

1. Copia `mercado.properties.ejemplo` como `mercado.properties`.

2. Pon en `servidor.host` la IP que imprime su servidor al arrancar (`./gradlew :servidor:run`).

## Ramas y tags

- `main`: el esqueleto común (API, mercado simulado, cliente remoto y servidor).
- `estacion-N`: la estación N. Su tag `eN-ejemplo` marca el punto de partida del ejercicio.
- `soluciones`: un commit por estación, con tag `eN-solucion`. Para ver qué cambia respecto a tu punto de partida, compara `eN-ejemplo` con `eN-solucion` en GitHub.
