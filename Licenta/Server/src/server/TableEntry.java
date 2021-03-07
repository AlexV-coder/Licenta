/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.Arrays;

/**
 *
 * @author Alex
 */
public class TableEntry {

    public boolean turn;
    public int depth;
    public String bestMove = "";
    public int score;
    public String enPassant = null;
    public boolean[] castle = {true, true, true, true};
    public int pruneType;
    public int age = 0;

    TableEntry(boolean t, int d, String b, int s, String enP, boolean[] cstl, int p) {
        turn = t;
        depth = d;
        bestMove = b;
        score = s;
        enPassant = enP;
        castle[0] = cstl[0];
        castle[1] = cstl[1];
        castle[2] = cstl[2];
        castle[3] = cstl[3];
        pruneType = p;
    }

    @Override
    public String toString() {
        return "TableEntry{" + "turn=" + turn + ", depth=" + depth + ", bestMove=" + bestMove + ", score=" + score + ", enPassant=" + enPassant + ", castle=" + Arrays.toString(castle) + '}';
    }
}
