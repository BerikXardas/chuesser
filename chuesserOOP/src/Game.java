import codedraw.*;
import codedraw.Image;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class Game {

    private static final Image place = CodeDraw.fromFile("src/place.png");
    private Image scaledPlace = CodeDraw.fromFile("src/place.png");
    private CodeDraw canvas;
    private final TextFormat numbersFormat = new TextFormat();
    private final TextFormat textFormat = new TextFormat();
    private int canvasHeight = 800;
    private int canvasWidth = 792;
    private double squareSize = 77;
    private double boardWidth = 616;
    private double boardHeight = 616;
    private double offset = 8;

    private final ChessPiece[] pieces;
    private final ChessPiece[] solution;
    private final int hintCost;
    private final int wrongGuessCost;
    private final boolean[][] hints;
    private final int[][] board;
    private int score = 100;
    private String info = "Adjust the window size by pressing +/UP/-/DOWN keys.\nGood Luck!";
    private ChessPiece selectedPiece;
    private boolean isRunning = true;

    public boolean isRunning() {
        return isRunning && !canvas.isClosed();
    }

    //standard game
    public Game() {
        this.board = new int[8][8];
        this.hints = new boolean[8][8];
        this.pieces = new ChessPiece[5];
        this.solution = new ChessPiece[5];
        this.pieces[0] = new ChessPiece("Knight");
        this.pieces[1] = new ChessPiece("Bishop");
        this.pieces[2] = new ChessPiece("Rook");
        this.pieces[3] = new ChessPiece("Queen");
        this.pieces[4] = new ChessPiece("King");
        this.canvas = new CodeDraw(canvasWidth, canvasHeight);
        this.hintCost = 3;
        this.wrongGuessCost = 7;
        this.scaleImages();
        this.generateSolution();
    }

    //custom screen, pieces, board, starting score and costs
    public Game(int canvasSize, ChessPiece[] pieces, int ranks, int files, int score, int hintCost, int wrongGuessCost) {
        ranks = Math.max(ranks, 5);
        files = Math.max(files, 5);
        ranks = Math.min(ranks, 20);
        files = Math.min(files, 20);
        this.board = new int[ranks][files];
        this.hints = new boolean[ranks][files];
        int pieceCount = Math.min(2 * (ranks - 1), pieces.length);
        this.pieces = new ChessPiece[pieceCount];
        this.solution = new ChessPiece[pieceCount];
        for (int i = 0; i < pieceCount; i++) {
            this.pieces[i] = new ChessPiece(pieces[i]);
        }
        this.adjustMeasurements(canvasSize);
        this.score = score;
        this.hintCost = hintCost;
        this.wrongGuessCost = wrongGuessCost;
        this.generateSolution();
    }

    private void adjustMeasurements(int canvasSize) {
        canvasSize = Math.max(canvasSize, 600);
        int squareBasis = Math.max(board.length, board[0].length);
        squareSize = canvasSize * (squareBasis - 2.) / (squareBasis * squareBasis);
        boardHeight = squareSize * board.length;
        boardWidth = squareSize * board[0].length;
        offset = Math.min(boardWidth, boardHeight) / 100.;
        canvasWidth = (int) (boardWidth + 2 * squareSize + 3 * offset);
        canvasHeight = (int) (boardHeight + 2 * squareSize + 4 * offset);
        canvas = new CodeDraw(canvasWidth, canvasHeight);
        scaleImages();
    }

    private void scaleImages() {
        numbersFormat.setTextOrigin(TextOrigin.CENTER);
        numbersFormat.setFontSize((int) (2 * squareSize / 3));
        textFormat.setTextOrigin(TextOrigin.CENTER_LEFT);
        textFormat.setFontSize((int) (squareSize / 3));
        double scaleX = squareSize / place.getWidth();
        double scaleY = squareSize / place.getHeight();
        double scale = Math.min(scaleX, scaleY);
        scaledPlace = Image.scale(place, scale);
        for (ChessPiece piece : pieces) {
            piece.scaleImage((int) squareSize);
        }
    }

    public EventScanner getEventScanner() {
        return this.canvas.getEventScanner();
    }

    private void generateSolution() {
        for (int i = 0; i < pieces.length; i++) {
            solution[i] = new ChessPiece(pieces[i]);
        }
        Random random = new Random();
        for (ChessPiece piece : solution) {
            int rank = random.nextInt(board.length);
            int file = random.nextInt(board[rank].length);

            while (board[rank][file] == -1) {
                rank = random.nextInt(board.length);
                file = random.nextInt(board[rank].length);
            }

            board[rank][file] = -1;
            hints[rank][file] = true;

            piece.setCoordinates(rank, file);
            piece.simulateMovements(board);
        }
    }

    public void drawGame() {
        canvas.clear(Color.white);

        canvas.setTitle("Black Box Chess - Score: " + score);

        // draw board
        canvas.setColor(Color.black);

        for (int i = 0; i <= board.length; i++) {
            double currentPos = i * squareSize + offset;
            canvas.drawLine(offset, currentPos, boardWidth + offset, currentPos);
        }
        for (int i = 0; i <= board[0].length; i++) {
            double currentPos = i * squareSize + offset;
            canvas.drawLine(currentPos, offset, currentPos, boardHeight + offset);
        }

        canvas.setTextFormat(numbersFormat);
        for (int rank = 0; rank < board.length; rank++) {
            for (int file = 0; file < board[rank].length; file++) {
                if ((rank + file) % 2 == 0) {
                    canvas.setColor(Color.white);
                } else {
                    canvas.setColor(Color.black);
                }
                canvas.fillSquare(file * squareSize + offset, rank * squareSize + offset, squareSize);

                if (hints[rank][file] && board[rank][file] != -1) {
                    canvas.setColor(Color.cyan);
                    canvas.drawText(file * squareSize + offset + squareSize / 2., rank * squareSize + offset + squareSize / 2., String.valueOf(board[rank][file]));
                }
            }
        }

        // sidebar, pieces, placement markers
        canvas.setColor(Color.black);
        canvas.drawRectangle(boardWidth + 2 * offset, offset + squareSize, 2 * squareSize, (board.length - 1) * squareSize);
        for (int i = 0; i < solution.length; i++) {
            boolean placed = false;
            int[] coordinates = solution[i].getCoordinates();
            for (int j = 0; j < pieces.length; j++) {
                if (Arrays.equals(pieces[j].getCoordinates(), coordinates)) {
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                canvas.drawImage(coordinates[1] * squareSize + offset, coordinates[0] * squareSize + offset, scaledPlace);
            }
        }
        int[] notPlaced = {-1, -1};
        for (int i = 0; i < pieces.length; i++) {
            int[] coordinates = pieces[i].getCoordinates();
            if (Arrays.equals(coordinates, notPlaced)) {
                canvas.drawImage((boardWidth + 2 * offset) + ((i / (board.length - 1) * squareSize)), offset + (i % (board.length - 1) + 1) * squareSize, pieces[i].getImage());
            } else {
                canvas.drawImage(coordinates[1] * squareSize + offset, coordinates[0] * squareSize + offset, pieces[i].getImage());
            }
        }

        // selection marker
        if (selectedPiece != null) {
            canvas.setColor(Color.red);
            int[] coordinates = selectedPiece.getCoordinates();
            if (Arrays.equals(coordinates, notPlaced)) {
                int pieceIndex = 0;
                for (int i = 0; i < pieces.length; i++) {
                    if (pieces[i] == selectedPiece) {
                        pieceIndex = i;
                        break;
                    }
                }
                canvas.drawSquare(boardWidth + 2 * offset + (pieceIndex / (board.length - 1)) * squareSize, offset + (pieceIndex % (board.length - 1) + 1) * squareSize, squareSize);
            } else {
                canvas.drawSquare(coordinates[1] * squareSize + offset, coordinates[0] * squareSize + offset, squareSize);
            }
        }

        // display score
        canvas.setColor(Color.black);
        canvas.setTextFormat(textFormat);
        canvas.drawText(boardWidth + 2 * offset, offset + 0.5 * squareSize, "Score: " + score);

        // buttons
        canvas.setColor(Color.YELLOW);
        canvas.fillRectangle(boardWidth + 2 * offset, boardHeight + 2 * offset, 2 * squareSize, squareSize);
        canvas.setColor(Color.black);
        canvas.drawText(boardWidth + 3 * offset, boardHeight + 0.5 * squareSize + 2 * offset, "Submit");

        canvas.setColor(Color.red);
        canvas.fillRectangle(boardWidth + 2 * offset, boardHeight + squareSize + 3 * offset, 2 * squareSize, squareSize);
        canvas.setColor(Color.black);
        canvas.drawText(boardWidth + 3 * offset, boardHeight + 1.5 * squareSize + 3 * offset, "Give up");

        // info
        canvas.drawText(4 * offset, boardHeight + 0.5 * squareSize + 2 * offset, info);

        canvas.show();
        if (!isRunning) {
            canvas.show(3000);
            System.out.println(info);
            canvas.close();
        }
    }

    public void handleMouseClick(int mouseX, int mouseY) {
        info = "";
        // clicked board
        if (mouseX > offset && mouseX < boardWidth + offset && mouseY > offset && mouseY < boardHeight + offset) {
            int file = (int) ((mouseX - offset) / squareSize);
            int rank = (int) ((mouseY - offset) / squareSize);
            int[] coordinates = {rank, file};
            if (selectedPiece != null) {
                if (isPlacement(coordinates)) {
                    ChessPiece placedPiece = placedPiece(coordinates);
                    if (placedPiece != null) {
                        removePiece(placedPiece);
                    }
                    if (placedPiece != selectedPiece) {
                        selectedPiece.setCoordinates(rank, file);
                    }
                }
                selectedPiece = null;
            } else if (isPlacement(coordinates)) {
                ChessPiece placedPiece = placedPiece(coordinates);
                if (placedPiece != null) {
                    info = "You chose a placed piece.\nClick it again to remove it.";
                    selectedPiece = placedPiece;
                }
            } else if (hints[rank][file]) {
                info = "Hint already bought.";
            } else {
                info = "Bought a hint for " + hintCost + " points.";
                hints[rank][file] = true;
                score -= hintCost;
                if (score <= 0) {
                    score = 0;
                    canvas.setTitle("Black Box Chess - Game Over");
                    info = "GAME OVER\nBetter luck next time!";
                    isRunning = false;
                }
            }
        }

        // clicked sidebar (= unplaced pieces)
        else if (mouseX > boardWidth + 2 * offset && mouseX < boardWidth + 2 * squareSize + 2 * offset &&
                mouseY > squareSize + offset && mouseY <= squareSize + boardHeight + offset - squareSize) {
            int pieceIndex = (int) ((mouseY - offset) / squareSize) - 1;
            pieceIndex += (int) ((mouseX - boardWidth - 2 * offset) / squareSize) * (board.length - 1);
            if (pieceIndex >= 0 && pieceIndex < pieces.length && Arrays.equals(pieces[pieceIndex].getCoordinates(), new int[]{-1, -1})) {
                    selectedPiece = pieces[pieceIndex];
            } else {
                selectedPiece = null;
            }
        }

        // clicked submit
        else if (mouseX > boardWidth + 2 * offset && mouseX < boardWidth + 2 * offset + 2 * squareSize &&
                mouseY > boardHeight + 2 * offset && mouseY < boardHeight + squareSize + 2 * offset) {
            selectedPiece = null;
            if (isAttemptComplete()) {
                submitAttempt();
            }
        }

        // clicked give up
        else if (mouseX > boardWidth + 2 * offset && mouseX < boardWidth + 2 * offset + 2 * squareSize &&
                mouseY > boardHeight + squareSize + 3 * offset && mouseY < boardHeight + 2 * squareSize + 3 * offset) {
            selectedPiece = null;
            info = "You surrendered.";
            isRunning = false;
        } else {
            selectedPiece = null;
        }

    }

    public void handleKeystroke(Key key) {
        int canvasSizeChange = 0;
        switch (key) {
            case Key.ESCAPE -> {
                info = "You surrendered.";
                isRunning = false;
            }
            case Key.ENTER -> {
                if (isAttemptComplete()) {
                    submitAttempt();
                }
            }
            case Key.PLUS, Key.UP, Key.ADD -> {
                canvasSizeChange = (int) squareSize * 3;
                info = "Window enlarged";
            }
            case Key.MINUS, Key.DOWN, Key.SUBTRACT -> {
                canvasSizeChange = (int) -squareSize * 3;
                info = "Window compacted";
            }
            default -> {
            }
        }
        if (canvasSizeChange != 0) {
            int newCanvasSize = Math.max(canvasWidth, canvasHeight) + canvasSizeChange;
            int x = canvas.getWindowPositionX();
            int y = canvas.getWindowPositionY();
            canvas.close();
            adjustMeasurements(newCanvasSize);
            canvas.setWindowPositionX(x);
            canvas.setWindowPositionY(y);
        }
    }

    private boolean isPlacement(int[] coordinates) {
        for (ChessPiece piece : solution) {
            if (Arrays.equals(piece.getCoordinates(), coordinates)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttemptComplete() {
        int[] notPlaced = {-1, -1};
        for (ChessPiece piece : pieces) {
            if (Arrays.equals(piece.getCoordinates(), notPlaced)) {
                info = "Please place all pieces before submitting.";
                return false;
            }
        }
        return true;
    }

    private ChessPiece placedPiece(int[] coordinates) {
        for (ChessPiece piece : pieces) {
            if (Arrays.equals(coordinates, piece.getCoordinates())) {
                return piece;
            }
        }
        return null;
    }

    private void removePiece(ChessPiece piece) {
        piece.setCoordinates(-1, -1);
    }

    private void submitAttempt() {
        boolean winning = true;
        for (int i = 0; i < pieces.length; i++) {
            boolean found = false;
            for (int j = 0; j < solution.length; j++) {
                if (pieces[i].equals(solution[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                winning = false;
                break;
            }
        }
        if (!winning) {
            score -= wrongGuessCost;
            info = "Nice try, but wrong!\n" + wrongGuessCost + " Points deducted.";
            if (score <= 0) {
                score = 0;
                canvas.setTitle("Black Box Chess - Game Over");
                info = "GAME OVER\nBetter luck next time!";
                isRunning = false;
            }
        } else {
            info = "Congratulations!\nYour Score: " + score;
            isRunning = false;
        }
    }
}
