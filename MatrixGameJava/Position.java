package MatrixGameJava;

public class Position {
    private final int row;
    private final int col;
    
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    
    // Calcular distancia Manhattan, para encontrar el objetivo mas cercano
    public double distanceTo(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }
    
}