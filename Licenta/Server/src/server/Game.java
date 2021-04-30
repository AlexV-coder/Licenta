/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Alex
 */
public class Game {

    Player player1;
    Player player2;
    Bitboards bitboards;
    
    AtomicBoolean gameOn;
    AtomicBoolean turn;
    AtomicBoolean player1SetPieces;
    AtomicBoolean player2SetPieces;
    
    int id;
    
    String winner;
    String player1Color;
    String player2Color;
    String lastMove;
    String mode;
    String board[][];
    String name;

    Game() {
        lastMove = "-----";
        turn = new AtomicBoolean(true);
        gameOn = new AtomicBoolean(false);
        player1SetPieces = new AtomicBoolean(false);
        player2SetPieces = new AtomicBoolean(false);
        bitboards = new Bitboards();
        id = 0;
        player1 = null;
        player2 = null;
        mode = "";
        board = new String[][]{
            {"r", "n", "b", "q", "k", "b", "n", "r"},
            {"p", "p", "p", "p", "p", "p", "p", "p"},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {"P", "P", "P", "P", "P", "P", "P", "P"},
            {"R", "N", "B", "Q", "K", "B", "N", "R"}};
    }

    void resetBoard() {
        board = new String[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = " ";
            }
        }
    }

}
