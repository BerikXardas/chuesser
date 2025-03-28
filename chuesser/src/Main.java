import codedraw.*;
import codedraw.Image;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class Main {


    public static void main(String[] args) {

        Image place = CodeDraw.fromFile("src/place.png");
        Image knight = CodeDraw.fromFile("src/knight.png");
        Image bishop = CodeDraw.fromFile("src/bishop.png");
        Image rook = CodeDraw.fromFile("src/rook.png");
        Image queen = CodeDraw.fromFile("src/queen.png");
        Image king = CodeDraw.fromFile("src/king.png");
        Image emptyImage = new Image(1, 1);


        int canvasSize = 800;
        int boardSize = 600;
        int increment = boardSize/8;
        int offset = 10;
        int mouseX = 0;
        int mouseY = 0;
        int selectedX = -1;
        int selectedY = -1;
        boolean gameRunning = true;
        int score = 100;
        int selectedPiece = -1;

        CodeDraw game = new CodeDraw(canvasSize,canvasSize);

        TextFormat textFormat = game.getTextFormat();
        TextFormat numbersFormat = new TextFormat();
        numbersFormat.setTextOrigin(TextOrigin.CENTER);
        numbersFormat.setFontSize((int)(increment/1.5));
        double standardLineWidth = game.getLineWidth();

        for (int i = 0; i <= 8; i++) {
            int currentPos = i * increment + offset;
            game.drawLine(currentPos, offset, currentPos, boardSize + offset);
            game.drawLine(offset, currentPos, boardSize + offset, currentPos);
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8 ; j++) {
                if ((i+j) % 2 == 0){
                    game.setColor(Color.white);
                }
                else{
                    game.setColor(Color.black);
                }
                game.fillSquare(i * increment + offset, j * increment + offset, increment);
            }
        }

        game.setColor(Color.black);
        game.drawText(boardSize + 2*offset, offset, "Score: " + score);

        game.drawRectangle(boardSize + 2*offset, offset + increment, canvasSize - boardSize - 3*offset, 7*increment);
        game.drawImage(boardSize + 3*offset, 2* offset + increment, knight);
        game.drawImage(boardSize + 3*offset, 2* offset + 2*increment, bishop);
        game.drawImage(boardSize + 3*offset, 2* offset + 3*increment, rook);
        game.drawImage(boardSize + 3*offset, 2* offset + 4*increment, queen);
        game.drawImage(boardSize + 3*offset, 2* offset + 5*increment, king);
        game.setColor(Color.green);
        game.fillSquare(boardSize + 3*offset, 2* offset + 6*increment, increment);

        enum Piece {
            KNIGHT,
            BISHOP,
            ROOK,
            QUEEN,
            KING
        }

        int[][] board = new int[8][8];
        boolean[][] hints = new boolean[8][8];

        int[][] attempt = new int[5][2];
        int[][] solution = new int[5][2];


        for (int i = 0; i < 5; i++) {
            attempt[i] = new int[]{-1, -1};
            solution[i] = new int[]{-1, -1};
        }

        Random random = new Random();
        int setPieces = 0;
        while (setPieces < 5) {
            int file = random.nextInt(8);
            int rank = random.nextInt(8);
            boolean placementUsed = false;
            for (int i = 0; i < setPieces; i++) {
                if (Arrays.equals(solution[i], new int[]{file, rank})){
                    placementUsed = true;
                    break;
                }
            }
            if (!placementUsed){
                solution[setPieces] = new int[]{file, rank};
                board[file][rank] = -1;
                hints[file][rank] = true;
                switch (setPieces) {
                    case 0 -> {
                        int reachingY = file - 2;
                        int reachingX = rank - 1;
                        reachPosition(reachingY, reachingX, board);

                        reachingX = rank + 1;
                        reachPosition(reachingY, reachingX, board);

                        reachingY = file - 1;
                        reachingX = rank - 2;
                        reachPosition(reachingY, reachingX, board);

                        reachingX = rank + 2;
                        reachPosition(reachingY, reachingX, board);

                        reachingY = file + 1;
                        reachingX = rank - 2;
                        reachPosition(reachingY, reachingX, board);

                        reachingX = rank + 2;
                        reachPosition(reachingY, reachingX, board);

                        reachingY = file + 2;
                        reachingX = rank - 1;
                        reachPosition(reachingY, reachingX, board);

                        reachingX = rank + 1;
                        reachPosition(reachingY, reachingX, board);
                    }
                    case 1 -> {
                        for (int i = 1; i < 8; i++) {
                            reachPosition(file + i, rank + i, board);
                            reachPosition(file + i, rank - i, board);
                            reachPosition(file - i, rank + i, board);
                            reachPosition(file - i, rank - i, board);
                        }
                    }
                    case 2 -> {
                        for (int i = 0; i < 8; i++) {
                            reachPosition(file, i, board);
                            reachPosition(i, rank, board);
                        }
                    }
                    case 3 -> {
                        for (int i = 0; i < 8; i++) {
                            reachPosition(file + i, rank + i, board);
                            reachPosition(file + i, rank - i, board);
                            reachPosition(file - i, rank + i, board);
                            reachPosition(file - i, rank - i, board);
                            reachPosition(file, i, board);
                            reachPosition(i, rank, board);
                        }
                    }
                    case 4 -> {
                        reachPosition(file + 1, rank + 1, board);
                        reachPosition(file + 1, rank - 1, board);
                        reachPosition(file - 1, rank + 1, board);
                        reachPosition(file - 1, rank - 1, board);
                        reachPosition(file, rank + 1, board);
                        reachPosition(file + 1, rank, board);
                        reachPosition(file, rank - 1, board);
                        reachPosition(file - 1, rank, board);
                    }
                    default -> { }
                }
                game.drawImage(rank * increment + offset, file * increment + offset, increment, increment, place);
                game.show();
                setPieces++;
            }
        }

        printBoard(board);

        game.show();
        while (gameRunning){
            for (var event : game.getEventScanner()) {
                switch (event) {
                    case MouseClickEvent click -> {
                        mouseX = click.getX();
                        mouseY = click.getY();
                        if (mouseX >= boardSize + offset && mouseY >= boardSize + offset) {
                            gameRunning = false;
                        }
                        else if (mouseX >= offset && mouseX <= boardSize + offset && mouseY >= offset && mouseY <= boardSize + offset) {
                            int x = (int) (((double) (mouseX - offset) / increment));
                            int y = (int) (((double) (mouseY - offset) / increment));
                            if (selectedPiece >= 0){
                                if(isQuestion(y, x, board)){
                                    int placedPiece = placedPiece(y, x, attempt);
                                    Image image = place;
                                    if (placedPiece >= 0){
                                        removePiece(placedPiece, attempt);
                                    }
                                    if (placedPiece != selectedPiece) {
                                        if (selectedX >= 0 && selectedY >= 0){
                                            removePiece(selectedPiece, attempt);
                                            if ((selectedX+selectedY) % 2 == 0){
                                                game.setColor(Color.white);
                                            }
                                            else{
                                                game.setColor(Color.black);
                                            }
                                            game.fillSquare(selectedX * increment + offset, selectedY * increment + offset, increment);
                                            game.drawImage(selectedX * increment + offset, selectedY * increment + offset, increment, increment, place);
                                            game.show();
                                            selectedX = -1;
                                            selectedY = -1;
                                        }
                                        attempt[selectedPiece] = new int[]{y, x};
                                        switch (selectedPiece) {
                                            case 0 -> image = knight;
                                            case 1 -> image = bishop;
                                            case 2 -> image = rook;
                                            case 3 -> image = queen;
                                            case 4 -> image = king;
                                            default -> {}
                                        }
                                    }
                                    if ((x+y) % 2 == 0){
                                        game.setColor(Color.white);
                                    }
                                    else{
                                        game.setColor(Color.black);
                                    }
                                    game.fillSquare(x * increment + offset, y * increment + offset, increment);
                                    game.drawImage(x * increment + offset, y * increment + offset, increment, increment, image);
                                }
                                else if (selectedX >= 0 && selectedY >= 0){
                                    if ((selectedX+selectedY) % 2 == 0){
                                        game.setColor(Color.white);
                                    }
                                    else{
                                        game.setColor(Color.black);
                                    }
                                    game.fillSquare(selectedX * increment + offset, selectedY * increment + offset, increment);
                                    Image image = knight;

                                    switch (selectedPiece) {
                                        case 1 -> image = bishop;
                                        case 2 -> image = rook;
                                        case 3 -> image = queen;
                                        case 4 -> image = king;
                                        default -> {}
                                    }

                                    game.drawImage(selectedX * increment + offset, selectedY * increment + offset, increment, increment, image);
                                    game.show();
                                    selectedX = -1;
                                    selectedY = -1;
                                }
                                game.setColor(Color.white);
                                game.fillRectangle(boardSize + 2.5*offset, 1.5*offset + increment, canvasSize - boardSize - 4*offset, 7*increment - offset);
                                for (int i = 0; i < attempt.length; i++) {
                                    if (Arrays.equals(attempt[i], new int[]{-1, -1})){
                                        Image image = knight;
                                        switch (i) {
                                            case 1 -> image = bishop;
                                            case 2 -> image = rook;
                                            case 3 -> image = queen;
                                            case 4 -> image = king;
                                            default -> {}
                                        }
                                        game.drawImage(boardSize + 3*offset, 2* offset + (i+1)*increment, image);
                                    }
                                }
                                game.setColor(Color.green);
                                game.fillSquare(boardSize + 3*offset, 2* offset + 6*increment, increment);
                                game.show();
                                selectedPiece = -1;
                            }
                            else if (!hints[y][x]){
                                hints[y][x] = true;
                                score -= 2;

                                if ((x+y) % 2 == 0){
                                    game.setColor(Color.white);
                                }
                                else{
                                    game.setColor(Color.black);
                                }

                                game.fillSquare(x * increment + offset, y * increment + offset, increment);
                                game.setColor(Color.cyan);

                                game.setTextFormat(numbersFormat);
                                game.drawText(x * increment + offset + increment/2., y * increment + offset + increment/2., String.valueOf(board[y][x]));
                                game.setColor(Color.white);
                                game.fillRectangle(boardSize + 2*offset, offset, canvasSize - boardSize - 3*offset, increment);
                                game.setColor(Color.black);
                                game.setTextFormat(textFormat);
                                game.drawText(boardSize + 2*offset, offset, "Score: " + score);
                                game.show();
                            }
                            else if (isQuestion(y, x, board)){
                                int placedPiece = placedPiece(y, x, attempt);
                                if (placedPiece >= 0){
                                    selectedPiece = placedPiece;
                                    game.setColor(Color.red);
                                    game.drawSquare(x * increment + offset, y * increment + offset, increment);
                                    game.show();
                                    selectedX = x;
                                    selectedY = y;
                                }
                            }
                        }
                        else if (mouseX >= offset + boardSize && mouseX <= canvasSize - offset && mouseY >= offset + increment && mouseY <= boardSize + offset){
                            int piece = (int) ((double) (mouseY - 2*offset - increment) / increment);
                            if (piece >= 0 && piece <= 4){
                                game.setColor(Color.white);
                                game.fillRectangle(boardSize + 2.5*offset, 1.5*offset + increment, canvasSize - boardSize - 4*offset, 7*increment - offset);
                                for (int i = 0; i < attempt.length; i++) {
                                    if (Arrays.equals(attempt[i], new int[]{-1, -1})){
                                        Image image = knight;
                                        switch (i) {
                                            case 1 -> image = bishop;
                                            case 2 -> image = rook;
                                            case 3 -> image = queen;
                                            case 4 -> image = king;
                                            default -> {}
                                        }
                                        game.drawImage(boardSize + 3*offset, 2* offset + (i+1)*increment, image);
                                        if (i == piece && Arrays.equals(attempt[i], new int[]{-1, -1})){
                                            selectedPiece = i;
                                            game.setColor(Color.red);
                                            game.drawSquare(boardSize + 3*offset, 2* offset + (i+1)*increment, increment);
                                        }
                                    }
                                }
                                game.setColor(Color.green);
                                game.fillSquare(boardSize + 3*offset, 2* offset + 6*increment, increment);
                                game.show();
                            }
                            if (piece == 5){
                                boolean completedAttempt = true;
                                boolean winning = true;
                                for (int i = 0; i < attempt.length; i++) {
                                    if (Arrays.equals(attempt[i], new int[]{-1, -1})) {
                                        completedAttempt = false;
                                        break;
                                    }
                                    if (!Arrays.equals(attempt[i], solution[i])) {
                                        winning = false;
                                    }
                                }
                                if (!completedAttempt){
                                    System.out.println("PLACE ALL PIECES");
                                }
                                else if (!winning){
                                    score -= 5;
                                    System.out.println("NO!");
                                    game.setColor(Color.white);
                                    game.fillRectangle(boardSize + 2*offset, offset, canvasSize - boardSize - 3*offset, increment);
                                    game.setColor(Color.black);
                                    game.setTextFormat(textFormat);
                                    game.drawText(boardSize + 2*offset, offset, "Score: " + score);
                                    game.show();
                                }
                                else{
                                    System.out.println("A WINNER IS YOU!");
                                    System.out.println("SCORE: " + score);
                                }
                            }
                        }
                        else if (selectedPiece >= 0){
                            if (selectedX >= 0 && selectedY >= 0){
                                int placedPiece = placedPiece(selectedY, selectedX, attempt);
                                if (placedPiece >= 0){
                                    if ((selectedX+selectedY) % 2 == 0){
                                        game.setColor(Color.white);
                                    }
                                    else{
                                        game.setColor(Color.black);
                                    }
                                    game.fillSquare(selectedX * increment + offset, selectedY * increment + offset, increment);
                                    game.show();
                                    selectedX = -1;
                                    selectedY = -1;
                                }
                            }
                            else{
                                game.setColor(Color.white);
                                game.fillRectangle(boardSize + 2.5*offset, 1.5*offset + increment, canvasSize - boardSize - 4*offset, 7*increment - offset);
                                for (int i = 0; i < attempt.length; i++) {
                                    if (Arrays.equals(attempt[i], new int[]{-1, -1})){
                                        Image image = knight;
                                        switch (i) {
                                            case 1 -> image = bishop;
                                            case 2 -> image = rook;
                                            case 3 -> image = queen;
                                            case 4 -> image = king;
                                            default -> {}
                                        }
                                        game.drawImage(boardSize + 3*offset, 2* offset + (i+1)*increment, image);
                                    }
                                }
                                game.setColor(Color.green);
                                game.fillSquare(boardSize + 3*offset, 2* offset + 6*increment, increment);
                                game.show();
                            }
                            selectedPiece = -1;
                        }
                    }
                    case KeyPressEvent keystroke -> {
                        Key key = keystroke.getKey();
                        if (key.equals(Key.ESCAPE)){
                            gameRunning = false;
                        }
                    }
                    default -> { }
                }
            }
        }
        game.close();
    }

    public static void reachPosition(int y, int x, int[][] board){
        if (x >= 0 && x <= 7 && y >= 0 && y <= 7 && board[y][x] >= 0){
            board[y][x] = board[y][x] + 1;
        }
    }

    public static void printBoard(int[][] board){
        for (int[] row : board)
            System.out.println(Arrays.toString(row));
    }

    public static boolean isQuestion(int y, int x, int[][] board){
        return board[y][x] == -1;
    }

    public static void removePiece(int piece, int[][] attempt){
        attempt[piece] = new int[]{-1, -1};
    }
    public static int placedPiece(int y, int x, int[][] attempt){
        int[] check = {y, x};
        for (int i = 0; i < attempt.length; i++) {
            if (Arrays.equals(attempt[i], check)){
                return i;
            }
        }
        return -1;
    }
}