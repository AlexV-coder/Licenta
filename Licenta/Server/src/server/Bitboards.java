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
public class Bitboards {

    long A = 0L, WA = 0L, BA = 0L, WK = 0L, WQ = 0L, WR = 0L, WB = 0L, WN = 0L, WP = 0L, BK = 0L, BQ = 0L, BR = 0L, BB = 0L, BN = 0L, BP = 0L;
    boolean turn = true;
    String lastMove = null; // startSquare, destionationSquare, pieceCapured, promotion, enPassant
    boolean castle[] = {true, true, true, true};
    String enPassant = null;
    long hashKey = 0L;
    char stage = '1';

    Bitboards() {

    }

    Bitboards(Bitboards bitboards) {
        A = bitboards.A;
        WA = bitboards.WA;
        BA = bitboards.BA;
        WK = bitboards.WK;
        WQ = bitboards.WQ;
        WR = bitboards.WR;
        WN = bitboards.WN;
        WB = bitboards.WB;
        WP = bitboards.WP;
        BK = bitboards.BK;
        BQ = bitboards.BQ;
        BR = bitboards.BR;
        BN = bitboards.BN;
        BB = bitboards.BB;
        BP = bitboards.BP;
        turn = bitboards.turn;
        lastMove = bitboards.lastMove;
        castle[0] = bitboards.castle[0];
        castle[1] = bitboards.castle[1];
        castle[2] = bitboards.castle[2];
        castle[3] = bitboards.castle[3];
        enPassant = bitboards.enPassant;
        hashKey = bitboards.hashKey;
    }
}
