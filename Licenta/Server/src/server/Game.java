/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author Alex
 */
public class Game {

    Player player1 = null;
    Player player2 = null;
    Bitboards bitboards = new Bitboards();
    boolean gameOn = false;
    char turn = '1';
    int id = 0;
    String winner;
    String player1Color;
    String player2Color;
    String lastMove = "     ";
    String board[][] = {
        {"r", "n", "b", "q", "k", "b", "n", "r"},
        {"p", "p", "p", "p", "p", "p", "p", "p"},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {"P", "P", "P", "P", "P", "P", "P", "P"},
        {"R", "N", "B", "Q", "K", "B", "N", "R"}};

    @Override
    public String toString() {
        return "Game{" + "player1=" + player1 + ", player2=" + player2 + ", id=" + id + '}';
    }

}
