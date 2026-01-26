package MatrixGameJava;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.*;

public class Board {
    private final CellType[][] grid;
    public final int size;

    // SINCRONIZACION: Lock para proteger el tablero
    private final Lock lock = new ReentrantLock(true);

    // COORDINACION DE TURNOS
    private final Condition turnCondition = lock.newCondition();
    private int movesDoneInCurrentTurn = 0; // Movimientos realizados en el turno actual
    private final int totalMovers; // Neo + Agentes
    private int currentTurn = 0; // Contador de turnos
    
    // ESTADO DEL JUEGO
    // Usa volatile para visibilidad entre hilos
    private volatile boolean gameOver = false;
    private volatile String winner = null;
    
    // POSICIONES
    private Position neoPosition;
    private List<Position> agentPositions;
    private List<Position> phonePositions;
    
    // Constructor del tablero
    public Board(int numAgents, int numPhones, int numWalls, int[] neoStartPos, int size) {
        this.size = size;
        this.grid = new CellType[size][size];
        this.totalMovers = 1 + numAgents; // Neo + agentes
        this.agentPositions = new ArrayList<>();
        this.phonePositions = new ArrayList<>();
        this.neoPosition = new Position(neoStartPos[0], neoStartPos[1]);
        initializeBoard(numAgents, numPhones, numWalls, neoPosition);
    }
    
    private void initializeBoard(int numAgents, int numPhones, int numWalls, Position neoPosition) {
        // Inicializar todo como vacio
        for (int i = 0; i < size; i++) {
            Arrays.fill(grid[i], CellType.EMPTY);
        }
        
        Random rand = new Random();
        
        for (int i = 0; i < numWalls; i++) {
            Position pos = getRandomEmptyPosition(rand);
            grid[pos.getRow()][pos.getCol()] = CellType.WALL;
        }
        
        // Colocar Neo 
        neoPosition = new Position(0, 0);
        grid[0][0] = CellType.NEO;
        
        // Colocar agentes
        for (int i = 0; i < numAgents; i++) {
            Position pos = getRandomEmptyPosition(rand);
            agentPositions.add(pos);
            grid[pos.getRow()][pos.getCol()] = CellType.AGENT;
        }
        
        // Colocar teléfonos
        for (int i = 0; i < numPhones; i++) {
            Position pos = getRandomEmptyPosition(rand);
            phonePositions.add(pos);
            grid[pos.getRow()][pos.getCol()] = CellType.PHONE;
        }
    }
    
    private Position getRandomEmptyPosition(Random rand) {
        while (true) {
            int row = rand.nextInt(size);
            int col = rand.nextInt(size);
            if (grid[row][col] == CellType.EMPTY) {
                return new Position(row, col);
            }
        }
    }
    
    // Mover un personaje de forma sincronizada
    public boolean moveCharacter(Position from, Position to, CellType characterType, int agentIndex) {
        lock.lock(); // Bloquear el tablero para que solo un personaje mueva a la vez
        try {
            // Esperar a que todos los personajes del turno anterior terminen
            while (movesDoneInCurrentTurn >= totalMovers && !gameOver) {
                try {
                    turnCondition.await(); // Esperar a la señal de nuevo turno
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            if (gameOver) return false;
            
            // Verificar si el movimiento es válido
            if (!isValidMove(to)) {
                movesDoneInCurrentTurn++;
                checkTurnComplete();
                return false;
            }
            
            CellType destinationCell = grid[to.getRow()][to.getCol()];
            
            // LOGICA DE NEO
            if (characterType == CellType.NEO) {
                // Llego a un telefono
                if (destinationCell == CellType.PHONE) {
                    grid[from.getRow()][from.getCol()] = CellType.EMPTY;
                    grid[to.getRow()][to.getCol()] = CellType.NEO;
                    neoPosition = to;
                    endGame("Neo");
                    return true;
                }
                
                // Hay un agente en el destino
                if (destinationCell == CellType.AGENT) {
                    endGame("Agents");
                    return false;
                }
                
                // Movimiento normal
                grid[from.getRow()][from.getCol()] = CellType.EMPTY;
                grid[to.getRow()][to.getCol()] = CellType.NEO;
                neoPosition = to;
            }
            
            // LOGICA DE AGENTE
            else if (characterType == CellType.AGENT) {
                // Capturo a Neo
                if (destinationCell == CellType.NEO) {
                    grid[from.getRow()][from.getCol()] = CellType.EMPTY;
                    grid[to.getRow()][to.getCol()] = CellType.AGENT;
                    agentPositions.set(agentIndex, to);
                    endGame("Agents");
                    return true;
                }
                
                // Movimiento normal
                grid[from.getRow()][from.getCol()] = CellType.EMPTY;
                grid[to.getRow()][to.getCol()] = CellType.AGENT;
                agentPositions.set(agentIndex, to);
            }
            
            movesDoneInCurrentTurn++;
            checkTurnComplete();
            return true;
            
        } finally {
            lock.unlock();
        }
    }
    
    private void checkTurnComplete() {
        if (movesDoneInCurrentTurn >= totalMovers) {
            currentTurn++;
            movesDoneInCurrentTurn = 0;
            System.out.println("TURNO " + currentTurn + " COMPLETADO: ");
            printBoard();
            turnCondition.signalAll();  //notifica a todos los hilos que estan esperando esa condicion        
        }
    }
    
    private void endGame(String winnerName) {
        gameOver = true;
        winner = winnerName;
        turnCondition.signalAll();
    }
    
    private boolean isValidMove(Position pos) {
        int row = pos.getRow();
        int col = pos.getCol();
        
        // Fuera del tablero
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        
        // Pared
        if (grid[row][col] == CellType.WALL) {
            return false;
        }
        
        return true;
    }
    
    // Métodos de acceso seguros

    public List<Position> getAgentPositions() {
        lock.lock();
        try {
            return new ArrayList<>(agentPositions);
        } finally {
            lock.unlock();
        }
    }
    public Position getNeoPosition() {
        lock.lock();
        try {
            return neoPosition;
        } finally {
            lock.unlock();
        }
    }
    
    public Position getClosestPhone(Position from) {
        lock.lock();
        try {
            return phonePositions.stream()
                .min(Comparator.comparingDouble(from::distanceTo))
                .orElse(from);
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public int getCurrentTurn() {
        return currentTurn;
    }

    public CellType[][] getGrid() {
        lock.lock();
        try {
            return this.grid;
        } finally {
            lock.unlock();
        }
    }
    
    public void printBoard() {
        lock.lock();
        try {
            // Imprimir encabezado de columnas
            System.out.print("   ");
            for (int j = 0; j < size; j++) {
                System.out.printf("%2d", j);
            }
            System.out.println();
            // Imprimir filas
            for (int i = 0; i < size; i++) {
                System.out.printf("%2d ", i);
                for (int j = 0; j < size; j++) {
                    System.out.print(" " + grid[i][j].getSymbol());
                }
                System.out.println();
            }
            System.out.println();
        } finally {
            lock.unlock();
        }
    }
}