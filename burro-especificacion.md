# BURRO — Especificación funcional

App Android (equivalente al SKATE de skateboarding, para patines agressive) para jugar al Burro en grupo desde un solo dispositivo (modo "master game", sin gestión multidispositivo).

## Concepto del juego

- Un jugador propone/recibe un truco al azar.
- El resto de jugadores vivos, en orden, intentan el mismo truco.
- Fallar el truco cuesta una letra de la palabra **B-U-R-R-O**.
- Al completar las 5 letras, el jugador queda eliminado.
- Gana el último jugador que quede sin completar la palabra.

## Modos de juego

| Modo | Trucos disponibles |
|---|---|
| **STANDARD** | Trucos 1-10: solo BASIC. Trucos 11-25: BASIC + MEDIUM. Truco 26+: BASIC + MEDIUM + PRO |
| **BASIC** | Solo BASIC |
| **MEDIUM** | Solo MEDIUM |
| **PRO** | Solo PRO |

El contador de "número de truco" para el modo STANDARD es global a la partida (no por jugador).

## Catálogo de trucos

Cada truco tiene:
- `nombre`
- `nivel`: BASIC / MEDIUM / PRO
- `sublevel`: entero (1, 2, 3...) — controla dificultad progresiva dentro del nivel

**Ponderación de sublevel** (ajustable, valores iniciales):
- Sublevel 1: 50%
- Sublevel 2: 30%
- Sublevel 3: 20%

**Sin repetición**: un truco que ya ha salido en la partida se descarta del pool hasta agotar los del nivel correspondiente.

## Configuración de partida

1. Selección de número de jugadores + nombre de cada uno.
2. Selección de modo de juego (STANDARD / BASIC / MEDIUM / PRO).
3. Orden de turno inicial generado aleatoriamente.

## Mecánica de turno

- El jugador en turno pulsa **TRUCO** → se sortea el truco según nivel/sublevel/pool restante.
- Ese mismo truco pasa por todos los jugadores vivos, en orden, empezando por quien lo generó.
- Cada jugador tiene 3 botones:
  - **FAIL** (izquierda): el intento salió mal → **letra inmediata**, sin pasar por ningún contador.
  - **PASAR** (centro): decide no intentarlo → resta 1 de las 3 "vidas de pase" asociadas a la letra actual. Al 3er PASAR sin FAIL de por medio, se gana letra automáticamente.
  - **CHECK** (derecha): lo consigue → pasa el turno al siguiente jugador vivo con el mismo truco, sin penalización.
- **FAIL y PASAR son caminos independientes** hacia la misma letra (no comparten contador).
- El contador de "vidas de pase" (0-2) se resetea a 0 cada vez que el jugador gana una letra (sea por FAIL o por completar 3 PASAR), no se resetea con un CHECK.
- Cuando el último jugador vivo de la ronda intenta el truco, la ronda termina.
- La siguiente ronda la inicia el jugador siguiente al que inició la ronda anterior (saltando eliminados).
- Un jugador eliminado a mitad de ronda se salta en rondas siguientes.

## Muerte súbita

Se activa **únicamente** cuando, quedando exactamente 2 jugadores vivos, **ambos completan su última letra pendiente (la O) en la misma ronda**, antes de que la ronda determine un ganador de forma normal.

Mecánica:
- Se sortea un truco nuevo, con solo **FAIL / CHECK** disponibles (sin PASAR).
- Uno hace CHECK y el otro FAIL → gana quien hizo CHECK. Fin de la partida.
- Ambos CHECK o ambos FAIL → se sortea otro truco y se repite, sin más consecuencias, hasta desempatar.

Si, con 2 jugadores vivos, uno completa BURRO y el otro no en la misma ronda, gana el que no completó — sistema normal, sin muerte súbita.

## Persistencia de datos

- **SQLite vía Room** (Jetpack) para el catálogo de trucos.
- Importación inicial: catálogo definido en un JSON dentro de `assets/`, volcado a la base de datos la primera vez que arranca la app (si la tabla está vacía).
- El JSON lo mantiene el propio desarrollador por ahora; futura pantalla de administración (CRUD) para editar el catálogo desde la app sin tocar código, reutilizando la misma tabla.
- Historial de partidas (opcional, no bloqueante para v1): tabla adicional con log de trucos/resultados por partida.

## Modelo de datos (borrador)

**Trick**
- `id`, `nombre`, `nivel` (BASIC/MEDIUM/PRO), `sublevel` (Int)

**Player**
- `id`, `nombre`, `letrasAcumuladas` (0-5), `pasesEnLetraActual` (0-2), `eliminado` (Bool)

**GameSession**
- `modo`, `jugadores` (orden), `turnoActualIndex`, `contadorTrucosGlobal`, `truckActualDelTurno`, `jugadoresQueYaIntentaronEsteTruco`, `estado` (NORMAL / SUDDEN_DEATH)

## Pantallas

1. Configuración de jugadores (número + nombres)
2. Selección de modo
3. Pantalla de juego principal: nombre del jugador en turno, marcador de letras de todos los jugadores, botón TRUCO → truco revelado + FAIL/PASAR/CHECK
4. Aviso de eliminación de jugador
5. Pantalla de victoria final

## Stack técnico

- Kotlin + Jetpack Compose
- MVVM con `GameViewModel` (estado en memoria, sin backend ni red)
- Room + SQLite para catálogo de trucos (y opcionalmente historial)
- Sin gestión multidispositivo — todo en local, un solo dispositivo "master"
