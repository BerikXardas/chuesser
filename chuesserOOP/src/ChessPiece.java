import codedraw.CodeDraw;
import codedraw.Image;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class ChessPiece {

    private static final Image knight = CodeDraw.fromFile("src/knight.png");
    private static final Image bishop = CodeDraw.fromFile("src/bishop.png");
    private static final Image rook = CodeDraw.fromFile("src/rook.png");
    private static final Image queen = CodeDraw.fromFile("src/queen.png");
    private static final Image king = CodeDraw.fromFile("src/king.png");

    private final String name;
    private final int straightLine;
    private final int diagonalLine;
    private final int[][] specialMoves;
    private final int[] coordinates = {-1, -1};
    public int[] getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(int rank, int file) {
        this.coordinates[0] = rank;
        this.coordinates[1] = file;
    }
    private Image image = new Image(64,64, Color.white);
    public Image getImage() {
        return image;
    }

    public ChessPiece(String name, int straightLine, int diagonalLine, int[][] specialMoves, Image image) {
        this.name = name;
        this.straightLine = straightLine;
        this.diagonalLine = diagonalLine;
        this.specialMoves = specialMoves;
        this.image = image;
    }

    public ChessPiece(String name) {
        this.name = name;
        if (name.toLowerCase().startsWith("knight")){
            this.specialMoves = new int[][]{{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
            this.straightLine = 0;
            this.diagonalLine = 0;
            this.image = knight;
        } else if (name.toLowerCase().startsWith("bishop")){
            this.specialMoves = new int[0][0];
            this.straightLine = 0;
            this.diagonalLine = Integer.MAX_VALUE;
            this.image = bishop;
        } else if (name.toLowerCase().startsWith("rook")){
            this.specialMoves = new int[0][0];
            this.straightLine = Integer.MAX_VALUE;
            this.diagonalLine = 0;
            this.image = rook;
        } else if (name.toLowerCase().startsWith("queen")){
            this.specialMoves = new int[0][0];
            this.straightLine = Integer.MAX_VALUE;
            this.diagonalLine = Integer.MAX_VALUE;
            this.image = queen;
        } else if (name.toLowerCase().startsWith("king")){
            this.specialMoves = new int[0][0];
            this.straightLine = 1;
            this.diagonalLine = 1;
            this.image = king;
        }
        else{
            this.specialMoves = new int[0][0];
            this.straightLine = 0;
            this.diagonalLine = 0;
        }
    }

    public void simulateMovements(int[][] board){
        int straight = Math.min(board.length, this.straightLine);
        int skewed = Math.min(board.length, this.diagonalLine);
        int rank = coordinates[0];
        int file = coordinates[1];
        for (int i = 1; i <= straight; i++) {
            reachPosition(rank + i, file, board);
            reachPosition(rank - i, file, board);
            reachPosition(rank, file + i, board);
            reachPosition(rank, file - i, board);
        }
        for (int i = 1; i <= skewed; i++) {
            reachPosition(rank + i, file + i, board);
            reachPosition(rank + i, file - i, board);
            reachPosition(rank - i, file + i, board);
            reachPosition(rank - i, file - i, board);
        }
        for (int[] movement : this.specialMoves){
            reachPosition(rank + movement[0], file + movement[1], board);
        }
    }

    private void reachPosition(int rank, int file, int[][] board) {
        if (rank >= 0 && rank < board.length && file >= 0 && file < board[rank].length && board[rank][file] > -1) {
            board[rank][file] += 1;
        }
    }
    protected ChessPiece clone() {
        return new ChessPiece(name, straightLine, diagonalLine, specialMoves, image);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece that)) return false;
        return straightLine == that.straightLine && diagonalLine == that.diagonalLine && Arrays.equals(coordinates, that.coordinates) &&
                Arrays.deepEquals(specialMoves, that.specialMoves);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(straightLine, diagonalLine, Arrays.hashCode(coordinates));
        result = 31 * result + Arrays.deepHashCode(specialMoves);
        return result;
    }

    public void scaleImage(int squareSize) {
        double scaleX = (double) squareSize / image.getWidth();
        double scaleY = (double) squareSize / image.getHeight();
        double scale = Math.min(scaleX, scaleY);
        image = Image.scale(image, scale);
    }
}
