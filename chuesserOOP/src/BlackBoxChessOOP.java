import codedraw.*;
import codedraw.Image;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class BlackBoxChessOOP {

    public static void main(String[] args) {
        //Game game1 = new Game();

        Image unicorn = CodeDraw.fromFile("src/knight2.png");
        ChessPiece[] pieces = new ChessPiece[7];
        pieces[0] = new ChessPiece("King1");
        pieces[1] = new ChessPiece("KnightRider");
        pieces[2] = new ChessPiece("ROOK");
        pieces[3] = new ChessPiece("King2");
        pieces[4] = new ChessPiece("Unicorn", 0, 3, new int[][]{{-2, 0}, {0, -2}, {2, 0}, {0, 2}}, unicorn);
        pieces[5] = new ChessPiece("QueenVictoria");
        pieces[6] = new ChessPiece("BishopOfCologne");
        Game game2 = new Game(700, pieces, 6, 9, 77, 2, 7);

        Game game = game2;

        game.drawGame();
        EventScanner eventScanner;

        while (game.isRunning()) {
            eventScanner = game.getEventScanner();
            for (var event : eventScanner) {
                switch (event) {
                    case MouseClickEvent click -> {
                        int mouseX = click.getX();
                        int mouseY = click.getY();
                        game.handleMouseClick(mouseX, mouseY);
                        game.drawGame();
                    }
                    case KeyPressEvent keystroke -> {
                        game.handleKeystroke(keystroke.getKey());
                        game.drawGame();
                    }
                    default -> {
                    }
                }
            }
        }
    }

}