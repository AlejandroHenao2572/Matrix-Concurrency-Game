package MatrixGameJava;

//Definicion de tipos de celdas en el juego
public enum CellType {
    EMPTY('.'),
    NEO('N'),
    AGENT('A'),
    PHONE('T'),
    WALL('W');
    
    private final char symbol;
    
    CellType(char symbol) {
        this.symbol = symbol;
    }
    
    public char getSymbol() {
        return symbol;
    }
}

