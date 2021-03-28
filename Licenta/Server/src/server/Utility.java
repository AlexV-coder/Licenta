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
public class Utility {

    public static void drawBitboard(long bitBoard) {
        String board[][] = new String[8][8];
        for (int i = 0; i < 64; i++) {
            board[i / 8][i % 8] = "";
        }
        for (int i = 0; i < 64; i++) {
            if (((bitBoard >>> i) & 1) == 1) {
                board[i / 8][i % 8] = "P";
            }
            if ("".equals(board[i / 8][i % 8])) {
                board[i / 8][i % 8] = " ";
            }
        }
        for (int i = 0; i < 8; i++) {
            System.out.println(Arrays.toString(board[i]));
        }
    }

    public static void arrayToBitboards(String[][] board, Bitboards bitboards) {
        String Binary;
        for (int i = 0; i < 64; i++) {
            Binary = "0000000000000000000000000000000000000000000000000000000000000000";
            Binary = Binary.substring(i + 1) + "1" + Binary.substring(0, i);
            switch (board[i / 8][i % 8]) {
                case "P":
                    bitboards.WP += convertStringToBitboard(Binary);
                    break;
                case "N":
                    bitboards.WN += convertStringToBitboard(Binary);
                    break;
                case "B":
                    bitboards.WB += convertStringToBitboard(Binary);
                    break;
                case "R":
                    bitboards.WR += convertStringToBitboard(Binary);
                    break;
                case "Q":
                    bitboards.WQ += convertStringToBitboard(Binary);
                    break;
                case "K":
                    bitboards.WK += convertStringToBitboard(Binary);
                    break;
                case "p":
                    bitboards.BP += convertStringToBitboard(Binary);
                    break;
                case "n":
                    bitboards.BN += convertStringToBitboard(Binary);
                    break;
                case "b":
                    bitboards.BB += convertStringToBitboard(Binary);
                    break;
                case "r":
                    bitboards.BR += convertStringToBitboard(Binary);
                    break;
                case "q":
                    bitboards.BQ += convertStringToBitboard(Binary);
                    break;
                case "k":
                    bitboards.BK += convertStringToBitboard(Binary);
                    break;
            }
        }
        bitboards.WA = bitboards.WK | bitboards.WQ | bitboards.WR | bitboards.WN | bitboards.WB | bitboards.WP;
        bitboards.BA = bitboards.BK | bitboards.BQ | bitboards.BR | bitboards.BN | bitboards.BB | bitboards.BP;
        bitboards.A = bitboards.WA | bitboards.BA;
    }

    public static void bitboardsToArray(String[][] board, Bitboards bitboards) {
        long WK = bitboards.WK;
        long BK = bitboards.BK;
        long WQ = bitboards.WQ;
        long BQ = bitboards.BQ;
        long WR = bitboards.WR;
        long BR = bitboards.BR;
        long WB = bitboards.WB;
        long BB = bitboards.BB;
        long WN = bitboards.WN;
        long BN = bitboards.BN;
        long WP = bitboards.WP;
        long BP = bitboards.BP;
        int index;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = " ";
            }
        }

        while (WK != 0) {
            index = Long.numberOfTrailingZeros(WK);
            board[index / 8][index % 8] = "K";
            WK = WK & (WK - 1);
        }

        while (WQ != 0) {
            index = Long.numberOfTrailingZeros(WQ);
            board[index / 8][index % 8] = "Q";
            WQ = WQ & (WQ - 1);
        }
        while (WR != 0) {
            index = Long.numberOfTrailingZeros(WR);
            board[index / 8][index % 8] = "R";
            WR = WR & (WR - 1);
        }
        while (WB != 0) {
            index = Long.numberOfTrailingZeros(WB);
            board[index / 8][index % 8] = "B";
            WB = WB & (WB - 1);
        }
        while (WN != 0) {
            index = Long.numberOfTrailingZeros(WN);
            board[index / 8][index % 8] = "N";
            WN = WN & (WN - 1);
        }
        while (WP != 0) {
            index = Long.numberOfTrailingZeros(WP);
            board[index / 8][index % 8] = "P";
            WP = WP & (WP - 1);
        }

        while (BK != 0) {
            index = Long.numberOfTrailingZeros(BK);
            board[index / 8][index % 8] = "k";
            BK = BK & (BK - 1);
        }
        while (BQ != 0) {
            index = Long.numberOfTrailingZeros(BQ);
            board[index / 8][index % 8] = "q";
            BQ = BQ & (BQ - 1);
        }
        while (BR != 0) {
            index = Long.numberOfTrailingZeros(BR);
            board[index / 8][index % 8] = "r";
            BR = BR & (BR - 1);
        }
        while (BB != 0) {
            index = Long.numberOfTrailingZeros(BB);
            board[index / 8][index % 8] = "b";
            BB = BB & (BB - 1);
        }
        while (BN != 0) {
            index = Long.numberOfTrailingZeros(BN);
            board[index / 8][index % 8] = "n";
            BN = BN & (BN - 1);
        }
        while (BP != 0) {
            index = Long.numberOfTrailingZeros(BP);
            board[index / 8][index % 8] = "p";
            BP = BP & (BP - 1);
        }
    }

    public static long convertStringToBitboard(String Binary) {
        if (Binary.charAt(0) == '0') {
            return Long.parseLong(Binary, 2);
        } else {
            return Long.parseLong("1" + Binary.substring(2), 2) * 2;
        }
    }

    static String moveToNotation(String move) {
        String moveString = "";
        moveString += "" + (char) (move.charAt(1) + 49);
        moveString += "" + ('8' - move.charAt(0));
        moveString += "" + (char) (move.charAt(3) + 49);
        moveString += "" + ('8' - move.charAt(2));
        moveString += move.charAt(4);
        return moveString;
    }

    static String notationToMove(String move) {
        String moveString = "";
        moveString += String.valueOf(Math.abs(move.charAt(1) - '8'));
        moveString += (move.charAt(0) - 49 - 48);
        moveString += String.valueOf(Math.abs(move.charAt(3) - '8'));
        moveString += (move.charAt(2) - 49 - 48);
        moveString += move.charAt(4);
        return moveString;
    }

    public static long getNrOfMoves(Bitboards bitboards, int depth, int max_depth) {
        String moves;
        if (bitboards.turn) {
            moves = MoveGenerator.generateMovesW(bitboards);
        } else {
            moves = MoveGenerator.generateMovesB(bitboards);
        }
        if (depth == max_depth) {
            return moves.length() / 5;
        }
        int nr = 0;
        int movesLength = moves.length();
        for (int i = 0; i < movesLength; i += 5) {
            Bitboards aux = new Bitboards(bitboards);
            String move = moves.substring(i, i + 5);
            MoveGenerator.makeMove(aux, move);
            long n = getNrOfMoves(aux, depth + 1, max_depth);
            if (depth == 1) {
                System.out.println(Utility.moveToNotation(move) + ": " + n);
            }
            nr += n;
        }
        return nr;
    }

    static void transpose(double[] val1, double[] val2) {
        for (int i = 0; i < 64; i++) {
            val2[i] = val1[63 - i];
        }
    }
}
