package MatrixGameJava;

public class AgentThread extends Thread {
    private final Board board;
    private final int agentIndex;
    private Position currentPosition;
    
    public AgentThread(Board board, int agentIndex, Position initialPosition) {
        this.board = board;
        this.agentIndex = agentIndex;
        this.currentPosition = initialPosition;
        setName("Agent-" + agentIndex + "-Thread");
    }
    
    @Override
    public void run() {
        while (!board.isGameOver()) {
            Position neoPos = board.getNeoPosition();
            
            // Calcular movimiento hacia Neo
            Position nextMove = calculateBestMove(currentPosition, neoPos);
            
            if (board.moveCharacter(currentPosition, nextMove, CellType.AGENT, agentIndex)) {
                currentPosition = nextMove;
            }
            
            // Pausa para visualizacion
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("Agent " + agentIndex + " thread terminado");
    }
    
    private Position calculateBestMove(Position current, Position target) {
        int currentRow = current.getRow();
        int currentCol = current.getCol();
        int targetRow = target.getRow();
        int targetCol = target.getCol();

        int rowDiff = targetRow - currentRow;
        int colDiff = targetCol - currentCol;

        Position[] moves;
        if (Math.abs(rowDiff) > Math.abs(colDiff)) {
            // Priorizar vertical, luego horizontal
            moves = new Position[] {
                new Position(currentRow + Integer.signum(rowDiff), currentCol),
                new Position(currentRow, currentCol + Integer.signum(colDiff))
            };
        } else {
            // Priorizar horizontal, luego vertical
            moves = new Position[] {
                new Position(currentRow, currentCol + Integer.signum(colDiff)),
                new Position(currentRow + Integer.signum(rowDiff), currentCol)
            };
        }

        for (Position move : moves) {
            if (isValidAgentMove(move)) {
                return move;
            }
        }
        // Si no puede moverse, quedarse quieto
        return current;
    }

    // Verifica si el agente puede moverse a la posición (no sale del tablero ni choca con pared)
    private boolean isValidAgentMove(Position pos) {
        int row = pos.getRow();
        int col = pos.getCol();
        if (row < 0 || row >= board.size || col < 0 || col >= board.size) return false;

        CellType cell = board.getGrid()[row][col];
        return cell != CellType.WALL;
    }
}