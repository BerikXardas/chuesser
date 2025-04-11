import codedraw.*;
import codedraw.Image;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class Chuesser {

    private static int score = 100; // We start with 100 - each hint deducts 2, each wrong guess deducts 5
    private static Piece selectedPiece = Piece.NONE; // The currently selected piece

    // this enum improves human readability, as it is error-prone to rely on numbers only
    // the order must be adhered on other logics such as placements and images!
    private enum Piece {
        NONE,
        KNIGHT,
        BISHOP,
        ROOK,
        QUEEN,
        KING
    }
    public static void main(String[] args) {

        final int canvasSize = 800; // the size of everything is calculated in relation to the canvas size
        final int squareSize = canvasSize * 3 / 32; // for an 8x8 board, it should be 1/8 of 3/4 of the canvas
        final int boardSize = squareSize * 8; // the board should take ~3/4 of the canvas. Calculated relatively to squareSize to avoid truncating
        final int offset = canvasSize/100;

        CodeDraw game = new CodeDraw(canvasSize,canvasSize);

        // the number of rows/columns/squares is currently hardcoded as an 8x8 standard chess board
        int[][] board = new int[8][8]; // entries represent the count of pieces which can move there, -1 if a piece is placed there
        boolean[][] hints = new boolean[8][8]; // saves which hints have already been "bought"

        // placement logic is currently hard coded in order to simulate a hashtable:
        // 0: represents "no piece" (will be needed on other occasions)
        // 1: knight, 2: bishop, 3: rook, 4: queen, 5: king
        // in other words: must adhere the order of the Piece enum!
        // inner arrays represent rank(=row), file(=column) in this order. rank, column [-1, -1] means "not placed"
        int[][] placements = generatePlacement(board, hints); // the placement the player has to find
        int[][] attempt = new int[6][2]; // the current placement the player set
        for (int i = 0; i < attempt.length; i++) {
            attempt[i] = new int[]{-1, -1};
        }

        Image place = CodeDraw.fromFile("src/place.png");
        Image knight = CodeDraw.fromFile("src/knight.png");
        Image bishop = CodeDraw.fromFile("src/bishop.png");
        Image rook = CodeDraw.fromFile("src/rook.png");
        Image queen = CodeDraw.fromFile("src/queen.png");
        Image king = CodeDraw.fromFile("src/king.png");
        Image[] images = {place, knight, bishop, rook, queen, king}; // must adhere the order of the Piece enum!
        for (int i = 0; i < images.length; i++) {
            double scaleX = (double) squareSize / images[i].getWidth();
            double scaleY = (double) squareSize / images[i].getHeight();
            double scale = Math.min(scaleX, scaleY);
            images[i] =  Image.scale(images[i], scale);
        }

        //printBoard(board);
        drawGame(game, boardSize, squareSize, offset, board, hints, attempt, placements, images);
        game.show();

        boolean gameRunning = true;
        while (gameRunning && !game.isClosed() && score > 0){
            for (var event : game.getEventScanner()) {
                switch (event) {
                    case MouseClickEvent click -> {
                        int mouseX = click.getX();
                        int mouseY = click.getY();
                        gameRunning = handleMouseClick(mouseX, mouseY, boardSize, squareSize, offset, board, hints, attempt, placements);
                        drawGame(game, boardSize, squareSize, offset, board, hints, attempt, placements, images);
                    }
                    case KeyPressEvent keystroke -> {
                        Key key = keystroke.getKey();
                        if (key.equals(Key.ESCAPE)){
                            gameRunning = false;
                        }
                        else if (key.equals(Key.ENTER)){
                            if (isAttemptComplete(attempt)){
                                gameRunning = !submitAttempt(attempt, placements);
                            }
                        }
                    }
                    default -> { }
                }
            }
        }
        if (score <= 0){
            System.out.println("GAME OVER");
        }
        game.close();
    }

    private static void drawGame(CodeDraw game, int boardSize, int squareSize, int offset, int[][] board, boolean[][] hints, int[][] attempt, int[][] placements, Image[] images){

        TextFormat numbersFormat = new TextFormat();
        numbersFormat.setTextOrigin(TextOrigin.CENTER);
        numbersFormat.setFontSize((int)(squareSize/1.5));

        TextFormat textFormat = new TextFormat();
        textFormat.setTextOrigin(TextOrigin.CENTER_LEFT);
        textFormat.setFontSize((int)(squareSize/3.));

        game.clear(Color.white);

        // draw board
        game.setColor(Color.black);
        for (int i = 0; i <= 8; i++) {
            int currentPos = i * squareSize + offset;
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
                game.fillSquare(i * squareSize + offset, j * squareSize + offset, squareSize);

                if (hints[j][i] && board[j][i] != -1){
                    game.setColor(Color.cyan);
                    game.setTextFormat(numbersFormat);
                    game.drawText(i * squareSize + offset + squareSize/2., j * squareSize + offset + squareSize/2., String.valueOf(board[j][i]));
                }
            }
        }

        // display score
        game.setColor(Color.black);
        game.setTextFormat(textFormat);
        game.drawText(boardSize + 2*offset, offset + 0.5*squareSize, "Score: " + score);

        // sidebar, pieces, placement markers
        game.drawRectangle(boardSize + 2*offset, offset + squareSize, 2*squareSize + 2*offset, 7*squareSize);
        int[] notPlaced = {-1, -1};
        for (int i = 1; i < attempt.length; i++) {
            if (Arrays.equals(attempt[i], notPlaced)) {
                game.drawImage(boardSize + 3*offset, 2*offset + i*squareSize, images[i]);
            }
            else {
                game.drawImage( attempt[i][1] * squareSize + offset, attempt[i][0] * squareSize + offset, images[i]);
            }
        }
        for (int i = 1; i < placements.length; i++) {
            if (placedPiece(placements[i][0], placements[i][1], attempt) == Piece.NONE){
                game.drawImage( placements[i][1] * squareSize + offset, placements[i][0] * squareSize + offset, images[0]);
            }
        }

        // selection marker
        if (selectedPiece != Piece.NONE){
            int pieceIndex = selectedPiece.ordinal();
            game.setColor(Color.red);
            if (Arrays.equals(attempt[pieceIndex], notPlaced)){
                game.drawSquare( boardSize + 3*offset, 2*offset + pieceIndex*squareSize, squareSize);
            }
            else{
                game.drawSquare( attempt[pieceIndex][1] * squareSize + offset, attempt[pieceIndex][0] * squareSize + offset, squareSize);
            }
        }

        // footer / buttons
        game.setColor(Color.green);
        game.fillRectangle(offset,boardSize + 2*offset, 2*squareSize, squareSize);
        game.setColor(Color.black);
        game.drawText(offset,boardSize + 0.5*squareSize + 2*offset, "Submit");

        game.setColor(Color.red);
        game.fillRectangle(offset,boardSize + squareSize + 3*offset, 2*squareSize, squareSize);
        game.setColor(Color.black);
        game.drawText(offset,boardSize + 1.5*squareSize + 3*offset, "Exit");

        game.show();
    }


    // generates the placement the player has to guess
    // the placements array simulates a hashtable, with the piece identifyer as the key and a pair of coordinates as the value
    // order of the Piece enum must be adhered!
    private static int[][] generatePlacement(int[][] board, boolean[][] hints){
        Random random = new Random();

        int[][] placements = new int[6][2];
        for (int i = 0; i < placements.length; i++) {
            placements[i] = new int[]{-1, -1};
        }

        int pieceIndex = 1;
        while (pieceIndex < Piece.values().length) {
            int rank = random.nextInt(8);
            int file = random.nextInt(8);
            boolean coordinatesUsed = false;
            for (int i = 1; i < pieceIndex; i++) {
                if (Arrays.equals(placements[i], new int[]{rank, file})){
                    coordinatesUsed = true;
                    break;
                }
            }
            if (!coordinatesUsed){
                placements[pieceIndex] = new int[]{rank, file};
                board[rank][file] = -1;
                hints[rank][file] = true;
                Piece piece = Piece.values()[pieceIndex];
                simulateMovements(piece, rank, file, board);
                pieceIndex++;
            }
        }
        return placements;
    }

    private static void simulateMovements(Piece piece, int rank, int file, int[][] board){
        switch (piece) {
            case KNIGHT -> {
                int reachingRank = rank - 2;
                int reachingFile = file - 1;
                reachPosition(reachingRank, reachingFile, board);

                reachingFile = file + 1;
                reachPosition(reachingRank, reachingFile, board);

                reachingRank = rank - 1;
                reachingFile = file - 2;
                reachPosition(reachingRank, reachingFile, board);

                reachingFile = file + 2;
                reachPosition(reachingRank, reachingFile, board);

                reachingRank = rank + 1;
                reachingFile = file - 2;
                reachPosition(reachingRank, reachingFile, board);

                reachingFile = file + 2;
                reachPosition(reachingRank, reachingFile, board);

                reachingRank = rank + 2;
                reachingFile = file - 1;
                reachPosition(reachingRank, reachingFile, board);

                reachingFile = file + 1;
                reachPosition(reachingRank, reachingFile, board);
            }
            case BISHOP -> {
                for (int i = 1; i < 8; i++) {
                    reachPosition(rank + i, file + i, board);
                    reachPosition(rank + i, file - i, board);
                    reachPosition(rank - i, file + i, board);
                    reachPosition(rank - i, file - i, board);
                }
            }
            case ROOK -> {
                for (int i = 0; i < 8; i++) {
                    reachPosition(rank, i, board);
                    reachPosition(i, file, board);
                }
            }
            case QUEEN -> {
                for (int i = 0; i < 8; i++) {
                    reachPosition(rank + i, file + i, board);
                    reachPosition(rank + i, file - i, board);
                    reachPosition(rank - i, file + i, board);
                    reachPosition(rank - i, file - i, board);
                    reachPosition(rank, i, board);
                    reachPosition(i, file, board);
                }
            }
            case KING -> {
                reachPosition(rank + 1, file + 1, board);
                reachPosition(rank + 1, file - 1, board);
                reachPosition(rank - 1, file + 1, board);
                reachPosition(rank - 1, file - 1, board);
                reachPosition(rank, file + 1, board);
                reachPosition(rank + 1, file, board);
                reachPosition(rank, file - 1, board);
                reachPosition(rank - 1, file, board);
            }
            default -> { }
        }
    }

    // increases the count of pieces which can move to the specified coordinates by 1
    // checks if coordinates are valid
    // does not increase if the coordinates belong to the placement/solution
    private static void reachPosition(int rank, int file, int[][] board){
        if (file >= 0 && file <= 7 && rank >= 0 && rank <= 7 && !isPlacement(rank, file, board)){
            board[rank][file] = board[rank][file] + 1;
        }
    }

    // executes the changes in the game data / board which should be triggered by the mouse click
    // returns true if the game should continue running, false otherwise
    private static boolean handleMouseClick(int mouseX, int mouseY, int boardSize, int squareSize, int offset, int[][] board, boolean[][] hints, int[][] attempt, int[][] placements){
        // clicked board
        if (mouseX > offset && mouseX < boardSize + offset && mouseY > offset && mouseY < boardSize + offset) {
            int file = (int) (((double) (mouseX - offset) / squareSize));
            int rank = (int) (((double) (mouseY - offset) / squareSize));
            if (selectedPiece != Piece.NONE){
                if(isPlacement(rank, file, board)){
                    Piece placedPiece = placedPiece(rank, file, attempt);
                    if (placedPiece.ordinal() >= 1){
                        removePiece(placedPiece, attempt);
                    }
                    if (placedPiece != selectedPiece) {
                        attempt[selectedPiece.ordinal()] = new int[]{rank, file};
                    }
                }
                selectedPiece = Piece.NONE;
            }
            else if (!hints[rank][file]){
                hints[rank][file] = true;
                score -= 2;
            }
            else if (isPlacement(rank, file, board)){
                Piece placedPiece = placedPiece(rank, file, attempt);
                if (placedPiece != Piece.NONE){
                    selectedPiece = placedPiece;
                }
            }
        }

        // clicked sidebar (= unplaced pieces)
        else if (mouseX > boardSize + 3*offset && mouseX < boardSize + squareSize + 3*offset && mouseY > squareSize + 2*offset && mouseY < attempt.length*squareSize + 2*offset){
            int pieceIndex = (int) ((double) (mouseY - 2*offset) / squareSize);
            if (pieceIndex >= 1 && pieceIndex <= 5){
                if (Arrays.equals(attempt[pieceIndex], new int[]{-1, -1})){
                    selectedPiece = Piece.values()[pieceIndex];
                }
            }
        }

        // clicked footer (buttons, WIP)
        else if (mouseX > offset && mouseX < 2*squareSize + offset){
            selectedPiece = Piece.NONE;
            if(mouseY > boardSize + 2*offset && mouseY < boardSize + squareSize + 2*offset){
                if (isAttemptComplete(attempt)){
                    return !submitAttempt(attempt, placements);
                }
            }
            else if (mouseY > boardSize + squareSize + 3*offset && mouseY < boardSize + 2*squareSize + 3*offset){
                return false;
            }
        }

        else {
            selectedPiece = Piece.NONE;
        }

        return true;
    }

    // returns true iff a piece must be placed in the specified coordinates
    private static boolean isPlacement(int rank, int file, int[][] board){
        return board[rank][file] == -1;
    }

    // returns the piece currently placed in the specified coordinates
    private static Piece placedPiece(int rank, int file, int[][] attempt){
        int[] check = {rank, file};
        for (int i = 1; i < attempt.length; i++) {
            if (Arrays.equals(attempt[i], check)){
                return Piece.values()[i];
            }
        }
        return Piece.NONE;
    }

    // removes the piece currently placed in the specified coordinates
    private static void removePiece(Piece piece, int[][] attempt){
        attempt[piece.ordinal()] = new int[]{-1, -1};
    }

    private static boolean isAttemptComplete(int[][] attempt) {
        for (int i = 1; i < attempt.length; i++) {
            if (Arrays.equals(attempt[i], new int[]{-1, -1})) {
                System.out.println("Please place all pieces before submitting");
                return false;
            }
        }
        return true;
    }

    private static boolean submitAttempt(int[][] attempt, int[][]placements){
        boolean winning = true;
        for (int i = 1; i < attempt.length; i++) {
            if (!Arrays.equals(attempt[i], placements[i])) {
                winning = false;
                break;
            }
        }
        if (!winning){
            score -= 5;
            System.out.println("Nice try, but wrong: 5 Points deducted");
        }
        else{
            System.out.println("Congratulations!");
            System.out.println("Your Score: " + score);
        }
        return winning;
    }

    // method for debugging
    private static void printBoard(int[][] board){
        for (int[] row : board)
            System.out.println(Arrays.toString(row));
    }
}