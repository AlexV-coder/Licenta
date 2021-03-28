/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import static server.MoveGenerator.attackerBoardB;
import static server.MoveGenerator.attackerBoardW;
import static server.MoveGenerator.generateMovesB;
import static server.MoveGenerator.generateMovesW;
import static server.MoveGenerator.makeMove;
import static server.Utility.transpose;

/**
 *
 * @author Alex
 */
public class PC {
    /**
     * @param args the command line arguments
     */
     double pawnB[] = {
        0, 0, 0, 0, 0, 0, 0, 0,
        5, 10, 10, -20, -20, 10, 10, 5,
        5, -5, -10, 0, 0, -10, -5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, 5, 10, 25, 25, 10, 5, 5,
        10, 10, 20, 30, 30, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
        0, 0, 0, 0, 0, 0, 0, 0};

     double[] knightB = {
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50};

     double[] bishopB = {
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10, 5, 0, 0, 0, 0, 5, -10,
        -10, 10, 10, 10, 10, 10, 10, -10,
        -10, 0, 10, 10, 10, 10, 0, -10,
        -10, 5, 5, 10, 10, 5, 5, -10,
        -10, 0, 5, 10, 10, 5, 0, -10,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -20, -10, -10, -10, -10, -10, -10, -20};

     double[] rookB = {
        0, 0, 0, 5, 5, 0, 0, 0,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        5, 10, 10, 10, 10, 10, 10, 5,
        0, 0, 0, 0, 0, 0, 0, 0};

     double[] queenB = {
        -20, -10, -10, -5, -5, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 5, 5, 5, 5, 5, 0, -10,
        0, 0, 5, 5, 5, 5, 0, -5,
        -5, 0, 5, 5, 5, 5, 0, -5,
        -10, 0, 5, 5, 5, 5, 0, -10,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -20, -10, -10, -5, -5, -10, -10, -20};

     double[] kingB = {
        20, 30, 10, 0, 0, 10, 30, 20,
        20, 20, 0, 0, 0, 0, 20, 20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30};

     double pawnW[] = new double[64];
     double bishopW[] = new double[64];
     double knightW[] = new double[64];
     double rookW[] = new double[64];
     double queenW[] = new double[64];
     double kingW[] = new double[64];
     String chessBoard[][] = {
        {"r", "n", "b", "q", "k", "b", "n", "r"},
        {"p", "p", "p", "p", "p", "p", "p", "p"},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {" ", " ", " ", " ", " ", " ", " ", " "},
        {"P", "P", "P", "P", "P", "P", "P", "P"},
        {"R", "N", "B", "Q", "K", "B", "N", "R"}};

     int nps = 0;
     int R = 2;
     int maxDepth = 6;
     int side = 1;
     Bitboards bitboards;
     Map<Long, TableEntry> transpositionTable = new HashMap<>();
     String[][] killerMoves = new String[9][2];
     String compMove;

