# Matrix Concurrency Game

## Descripción General

Matrix Concurrency Game es una simulación inspirada en la saga de Matrix, donde Neo debe llegar a un teléfono para escapar, mientras agentes intentan capturarlo. El juego se desarrolla en un tablero representado por una matriz, y utiliza concurrencia para manejar los movimientos simultaneos de Neo y los agentes.

## Objetivo del Juego
- **Neo**: Su objetivo es llegar a cualquier teléfono del tablero para ganar.
- **Agentes**: Su objetivo es capturar a Neo antes de que llegue a un teléfono.

## Elementos del Tablero
- `N`: Neo
- `A`: Agente
- `T`: Teléfono
- `W`: Pared
- `.`: Espacio vacío

## Reglas del Juego
- Neo y los agentes se mueven por turnos
- Neo siempre intenta acercarse al teléfono más cercano, evitando paredes.
- Los agentes persiguen a Neo.
- Si Neo llega a un teléfono, gana el juego.
- Si un agente alcanza la posición de Neo, los agentes ganan.
- Los movimientos no pueden atravesar paredes ni salirse del tablero.

## Concurrencia y Sincronización
El juego utiliza concurrencia para simular el movimiento simultáneo de Neo y los agentes:

- **Hilos (Threads):**
	- Cada personaje (Neo y cada agente) es controlado por un hilo independiente (`NeoThread` y `AgentThread`).
	- Todos los hilos se ejecutan en paralelo, pero sus movimientos están coordinados por turnos.

- **Sincronización:**
	- Se utiliza un `ReentrantLock` para proteger el acceso al tablero y evitar condiciones de carrera.
	- Un `Condition` asociado al lock permite coordinar los turnos: los hilos esperan hasta que todos hayan realizado su movimiento antes de iniciar el siguiente turno.
	- El método `moveCharacter` en la clase `Board` asegura que solo un personaje mueva a la vez y que todos los movimientos de un turno se completen antes de avanzar.
	- El estado del juego `gameOver`, `winner` es `volatile` para garantizar la visibilidad entre hilos.
    - `await/signalAll`: Comunicación entre hilos


## Ejecución
1. Al iniciar el juego, se imprime el tablero inicial con la ubicación de todos los elementos.
2. Cada hilo realiza su movimiento en orden de turno.
3. El tablero se imprime al finalizar cada turno, mostrando el avance de los personajes.
4. El juego termina cuando Neo gana o es capturado, mostrando el ganador, el número de turnos y el tiempo de ejecución.
