package MatrixGameJava;

import java.util.ArrayList;
import java.util.List;

public class MatrixGame {
    public static void main(String[] args) {
        System.out.println("MATRIX CONCURRENCY GAME");
        System.out.println("N = Neo");
        System.out.println("A = Agente");
        System.out.println("T = Telefono");
        System.out.println("W = Pared");
        System.out.println(". = Espacio vacio\n");

        // Configuracion del juego
        int numAgents = 2;
        int numPhones = 3;
        int numWalls = 5;
        int boardSize = 10;
        int neoPos []= {0, 0}; // Posicion inicial de Neo
        
        
        // Crear el tablero
        Board board = new Board(numAgents, numPhones, numWalls, neoPos, boardSize);
        System.out.println("TABLERO INICIAL:");
        board.printBoard();

        long startTime = System.currentTimeMillis();
        
        // Crear hilos
        List<Thread> threads = new ArrayList<>();
        
        // Hilo de Neo
        NeoThread neoThread = new NeoThread(board);
        threads.add(neoThread);
        
        // Hilos de agentes
        for (int i = 0; i < numAgents; i++) {
            Position agentPos = board.getAgentPositions().get(i);
            AgentThread agentThread = new AgentThread(board, i, agentPos);
            threads.add(agentThread);
        }
        
        // Iniciar todos los hilos
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Esperar a que todos terminen
        for (Thread thread : threads) {
            try {
                thread.join(); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Resultados finales
        System.out.println("\nJUEGO TERMINADO");
        System.out.println("Ganador: " + board.getWinner());
        System.out.println("Total de turnos: " + board.getCurrentTurn());
        System.out.println("Tiempo de ejecucion: " + (endTime - startTime) + " ms");
        System.out.println("\nTABLERO FINAL:");
        board.printBoard();
    }
}