    PC (Bitboards bitboards, int s, int depth) {
        this.bitboards = new Bitboards(bitboards);
        maxDepth = depth;
        side = s;
        transpose(pawnB, pawnW);
        transpose(bishopB, bishopW);
        transpose(knightB, knightW);
        transpose(rookB, rookW);
        transpose(queenB, queenW);
        transpose(kingB, kingW);
        hashBoard(bitboards);
        Random gen = new Random();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 64; j++) {
                MoveGenerator.randomIntegers[i][j] = gen.nextLong();
            }
        }
    }
    public String move() {
        killerMoves = new String[9][2];
        transpositionTable = new HashMap<>();
        //iterative deepening
        for (int i = 2; i <= maxDepth; i++) {
            negaMax(bitboards, i, -999999, 999999, side, false);
        }
        return compMove;
    }

    private  int calculateScore(Bitboards bitboards) {
        int whiteScore = 0;
        int blackScore = 0;
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

        index = Long.numberOfTrailingZeros(WK);
        whiteScore += 2000 + kingW[index];

        while (WQ != 0) {
            index = Long.numberOfTrailingZeros(WQ);
            whiteScore += 900 + queenW[index];
            WQ = WQ & (WQ - 1);
        }
        while (WR != 0) {
            index = Long.numberOfTrailingZeros(WR);
            whiteScore += 500 + rookW[index];
            WR = WR & (WR - 1);
        }
        while (WB != 0) {
            index = Long.numberOfTrailingZeros(WB);
            whiteScore += 330 + bishopW[index];
            WB = WB & (WB - 1);
        }
        while (WN != 0) {
            index = Long.numberOfTrailingZeros(WN);
            whiteScore += 320 + knightW[index];
            WN = WN & (WN - 1);
        }
        while (WP != 0) {
            index = Long.numberOfTrailingZeros(WP);
            whiteScore += 100 + pawnW[index];
            WP = WP & (WP - 1);
        }

        index = Long.numberOfTrailingZeros(BK);
        blackScore += 2000 + kingB[index];
        while (BQ != 0) {
            index = Long.numberOfTrailingZeros(BQ);
            blackScore += 900 + queenB[index];
            BQ = BQ & (BQ - 1);
        }
        while (BR != 0) {
            index = Long.numberOfTrailingZeros(BR);
            blackScore += 500 + rookB[index];
            BR = BR & (BR - 1);
        }
        while (BB != 0) {
            index = Long.numberOfTrailingZeros(BB);
            blackScore += 330 + bishopB[index];
            BB = BB & (BB - 1);
        }
        while (BN != 0) {
            index = Long.numberOfTrailingZeros(BN);
            blackScore += 320 + knightB[index];
            BN = BN & (BN - 1);
        }
        while (BP != 0) {
            index = Long.numberOfTrailingZeros(BP);
            blackScore += 100 + pawnB[index];
            BP = BP & (BP - 1);
        }
        nps++;
        return blackScore - whiteScore;
    }

    private  void hashBoard(Bitboards bitboards) {
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
        long hashKey = bitboards.hashKey;
        while (WK != 0) {
            int index = Long.numberOfTrailingZeros(WK);
            hashKey ^= MoveGenerator.randomIntegers[0][index];
            WK = WK & (WK - 1);
        }
        while (WQ != 0) {
            int index = Long.numberOfTrailingZeros(WQ);
            hashKey ^= MoveGenerator.randomIntegers[1][index];
            WQ = WQ & (WQ - 1);
        }
        while (WR != 0) {
            int index = Long.numberOfTrailingZeros(WR);
            hashKey ^= MoveGenerator.randomIntegers[2][index];
            WR = WR & (WR - 1);
        }
        while (WB != 0) {
            int index = Long.numberOfTrailingZeros(WB);
            hashKey ^= MoveGenerator.randomIntegers[3][index];
            WB = WB & (WB - 1);
        }
        while (WN != 0) {
            int index = Long.numberOfTrailingZeros(WN);
            hashKey ^= MoveGenerator.randomIntegers[4][index];
            WN = WN & (WN - 1);
        }
        while (WP != 0) {
            int index = Long.numberOfTrailingZeros(WP);
            hashKey ^= MoveGenerator.randomIntegers[5][index];
            WP = WP & (WP - 1);
        }
        while (BK != 0) {
            int index = Long.numberOfTrailingZeros(BK);
            hashKey ^= MoveGenerator.randomIntegers[6][index];
            BK = BK & (BK - 1);
        }
        while (BQ != 0) {
            int index = Long.numberOfTrailingZeros(BQ);
            hashKey ^= MoveGenerator.randomIntegers[7][index];
            BQ = BQ & (BQ - 1);
        }
        while (BR != 0) {
            int index = Long.numberOfTrailingZeros(BR);
            hashKey ^= MoveGenerator.randomIntegers[8][index];
            BR = BR & (BR - 1);
        }
        while (BB != 0) {
            int index = Long.numberOfTrailingZeros(BB);
            hashKey ^= MoveGenerator.randomIntegers[9][index];
            BB = BB & (BB - 1);
        }
        while (BN != 0) {
            int index = Long.numberOfTrailingZeros(BN);
            hashKey ^= MoveGenerator.randomIntegers[10][index];
            BN = BN & (BN - 1);
        }
        while (BP != 0) {
            int index = Long.numberOfTrailingZeros(BP);
            hashKey ^= MoveGenerator.randomIntegers[11][index];
            BP = BP & (BP - 1);
        }
        bitboards.hashKey = hashKey;
    }

    private  double negaMax(Bitboards bitboards, int depth, double alpha, double beta, int side, boolean inNullMove) {
        double alphaOrig = alpha;
        String moveFromTT = "";
        TableEntry position = transpositionTable.get(bitboards.hashKey);
        if (position != null && position.turn == bitboards.turn && (position.enPassant == null ? bitboards.enPassant == null : position.enPassant.equals(bitboards.enPassant)) && Arrays.equals(bitboards.castle, position.castle)) {
            moveFromTT = position.bestMove;
            if (position.depth >= depth) {
                switch (position.pruneType) {
                    case 0:
                        return position.score;
                    case -1:
                        alpha = Math.max(alpha, position.score);
                        break;
                    default:
                        beta = Math.min(beta, position.score);
                        break;
                }
                if (alpha >= beta) {
                    return position.score;
                }
            }
        }

        if (depth == 0) {
            return side * calculateScore(bitboards);
            //return quiesce(bitboards, alpha, beta, side);
        }

        String moves;
        String bestMove = "";

        if (bitboards.turn) {
            moves = generateMovesW(bitboards);
        } else {
            moves = generateMovesB(bitboards);
        }

        if (moves.length() == 0) {
            if (bitboards.turn) {
                if (attackerBoardW(bitboards, bitboards.WK) == 0) {
                    return 0;
                } else {
                    return -300000 - depth;
                }
            } else {
                if (attackerBoardB(bitboards, bitboards.BK) == 0) {
                    return 0;
                } else {
                    return -300000 - depth;
                }
            }
        }

        //null move
        if (!inNullMove && ((bitboards.turn && attackerBoardW(bitboards, bitboards.WK) == 0) || (!bitboards.turn && attackerBoardB(bitboards, bitboards.BK) == 0)) && depth >= R + 1) {
            Bitboards child = new Bitboards(bitboards);
            child.turn = !child.turn;
            double val = -negaMax(child, depth - R - 1, -beta, -beta + 1, -side, true);
            if (val >= beta) {
                return val;
            }
        }
        //move ordering
        String captures = "";
        String pawnMoves = "";
        String killers = "";
        String others = "";
        for (int i = 0; i < moves.length(); i += 5) {
            String mv = moves.substring(i, i + 5);
            if ((bitboards.A & 1L << (Character.getNumericValue(mv.charAt(2)) * 8 + Character.getNumericValue(mv.charAt(3)))) != 0) {
                captures += mv;
            } else if ((mv.equals(killerMoves[depth][0]) || mv.equals(killerMoves[depth][1]))) {
                killers += mv;
            } else if (((bitboards.WP | bitboards.BP) & 1L << (Character.getNumericValue(mv.charAt(0)) * 8 + Character.getNumericValue(mv.charAt(1)))) != 0) {
                pawnMoves += mv;
            } else {
                others += mv;
            }
        }
        moves = captures + killers + pawnMoves + others;
        if (!"".equals(moveFromTT)) {
            moves = moves.replace(moveFromTT, "");
            moves = moveFromTT + moves;
        }
        double score = -9999999;
        for (int i = 0; i < moves.length(); i += 5) {
            String mv = moves.substring(i, i + 5);
            Bitboards child = new Bitboards(bitboards);
            makeMove(child, mv);
            double val = -negaMax(child, depth - 1, -beta, -alpha, -side, inNullMove);
            if (val > score) {
                score = val;
                bestMove = mv;
            }
            alpha = Math.max(alpha, score);
            if (alpha >= beta) {
                killerMoves[depth][1] = killerMoves[depth][0];
                killerMoves[depth][0] = bestMove;
                break;
            }
        }
        int flag = 0;
        if (score <= alphaOrig) {
            flag = 1;
        } else if (score >= beta) {
            flag = -1;
        }
        transpositionTable.put(bitboards.hashKey, new TableEntry(bitboards.turn, depth, bestMove, (int) score, bitboards.enPassant, bitboards.castle, flag));
        if (depth == maxDepth) {
            compMove = bestMove;
            makeMove(bitboards, bestMove);
        }
        return score;
    }

    private  double quiesce(Bitboards bitboards, double alpha, double beta, int side) {

        int stand_pat = calculateScore(bitboards) * side;
        if (stand_pat >= beta) {
            return beta;
        }
        if (alpha < stand_pat) {
            alpha = stand_pat;
        }

        String moves;
        if (bitboards.turn) {
            moves = generateMovesW(bitboards);
        } else {
            moves = generateMovesB(bitboards);
        }

        if (moves.length() == 0) {
            if (bitboards.turn) {
                if (attackerBoardW(bitboards, bitboards.WK) == 0) {
                    return 0;
                } else {
                    return -300000;
                }
            } else {
                if (attackerBoardB(bitboards, bitboards.BK) == 0) {
                    return 0;
                } else {
                    return 300000;
                }
            }
        }

        String captures = "";
        for (int i = 0; i < moves.length(); i += 5) {
            String move = moves.substring(i, i + 5);
            if ((bitboards.A & 1L << (Character.getNumericValue(move.charAt(2)) * 8 + Character.getNumericValue(move.charAt(3)))) != 0) {
                captures += moves.substring(i, i + 5);
            }
        }
        double score;
        for (int i = 0; i < captures.length(); i += 5) {
            Bitboards copy = new Bitboards(bitboards);
            makeMove(copy, captures.substring(i, i + 5));
            score = -quiesce(copy, -beta, -alpha, -side);

            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;
    }
}
