# chuesser
chuesser is a simple implementation of the chess puzzle game Black Box Chess.

## Attention! This implementation requires the CodeDraw library
https://github.com/Krassnig/CodeDraw
https://github.com/Krassnig/CodeDraw/blob/master/INSTALL.md
https://krassnig.github.io/CodeDrawJavaDoc/

## Purpose
This is a Java-implementation of the Black Box Chess puzzle game.
It serves educational purposes and is aimed to be used in a programming-101 lecture.
It is therefore intended that the code aims for simplicity rather than efficiency.
Objects as well as advanced OOP concepts should NOT be used, with the exception of arrays and CodeDraw.

## Rules
You start with 100 points.
At the beginning of the game, 5 random squares of the chess-like game board will be designated to place one of 5 different pieces on.
The objective is to find the generated placement.
Click on a piece to mark it, then click on one of the 5 designated squares to place it.
Of course, pieces can also be replaced any number of times.
After you have placed all pieces, you can submit your guess.
A wrong guess will deduct 5 points.

There are 120 possible placements (permutations) in each game.
In order to improve your 1/120 chance in your guess, you can "buy" hints anytime, by clicking one of the non-designated squares.
Clicking a square other than the 5 designated placement squares will reveal a number.
This number indicates how many pieces would be able to move on that square in the generated (= to be guessed) placement.
The pieces have the same move patterns as in standard chess, with the addition that every piece can jump over other pieces! 
Each hint comes with a cost of 2 points.

You win if you find the correct placement before your score drops below 1.

## Legal Notice
This project uses icons created by Cburnett and Francois-Pier which are licensed under the GNU General Public License (GPL).
The Icons were altered by changing the contures from black to grey.
https://commons.wikimedia.org/wiki/User:Cburnett
https://commons.wikimedia.org/wiki/User:Francois-Pier