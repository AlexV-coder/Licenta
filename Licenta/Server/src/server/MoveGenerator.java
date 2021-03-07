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

/* dreata -> stanga, jos -> sus */
 /*>>>ma apropii de stanga sus (A8) */
 /* << ma apropii de dreapta jos (H1) */
public class MoveGenerator {

    static long FILE_A = 72340172838076673L;
    static long FILE_H = -9187201950435737472L;
    static long FILE_AB = 217020518514230019L;
    static long FILE_GH = -4557430888798830400L;
    static long RANK_1 = -72057594037927936L;
    static long RANK_2 = 71776119061217280L;
    static long RANK_4 = 1095216660480L;
    static long RANK_5 = 4278190080L;
    static long RANK_7 = 65280L;
    static long RANK_8 = 255L;
    static long CENTRE = 103481868288L;
    static long EXTENDED_CENTRE = 66229406269440L;
    static long KING_SIDE = -1085102592571150096L;
    static long QUEEN_SIDE = 1085102592571150095L;
    static long randomIntegers[][] = new long[12][64];

    static long rankMasks[]
            =/*top to bottom*/ {
                0xFFL, 0xFF00L, 0xFF0000L, 0xFF000000L, 0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L, 0xFF00000000000000L
            };
    static long fileMasks[]
            =/*file A to file H*/ {
                0x101010101010101L, 0x202020202020202L, 0x404040404040404L, 0x808080808080808L,
                0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L
            };

    static long diagonalMasks[]
            =/*from top left to bottom right*/ {
                0x1L, 0x102L, 0x10204L, 0x1020408L, 0x102040810L, 0x10204081020L, 0x1020408102040L,
                0x102040810204080L, 0x204081020408000L, 0x408102040800000L, 0x810204080000000L,
                0x1020408000000000L, 0x2040800000000000L, 0x4080000000000000L, 0x8000000000000000L
            };
    static long antiDiagonalMasks[]
            =/*from top right to bottom left*/ {
                0x80L, 0x8040L, 0x804020L, 0x80402010L, 0x8040201008L, 0x804020100804L, 0x80402010080402L,
                0x8040201008040201L, 0x4020100804020100L, 0x2010080402010000L, 0x1008040201000000L,
                0x804020100000000L, 0x402010000000000L, 0x201000000000000L, 0x100000000000000L
            };

    static String generateMovesW(Bitboards bitboards) {
        String moves = "";
        long A = bitboards.A;
        long pinned = getPinnedPiecesW(bitboards, bitboards.WK);
        long pinnedN = pinned & bitboards.WN;
        long pinnedP = pinned & bitboards.WP;
        long pinnedQ = pinned & bitboards.WQ;
        long pinnedR = pinned & bitboards.WR;
        long pinnedB = pinned & bitboards.WB;
        bitboards.WN &= ~pinned;
        bitboards.WP &= ~pinned;
        bitboards.WQ &= ~pinned;
        bitboards.WR &= ~pinned;
        bitboards.WB &= ~pinned;
        long checks = attackerBoardW(bitboards, bitboards.WK);
        if (checks == 0) {
            while (pinned != 0) {
                int index = Long.numberOfTrailingZeros(pinned);
                long onePinned = 1L << index;
                long BQ = bitboards.BQ;
                long BR = bitboards.BR;
                long BB = bitboards.BB;
                long legalRay = 0L;
                boolean legalRayType = false; //false - straight, true - diagonal
                long pinner = 0L;
                while (BQ != 0) {
                    int indexQueen = Long.numberOfTrailingZeros(BQ);
                    long queen = 1L << indexQueen;
                    long pushMaskQueen = getPushMaskLine(queen, bitboards.WK);
                    if ((pushMaskQueen & onePinned) != 0) {
                        legalRay = pushMaskQueen;
                        pinner = queen;
                        legalRayType = false;
                        break;
                    }
                    pushMaskQueen = getPushMaskDiagonal(queen, bitboards.WK);
                    if ((pushMaskQueen & onePinned) != 0) {
                        legalRay = pushMaskQueen;
                        legalRayType = true;
                        pinner = queen;
                        break;
                    }
                    BQ = BQ & (BQ - 1);
                }
                while (BR != 0) {
                    int indexRook = Long.numberOfTrailingZeros(BR);
                    long rook = 1L << indexRook;
                    long pushMaskRook = getPushMaskLine(rook, bitboards.WK);
                    if ((pushMaskRook & onePinned) != 0) {
                        legalRay = pushMaskRook;
                        pinner = rook;
                        legalRayType = false;
                        break;
                    }
                    BR = BR & (BR - 1);
                }
                while (BB != 0) {
                    int indexBishop = Long.numberOfTrailingZeros(BB);
                    long bishop = 1L << indexBishop;
                    long pushMaskBishop = getPushMaskDiagonal(bishop, bitboards.WK);
                    if ((pushMaskBishop & onePinned) != 0) {
                        legalRay = pushMaskBishop;
                        legalRayType = true;
                        pinner = bishop;
                        break;
                    }
                    BB = BB & (BB - 1);
                }
                if (legalRayType == false) {
                    if ((onePinned & pinnedR) != 0) { //pinned is a rook
                        long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(onePinned)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * onePinned))) & rankMasks[index / 8];
                        long vertical = (((A & fileMasks[index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(onePinned)))) & fileMasks[index % 8];
                        long moveBitboard = (horizontal | vertical) & ~bitboards.WA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedQ) != 0) { //pinned is a queen
                        long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(onePinned)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * onePinned))) & rankMasks[index / 8];
                        long vertical = (((A & fileMasks[index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(onePinned)))) & fileMasks[index % 8];
                        // long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(onePinned)))) & diagonalMasks[index / 8 + index % 8];
                        // long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(onePinned)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
                        long moveBitboard = (horizontal | vertical) & ~bitboards.WA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedP) != 0) { //pinned is a pawn
                        long moveBitboard = ((((onePinned & RANK_2) >>> 8 & ~A) >>> 8) | (onePinned >>> 8)) & ~A & legalRay;
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    }
                } else {
                    if ((onePinned & pinnedB) != 0) { //pinned is a bishop
                        long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(onePinned)))) & diagonalMasks[index / 8 + index % 8];
                        long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(onePinned)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
                        long moveBitboard = (firstDiagonal | secondDiagonal) & ~bitboards.WA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedQ) != 0) { //pinned is a queen
                        // long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(onePinned)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * onePinned))) & rankMasks[index / 8];
                        // long vertical = (((A & fileMasks[index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(onePinned)))) & fileMasks[index % 8];
                        long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(onePinned)))) & diagonalMasks[index / 8 + index % 8];
                        long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(onePinned)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
                        long moveBitboard = (firstDiagonal | secondDiagonal) & ~bitboards.WA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedP) != 0) { //pinned is a pawn
                        long moveBitboard = ((onePinned >>> 7 | onePinned >>> 9) & bitboards.BA & (legalRay | pinner));
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            if ((moveBitboard & RANK_8) == 0) {
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            } else {
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "Q");
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "R");
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "N");
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "B");
                            }
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    }
                }
                pinned = pinned & (pinned - 1);
            }
            moves = moves.concat(generatePawnMovesW(bitboards));
            moves = moves.concat(generateKnightMovesW(bitboards));
            moves = moves.concat(generateRookMovesW(bitboards));
            moves = moves.concat(generateBishopMovesW(bitboards));
            moves = moves.concat(generateQueenMovesW(bitboards));
            moves = moves.concat(generateKingMovesW(bitboards));
        } else {
            moves = moves.concat(generateKingMovesW(bitboards));
            int attackers = Long.bitCount(checks);
            //single check & double check
            if (attackers == 1) {
                int attackerIndex = Long.numberOfTrailingZeros(checks);
                long captureCheck = attackerBoardB(bitboards, checks);
                long captureCheckWithPromotion = captureCheck & bitboards.WP & RANK_7;
                captureCheck &= ~(bitboards.WP & RANK_7);
                //generate moves that capture the piece giving check
                while (captureCheck != 0) {
                    int index = Long.numberOfTrailingZeros(captureCheck);
                    if (bitboards.enPassant != null && bitboards.enPassant.charAt(0) - 48 == attackerIndex / 8 && bitboards.enPassant.charAt(1) - 48 == attackerIndex % 8 && (1L << index & bitboards.WP) != 0 && ((captureCheck >>> 1 & checks) != 0 || (captureCheck << 1 & checks) != 0)) {
                        moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8 - 1) + (attackerIndex % 8) + " ");
                    } else {
                        moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + " ");
                    }
                    captureCheck = captureCheck & (captureCheck - 1);
                }
                while (captureCheckWithPromotion != 0) {
                    int index = Long.numberOfTrailingZeros(captureCheckWithPromotion);
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "Q");
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "R");
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "B");
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "N");
                    captureCheckWithPromotion = captureCheckWithPromotion & (captureCheckWithPromotion - 1);
                }
                //if checked by a sliding piece
                if ((checks & (bitboards.BQ | bitboards.BR | bitboards.BB)) != 0) {
                    long pushMask = 0L;
                    if ((checks & bitboards.BQ) != 0) {
                        pushMask = getPushMaskLine(checks, bitboards.WK) | getPushMaskDiagonal(checks, bitboards.WK);
                    } else if ((checks & bitboards.BB) != 0) {
                        pushMask = getPushMaskDiagonal(checks, bitboards.WK);
                    } else if ((checks & bitboards.BR) != 0) {
                        pushMask = getPushMaskLine(checks, bitboards.WK);
                    }
                    while (pushMask != 0) {
                        int index = Long.numberOfTrailingZeros(pushMask);
                        long s = 1L << index;
                        long blockCheck = blockerBoardB(bitboards, s);
                        while (blockCheck != 0) {
                            int index2 = Long.numberOfTrailingZeros(blockCheck);
                            moves += ("" + (index2 / 8) + (index2 % 8) + (index / 8) + (index % 8) + " ");
                            blockCheck = blockCheck & (blockCheck - 1);
                        }
                        pushMask = pushMask & (pushMask - 1);
                    }
                }
            }
        }
        bitboards.WN |= pinnedN;
        bitboards.WQ |= pinnedQ;
        bitboards.WB |= pinnedB;
        bitboards.WR |= pinnedR;
        bitboards.WP |= pinnedP;
        return moves;
    }

    static String generateMovesB(Bitboards bitboards) {
        String moves = "";
        long A = bitboards.A;
        long pinned = getPinnedPiecesB(bitboards, bitboards.BK);
        long pinnedN = pinned & bitboards.BN;
        long pinnedP = pinned & bitboards.BP;
        long pinnedQ = pinned & bitboards.BQ;
        long pinnedR = pinned & bitboards.BR;
        long pinnedB = pinned & bitboards.BB;
        bitboards.BN &= ~pinned;
        bitboards.BP &= ~pinned;
        bitboards.BQ &= ~pinned;
        bitboards.BR &= ~pinned;
        bitboards.BB &= ~pinned;
        // bitboards.WA &= ~pinned;
        // bitboards.A &= ~pinned;
        long checks = attackerBoardB(bitboards, bitboards.BK);
        if (checks == 0) {
            while (pinned != 0) {
                int index = Long.numberOfTrailingZeros(pinned);
                long onePinned = 1L << index;
                long WQ = bitboards.WQ;
                long WR = bitboards.WR;
                long WB = bitboards.WB;
                long legalRay = 0L;
                boolean legalRayType = false; //false - straight, true - diagonal
                long pinner = 0L;
                while (WQ != 0) {
                    int indexQueen = Long.numberOfTrailingZeros(WQ);
                    long queen = 1L << indexQueen;
                    long pushMaskQueen = getPushMaskLine(queen, bitboards.BK);
                    if ((pushMaskQueen & onePinned) != 0) {
                        legalRay = pushMaskQueen;
                        pinner = queen;
                        legalRayType = false;
                        break;
                    }
                    pushMaskQueen = getPushMaskDiagonal(queen, bitboards.BK);
                    if ((pushMaskQueen & onePinned) != 0) {
                        legalRay = pushMaskQueen;
                        legalRayType = true;
                        pinner = queen;
                        break;
                    }
                    WQ = WQ & (WQ - 1);
                }
                while (WR != 0) {
                    int indexRook = Long.numberOfTrailingZeros(WR);
                    long rook = 1L << indexRook;
                    long pushMaskRook = getPushMaskLine(rook, bitboards.BK);
                    if ((pushMaskRook & onePinned) != 0) {
                        legalRay = pushMaskRook;
                        pinner = rook;
                        legalRayType = false;
                        break;
                    }
                    WR = WR & (WR - 1);
                }
                while (WB != 0) {
                    int indexBishop = Long.numberOfTrailingZeros(WB);
                    long bishop = 1L << indexBishop;
                    long pushMaskBishop = getPushMaskDiagonal(bishop, bitboards.BK);
                    if ((pushMaskBishop & onePinned) != 0) {
                        legalRay = pushMaskBishop;
                        legalRayType = true;
                        pinner = bishop;
                        break;
                    }
                    WB = WB & (WB - 1);
                }
                if (legalRayType == false) {
                    if ((onePinned & pinnedR) != 0) { //pinned is a rook
                        long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(onePinned)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * onePinned))) & rankMasks[index / 8];
                        long vertical = (((A & fileMasks[index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(onePinned)))) & fileMasks[index % 8];
                        long moveBitboard = (horizontal | vertical) & ~bitboards.BA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedQ) != 0) { //pinned is a queen
                        long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(onePinned)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * onePinned))) & rankMasks[index / 8];
                        long vertical = (((A & fileMasks[index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(onePinned)))) & fileMasks[index % 8];
                        //  long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(onePinned)))) & diagonalMasks[index / 8 + index % 8];
                        // long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(onePinned)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
                        long moveBitboard = (horizontal | vertical) & ~bitboards.BA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedP) != 0) { //pinned is a pawn
                        long moveBitboard = ((((onePinned & RANK_7) << 8 & ~A) << 8) | (onePinned << 8)) & ~A & legalRay;
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    }
                } else {
                    if ((onePinned & pinnedB) != 0) { //pinned is a bishop
                        long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(onePinned)))) & diagonalMasks[index / 8 + index % 8];
                        long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(onePinned)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
                        long moveBitboard = (firstDiagonal | secondDiagonal) & ~bitboards.BA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedQ) != 0) { //pinned is a queen
                        //  long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(onePinned)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * onePinned))) & rankMasks[index / 8];
                        // long vertical = (((A & fileMasks[index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(onePinned)))) & fileMasks[index % 8];
                        long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(onePinned)))) & diagonalMasks[index / 8 + index % 8];
                        long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * onePinned)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(onePinned)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
                        long moveBitboard = (firstDiagonal | secondDiagonal) & ~bitboards.BA & (legalRay | pinner);
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    } else if ((onePinned & pinnedP) != 0) { //pinned is a pawn
                        long moveBitboard = ((onePinned << 7 | onePinned << 9) & bitboards.WA & (legalRay | pinner));
                        while (moveBitboard != 0) {
                            int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                            if ((moveBitboard & RANK_1) == 0) {
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                            } else {
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "q");
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "r");
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "n");
                                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + "b");
                            }
                            moveBitboard = moveBitboard & (moveBitboard - 1);
                        }
                    }
                }
                pinned = pinned & (pinned - 1);
            }
            moves = moves.concat(generatePawnMovesB(bitboards));
            moves = moves.concat(generateKnightMovesB(bitboards));
            moves = moves.concat(generateRookMovesB(bitboards));
            moves = moves.concat(generateBishopMovesB(bitboards));
            moves = moves.concat(generateQueenMovesB(bitboards));
            moves = moves.concat(generateKingMovesB(bitboards));
        } else {
            moves = moves.concat(generateKingMovesB(bitboards));
            int attackers = Long.bitCount(checks);
            //single check & double check
            if (attackers == 1) {
                int attackerIndex = Long.numberOfTrailingZeros(checks);
                long captureCheck = attackerBoardW(bitboards, checks);
                long captureCheckWithPromotion = captureCheck & bitboards.BP & RANK_2;
                captureCheck &= ~(bitboards.BP & RANK_2);
                //generate moves that capture the piece giving check
                while (captureCheck != 0) {
                    int index = Long.numberOfTrailingZeros(captureCheck);
                    if (bitboards.enPassant != null && bitboards.enPassant.charAt(0) - 48 == attackerIndex / 8 && bitboards.enPassant.charAt(1) - 48 == attackerIndex % 8 && (1L << index & bitboards.BP) != 0 && ((captureCheck >>> 1 & checks) != 0 || (captureCheck << 1 & checks) != 0)) {
                        moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8 + 1) + (attackerIndex % 8) + " ");
                    } else {
                        moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + " ");
                    }
                    captureCheck = captureCheck & (captureCheck - 1);
                }
                while (captureCheckWithPromotion != 0) {
                    int index = Long.numberOfTrailingZeros(captureCheckWithPromotion);
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "q");
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "r");
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "b");
                    moves += ("" + (index / 8) + (index % 8) + (attackerIndex / 8) + (attackerIndex % 8) + "n");
                    captureCheckWithPromotion = captureCheckWithPromotion & (captureCheckWithPromotion - 1);
                }
                //if checked by a sliding piece
                if ((checks & (bitboards.WQ | bitboards.WR | bitboards.WB)) != 0) {
                    long pushMask = 0L;
                    if ((checks & bitboards.WQ) != 0) {
                        pushMask = getPushMaskLine(checks, bitboards.BK) | getPushMaskDiagonal(checks, bitboards.BK);
                    } else if ((checks & bitboards.WB) != 0) {
                        pushMask = getPushMaskDiagonal(checks, bitboards.BK);
                    } else if ((checks & bitboards.WR) != 0) {
                        pushMask = getPushMaskLine(checks, bitboards.BK);
                    }
                    while (pushMask != 0) {
                        int index = Long.numberOfTrailingZeros(pushMask);
                        long s = 1L << index;
                        long blockCheck = blockerBoardW(bitboards, s);
                        while (blockCheck != 0) {
                            int index2 = Long.numberOfTrailingZeros(blockCheck);
                            moves += ("" + (index2 / 8) + (index2 % 8) + (index / 8) + (index % 8) + " ");
                            blockCheck = blockCheck & (blockCheck - 1);
                        }
                        pushMask = pushMask & (pushMask - 1);
                    }
                }
            }
        }
        bitboards.BN |= pinnedN;
        bitboards.BQ |= pinnedQ;
        bitboards.BB |= pinnedB;
        bitboards.BR |= pinnedR;
        bitboards.BP |= pinnedP;
        return moves;
    }

    private static String generatePawnMovesW(Bitboards bitboards) {
        String moves = "";
        long WP = bitboards.WP;
        long BA = bitboards.BA;
        long A = bitboards.A;
        String enPassant = bitboards.enPassant;
        long moveBitboard;
        //capture right
        moveBitboard = WP >>> 7 & BA & ~FILE_A & ~RANK_8;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //capture left
        moveBitboard = WP >>> 9 & BA & ~FILE_H & ~RANK_8;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //move 1 up
        moveBitboard = WP >>> 8 & ~RANK_8 & ~A;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        // move 2 up
        moveBitboard = ((WP & RANK_2) >>> 8 & ~A) >>> 8 & ~A;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 2) + (index % 8) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //promote capture right
        moveBitboard = (WP >>> 7) & BA & RANK_8 & ~FILE_A;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "Q");
            moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "R");
            moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "N");
            moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "B");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //promote capture left
        moveBitboard = (WP >>> 9) & BA & RANK_8 & ~FILE_H;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "Q");
            moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "R");
            moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "N");
            moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "B");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //promote move 1 up
        moveBitboard = (WP >>> 8) & ~A & RANK_8;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8) + (index / 8) + (index % 8) + "Q");
            moves += ("" + (index / 8 + 1) + (index % 8) + (index / 8) + (index % 8) + "R");
            moves += ("" + (index / 8 + 1) + (index % 8) + (index / 8) + (index % 8) + "N");
            moves += ("" + (index / 8 + 1) + (index % 8) + (index / 8) + (index % 8) + "B");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }

        if (enPassant != null) {
            long bitBoardEnP = 1L;
            int row = (enPassant.charAt(0) - '0');
            int col = (enPassant.charAt(1) - '0');
            int position = row * 8 + col;
            bitBoardEnP = bitBoardEnP << position;
            //capture en passant right
            moveBitboard = (WP << 1 & ~FILE_A & bitBoardEnP) >>> 8;
            while (moveBitboard != 0) {
                int index = Long.numberOfTrailingZeros(moveBitboard);
                long enPcaptureInitial = 1L << (index + 7);
                long enPcaptureFinal = 1L << (index);
                Bitboards aux = new Bitboards(bitboards);
                aux.WP &= ~enPcaptureInitial;
                aux.WP |= enPcaptureFinal;
                aux.BP &= ~bitBoardEnP;
                aux.WA &= ~enPcaptureInitial;
                aux.WA |= enPcaptureFinal;
                aux.BA &= ~bitBoardEnP;
                aux.A &= ~bitBoardEnP & ~enPcaptureInitial;
                aux.A |= enPcaptureFinal;
                if (attackerBoardW(aux, aux.WK) == 0) {
                    moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
                }
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
            //capture en passant left
            moveBitboard = (WP >>> 1 & ~FILE_H & bitBoardEnP) >>> 8;
            while (moveBitboard != 0) {
                int index = Long.numberOfTrailingZeros(moveBitboard);
                long enPcaptureInitial = 1L << (index + 9);
                long enPcaptureFinal = 1L << (index);
                Bitboards aux = new Bitboards(bitboards);
                aux.WP &= ~enPcaptureInitial;
                aux.WP |= enPcaptureFinal;
                aux.BP &= ~bitBoardEnP;
                aux.WA &= ~enPcaptureInitial;
                aux.WA |= enPcaptureFinal;
                aux.BA &= ~bitBoardEnP;
                aux.A &= ~bitBoardEnP & ~enPcaptureInitial;
                aux.A |= enPcaptureFinal;
                if (attackerBoardW(aux, aux.WK) == 0) {
                    moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
                }
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }
        return moves;
    }

    private static String generatePawnMovesB(Bitboards bitboards) {
        String moves = "";
        long BP = bitboards.BP;
        long WA = bitboards.WA;
        long A = bitboards.A;
        String enPassant = bitboards.enPassant;
        long moveBitboard;
        //capture right
        moveBitboard = BP << 7 & WA & ~FILE_H & ~RANK_1;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //capture left
        moveBitboard = BP << 9 & WA & ~FILE_A & ~RANK_1;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //move 1 down
        moveBitboard = BP << 8 & ~RANK_1 & ~A;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        // move 2 down
        moveBitboard = ((BP & RANK_7) << 8 & ~A) << 8 & ~A;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 2) + (index % 8) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //promote capture right
        moveBitboard = (BP << 7) & WA & RANK_1 & ~FILE_H;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "q");
            moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "r");
            moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "n");
            moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + "b");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //promote capture left
        moveBitboard = (BP << 9) & WA & RANK_1 & ~FILE_A;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "q");
            moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "r");
            moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "n");
            moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + "b");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        //promote move 1 down
        moveBitboard = (BP << 8) & ~A & RANK_1;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8) + (index / 8) + (index % 8) + "q");
            moves += ("" + (index / 8 - 1) + (index % 8) + (index / 8) + (index % 8) + "r");
            moves += ("" + (index / 8 - 1) + (index % 8) + (index / 8) + (index % 8) + "n");
            moves += ("" + (index / 8 - 1) + (index % 8) + (index / 8) + (index % 8) + "b");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }

        if (enPassant != null) {
            long bitBoardEnP = 1L;
            int row = (enPassant.charAt(0) - '0');
            int col = (enPassant.charAt(1) - '0');
            int position = row * 8 + col;
            bitBoardEnP = bitBoardEnP << position;
            //capture en passant right
            moveBitboard = (BP >>> 1 & ~FILE_H & bitBoardEnP) << 8;
            while (moveBitboard != 0) {
                int index = Long.numberOfTrailingZeros(moveBitboard);
                long enPcaptureInitial = 1L << (index - 7);
                long enPcaptureFinal = 1L << (index);
                Bitboards aux = new Bitboards(bitboards);
                aux.BP &= ~enPcaptureInitial;
                aux.BP |= enPcaptureFinal;
                aux.WP &= ~bitBoardEnP;
                aux.BA &= ~enPcaptureInitial;
                aux.BA |= enPcaptureFinal;
                aux.WA &= ~bitBoardEnP;
                aux.A &= ~bitBoardEnP & ~enPcaptureInitial;
                aux.A |= enPcaptureFinal;
                if (attackerBoardB(aux, aux.BK) == 0) {
                    moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
                }
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
            //capture en passant left
            moveBitboard = (BP << 1 & ~FILE_A & bitBoardEnP) << 8;
            while (moveBitboard != 0) {
                int index = Long.numberOfTrailingZeros(moveBitboard);
                long enPcaptureInitial = 1L << (index - 9);
                long enPcaptureFinal = 1L << (index);
                Bitboards aux = new Bitboards(bitboards);
                aux.BP &= ~enPcaptureInitial;
                aux.BP |= enPcaptureFinal;
                aux.WP &= ~bitBoardEnP;
                aux.BA &= ~enPcaptureInitial;
                aux.BA |= enPcaptureFinal;
                aux.WA &= ~bitBoardEnP;
                aux.A &= ~bitBoardEnP & ~enPcaptureInitial;
                aux.A |= enPcaptureFinal;
                if (attackerBoardB(aux, aux.BK) == 0) {
                    moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
                }
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }
        return moves;
    }

    private static String generateKnightMovesW(Bitboards bitboards) {
        String moves = "";
        long WN = bitboards.WN;
        long WA = bitboards.WA;
        long moveBitboard;
        moveBitboard = WN >>> 17 & ~FILE_H & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 2) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = WN >>> 15 & ~FILE_A & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 2) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = WN >>> 10 & ~FILE_GH & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 + 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = WN >>> 6 & ~FILE_AB & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 - 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }

        moveBitboard = WN << 17 & ~FILE_A & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 2) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = WN << 15 & ~FILE_H & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 2) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = WN << 10 & ~FILE_AB & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 - 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = WN << 6 & ~FILE_GH & ~WA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 + 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        return moves;
    }

    private static String generateKnightMovesB(Bitboards bitboards) {
        String moves = "";
        long BN = bitboards.BN;
        long BA = bitboards.BA;
        long moveBitboard;
        moveBitboard = BN >>> 17 & ~FILE_H & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 2) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = BN >>> 15 & ~FILE_A & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 2) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = BN >>> 10 & ~FILE_GH & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 + 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = BN >>> 6 & ~FILE_AB & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 - 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }

        moveBitboard = BN << 17 & ~FILE_A & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 2) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = BN << 15 & ~FILE_H & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 2) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = BN << 10 & ~FILE_AB & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 - 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }
        moveBitboard = BN << 6 & ~FILE_GH & ~BA;
        while (moveBitboard != 0) {
            int index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 + 2) + (index / 8) + (index % 8) + " ");
            moveBitboard = moveBitboard & (moveBitboard - 1);
        }

        return moves;
    }

    private static String generateRookMovesW(Bitboards bitboards) {
        String moves = "";
        long WR = bitboards.WR;
        long WA = bitboards.WA;
        long A = bitboards.A & ~bitboards.BK;
        long moveBitboard;
        while (WR != 0) {
            int index = Long.numberOfTrailingZeros(WR);
            long singleRookBitboard = 1L << index;
            long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(singleRookBitboard)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * singleRookBitboard))) & rankMasks[index / 8];
            long vertical = (((A & fileMasks[index % 8]) - (2 * singleRookBitboard)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(singleRookBitboard)))) & fileMasks[index % 8];
            WR = WR & (WR - 1);
            moveBitboard = (horizontal | vertical) & ~WA;
            while (moveBitboard != 0) {
                int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }

        return moves;
    }

    private static String generateRookMovesB(Bitboards bitboards) {
        String moves = "";
        long BR = bitboards.BR;
        long BA = bitboards.BA;
        long A = bitboards.A & ~bitboards.WK;
        long moveBitboard;
        while (BR != 0) {
            int index = Long.numberOfTrailingZeros(BR);
            long singleRookBitboard = 1L << index;
            long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(singleRookBitboard)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * singleRookBitboard))) & rankMasks[index / 8];
            long vertical = (((A & fileMasks[index % 8]) - (2 * singleRookBitboard)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(singleRookBitboard)))) & fileMasks[index % 8];
            BR = BR & (BR - 1);
            moveBitboard = (horizontal | vertical) & ~BA;
            while (moveBitboard != 0) {
                int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }

        return moves;
    }

    private static String generateBishopMovesW(Bitboards bitboards) {
        String moves = "";
        long WB = bitboards.WB;
        long WA = bitboards.WA;
        long A = bitboards.A & ~bitboards.BK;
        long moveBitboard;
        while (WB != 0) {
            int index = Long.numberOfTrailingZeros(WB);
            long singleBishopBitboard = 1L << index;
            long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * singleBishopBitboard)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(singleBishopBitboard)))) & diagonalMasks[index / 8 + index % 8];
            long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * singleBishopBitboard)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(singleBishopBitboard)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            WB = WB & (WB - 1);
            moveBitboard = (firstDiagonal | secondDiagonal) & ~WA;
            while (moveBitboard != 0) {
                int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }

        return moves;
    }

    private static String generateBishopMovesB(Bitboards bitboards) {
        String moves = "";
        long BB = bitboards.BB;
        long BA = bitboards.BA;
        long A = bitboards.A & ~bitboards.WK;
        long moveBitboard;
        while (BB != 0) {
            int index = Long.numberOfTrailingZeros(BB);
            long singleBishopBitboard = 1L << index;
            long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * singleBishopBitboard)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(singleBishopBitboard)))) & diagonalMasks[index / 8 + index % 8];
            long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * singleBishopBitboard)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(singleBishopBitboard)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            BB = BB & (BB - 1);
            moveBitboard = (firstDiagonal | secondDiagonal) & ~BA;
            while (moveBitboard != 0) {
                int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }

        return moves;
    }

    private static String generateQueenMovesW(Bitboards bitboards) {
        String moves = "";
        long WQ = bitboards.WQ;
        long WA = bitboards.WA;
        long A = bitboards.A & ~bitboards.BK;
        long moveBitboard;
        while (WQ != 0) {
            int index = Long.numberOfTrailingZeros(WQ);
            long singleQueenBitboard = 1L << index;
            long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(singleQueenBitboard)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * singleQueenBitboard))) & rankMasks[index / 8];
            long vertical = (((A & fileMasks[index % 8]) - (2 * singleQueenBitboard)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(singleQueenBitboard)))) & fileMasks[index % 8];
            long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * singleQueenBitboard)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(singleQueenBitboard)))) & diagonalMasks[index / 8 + index % 8];
            long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * singleQueenBitboard)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(singleQueenBitboard)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            WQ = WQ & (WQ - 1);
            moveBitboard = (horizontal | vertical | firstDiagonal | secondDiagonal) & ~WA;
            while (moveBitboard != 0) {
                int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }

        return moves;
    }

    private static String generateQueenMovesB(Bitboards bitboards) {
        String moves = "";
        long BQ = bitboards.BQ;
        long BA = bitboards.BA;
        long A = bitboards.A & ~bitboards.WK;
        long moveBitboard;
        while (BQ != 0) {
            int index = Long.numberOfTrailingZeros(BQ);
            long singleQueenBitboard = 1L << index;
            long horizontal = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(singleQueenBitboard)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * singleQueenBitboard))) & rankMasks[index / 8];
            long vertical = (((A & fileMasks[index % 8]) - (2 * singleQueenBitboard)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(singleQueenBitboard)))) & fileMasks[index % 8];
            long firstDiagonal = (((A & diagonalMasks[index / 8 + index % 8]) - (2 * singleQueenBitboard)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(singleQueenBitboard)))) & diagonalMasks[index / 8 + index % 8];
            long secondDiagonal = (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * singleQueenBitboard)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(singleQueenBitboard)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            BQ = BQ & (BQ - 1);
            moveBitboard = (horizontal | vertical | firstDiagonal | secondDiagonal) & ~BA;
            while (moveBitboard != 0) {
                int moveIndex = Long.numberOfTrailingZeros(moveBitboard);
                moves += ("" + (index / 8) + (index % 8) + (moveIndex / 8) + (moveIndex % 8) + " ");
                moveBitboard = moveBitboard & (moveBitboard - 1);
            }
        }

        return moves;
    }

    //tre sa optimizez metoda asta
    private static String generateKingMovesW(Bitboards bitboards) {
        String moves = "";
        long WK = bitboards.WK;
        long WA = bitboards.WA;
        long A = bitboards.A;
        long moveBitboard;
        boolean[] castle = bitboards.castle;
        int index;
        //up

        moveBitboard = (WK & ~RANK_8) >>> 8 & ~WA;
        if (moveBitboard != 0 && attackerBoardW(bitboards, ((WK & ~RANK_8) >>> 8)) == 0 && nextToBlackKing(bitboards, ((WK & ~RANK_8) >>> 8)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8) + (index / 8) + (index % 8) + " ");
        }
        //down
        moveBitboard = (WK & ~RANK_1) << 8 & ~WA;
        if (moveBitboard != 0 && attackerBoardW(bitboards, ((WK & ~RANK_1) << 8)) == 0 && nextToBlackKing(bitboards, ((WK & ~RANK_1) << 8)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8) + (index / 8) + (index % 8) + " ");
        }
        //left
        moveBitboard = (WK & ~FILE_A) >>> 1 & ~WA;
        if (moveBitboard != 0 && attackerBoardW(bitboards, ((WK & ~FILE_A) >>> 1)) == 0 && nextToBlackKing(bitboards, ((WK & ~FILE_A) >>> 1)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
        }
        //right
        moveBitboard = (WK & ~FILE_H) << 1 & ~WA;
        if (moveBitboard != 0 && attackerBoardW(bitboards, ((WK & ~FILE_H) << 1)) == 0 && nextToBlackKing(bitboards, ((WK & ~FILE_H) << 1)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
        }
        //up-left
        moveBitboard = (WK & ~FILE_A & ~RANK_8) >>> 9 & ~WA;
        if (moveBitboard != 0 && attackerBoardW(bitboards, ((WK & ~FILE_A & ~RANK_8) >>> 9)) == 0 && nextToBlackKing(bitboards, ((WK & ~RANK_8 & ~FILE_A) >>> 9)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
        }
        //up-right
        moveBitboard = (WK & ~FILE_H & ~RANK_8) >>> 7 & ~WA;
        if (moveBitboard != 0 && attackerBoardW(bitboards, ((WK & ~FILE_H & ~RANK_8) >>> 7)) == 0 && nextToBlackKing(bitboards, ((WK & ~RANK_8 & ~FILE_H) >>> 7)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
        }
        //down-left
        moveBitboard = (WK & ~FILE_A & ~RANK_1) << 7 & ~WA;
        if (moveBitboard != 0 & attackerBoardW(bitboards, ((WK & ~FILE_A & ~RANK_1) << 7)) == 0 && nextToBlackKing(bitboards, ((WK & ~RANK_1 & ~FILE_A) << 7)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
        }
        //down-right
        moveBitboard = (WK & ~FILE_H & ~RANK_1) << 9 & ~WA;
        if (moveBitboard != 0 && attackerBoardW(bitboards, ((WK & ~FILE_H & ~RANK_1) << 9)) == 0 && nextToBlackKing(bitboards, ((WK & ~RANK_1 & ~FILE_H) << 9)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
        }

        //castle short
        if (castle[0] && (RANK_1 & FILE_H & bitboards.WR) != 0 && (RANK_1 & (fileMasks[5] | fileMasks[6]) & A) == 0 && (attackerBoardW(bitboards, RANK_1 & fileMasks[5])) == 0 && nextToBlackKing(bitboards, (RANK_1 & fileMasks[5])) == false && (attackerBoardW(bitboards, RANK_1 & fileMasks[6])) == 0 && nextToBlackKing(bitboards, (RANK_1 & fileMasks[6])) == false && (attackerBoardW(bitboards, WK) == 0)) {
            moves += ("7476 ");
        }
        if (castle[1] && (RANK_1 & FILE_A & bitboards.WR) != 0 && (RANK_1 & (fileMasks[1] | fileMasks[2] | fileMasks[3]) & A) == 0 && (attackerBoardW(bitboards, RANK_1 & fileMasks[3])) == 0 && nextToBlackKing(bitboards, (RANK_1 & fileMasks[3])) == false && (attackerBoardW(bitboards, RANK_1 & fileMasks[2])) == 0 && nextToBlackKing(bitboards, (RANK_1 & fileMasks[2])) == false && (attackerBoardW(bitboards, WK) == 0)) {
            moves += ("7472 ");
        }

        return moves;
    }

    private static String generateKingMovesB(Bitboards bitboards) {
        String moves = "";
        long BK = bitboards.BK;
        long BA = bitboards.BA;
        long A = bitboards.A;
        long moveBitboard;
        boolean[] castle = bitboards.castle;
        int index;
        //up
        moveBitboard = (BK & ~RANK_8) >>> 8 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~RANK_8) >>> 8)) == 0 && nextToWhiteKing(bitboards, ((BK & ~RANK_8) >>> 8)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8) + (index / 8) + (index % 8) + " ");
        }
        //down
        moveBitboard = (BK & ~RANK_1) << 8 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~RANK_1) << 8)) == 0 && nextToWhiteKing(bitboards, ((BK & ~RANK_1) << 8)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8) + (index / 8) + (index % 8) + " ");
        }
        //left
        moveBitboard = (BK & ~FILE_A) >>> 1 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~FILE_A) >>> 1)) == 0 && nextToWhiteKing(bitboards, ((BK & ~FILE_A) >>> 1)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
        }
        //right
        moveBitboard = (BK & ~FILE_H) << 1 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~FILE_H) << 1)) == 0 && nextToWhiteKing(bitboards, ((BK & ~FILE_H) << 1)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
        }
        //up-left
        moveBitboard = (BK & ~FILE_A & ~RANK_8) >>> 9 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~FILE_A & ~RANK_8) >>> 9)) == 0 && nextToWhiteKing(bitboards, ((BK & ~RANK_8 & ~FILE_A) >>> 9)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
        }
        //up-right
        moveBitboard = (BK & ~FILE_H & ~RANK_8) >>> 7 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~FILE_H & ~RANK_8) >>> 7)) == 0 && nextToWhiteKing(bitboards, ((BK & ~RANK_8 & ~FILE_H) >>> 7)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 + 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
        }
        //down-left
        moveBitboard = (BK & ~FILE_A & ~RANK_1) << 7 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~FILE_A & ~RANK_1) << 7)) == 0 && nextToWhiteKing(bitboards, ((BK & ~RANK_1 & ~FILE_A) << 7)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 + 1) + (index / 8) + (index % 8) + " ");
        }
        //down-right
        moveBitboard = (BK & ~FILE_H & ~RANK_1) << 9 & ~BA;
        if (moveBitboard != 0 && attackerBoardB(bitboards, ((BK & ~FILE_H & ~RANK_1) << 9)) == 0 && nextToWhiteKing(bitboards, ((BK & ~RANK_1 & ~FILE_H) << 9)) == false) {
            index = Long.numberOfTrailingZeros(moveBitboard);
            moves += ("" + (index / 8 - 1) + (index % 8 - 1) + (index / 8) + (index % 8) + " ");
        }

        //castle short
        if (castle[2] && (RANK_8 & FILE_H & bitboards.BR) != 0 && (RANK_8 & (fileMasks[5] | fileMasks[6]) & A) == 0 && (attackerBoardB(bitboards, RANK_8 & fileMasks[5])) == 0 && nextToWhiteKing(bitboards, (RANK_8 & fileMasks[5])) == false && (attackerBoardB(bitboards, RANK_8 & fileMasks[6])) == 0 && nextToWhiteKing(bitboards, (RANK_8 & fileMasks[6])) == false && (attackerBoardB(bitboards, BK) == 0)) {
            moves += ("0406 ");
        }
        //castle long
        if (castle[3] && (RANK_8 & FILE_A & bitboards.BR) != 0 && (RANK_8 & (fileMasks[1] | fileMasks[2] | fileMasks[3]) & A) == 0 && (attackerBoardB(bitboards, RANK_8 & fileMasks[3])) == 0 && nextToWhiteKing(bitboards, (RANK_8 & fileMasks[3])) == false && (attackerBoardB(bitboards, RANK_8 & fileMasks[2])) == 0 && nextToWhiteKing(bitboards, (RANK_8 & fileMasks[2])) == false && (attackerBoardB(bitboards, BK) == 0)) {
            moves += ("0402 ");
        }

        return moves;
    }

    //given square, generate bitboard with all black pieces attacking square
    public static long attackerBoardW(Bitboards bitboards, long square) {
        if (square != 0) {
            long A = bitboards.A & ~bitboards.WK;
            int index = Long.numberOfTrailingZeros(square);
            long pawnAttacks;
            pawnAttacks = ((square >>> 7 & ~FILE_A) | (square >>> 9 & ~FILE_H)) & bitboards.BP;
            if (bitboards.enPassant != null && (bitboards.enPassant.charAt(0) - 48) == index / 8 && (bitboards.enPassant.charAt(1) - 48) == index % 8 && (square & bitboards.WA) != 0) {
                pawnAttacks |= ((square >> 1) & bitboards.BP & ~FILE_H);
                pawnAttacks |= ((square << 1) & bitboards.BP & ~FILE_A);
            }
            long knightAttacks = ((square >>> 17 & ~FILE_H) | (square >>> 15 & ~FILE_A) | (square >>> 10 & ~FILE_GH) | (square >>> 6 & ~FILE_AB) | (square << 17 & ~FILE_A) | (square << 15 & ~FILE_H) | (square << 10 & ~FILE_AB) | (square << 6 & ~FILE_GH)) & bitboards.BN;
            long rookAttacks = (Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(square)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * square))) & rankMasks[index / 8] ^ (((A & fileMasks[index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(square)))) & fileMasks[index % 8]) & (bitboards.BR | bitboards.BQ);
            long bishopAttacks = ((((A & diagonalMasks[index / 8 + index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(square)))) & diagonalMasks[index / 8 + index % 8] ^ (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(square)))) & antiDiagonalMasks[index / 8 + 7 - index % 8]) & (bitboards.BB | bitboards.BQ);
            return pawnAttacks | knightAttacks | rookAttacks | bishopAttacks;
        }
        return 0L;
    }

    //given square, generate bitboard with all white pieces attacking square
    public static long attackerBoardB(Bitboards bitboards, long square) {
        if (square != 0) {
            long A = bitboards.A & ~bitboards.BK;
            int index = Long.numberOfTrailingZeros(square);
            long pawnAttacks;
            pawnAttacks = ((square << 7 & ~FILE_H) | (square << 9 & ~FILE_A)) & bitboards.WP;
            if (bitboards.enPassant != null && (bitboards.enPassant.charAt(0) - 48) == index / 8 && (bitboards.enPassant.charAt(1) - 48) == index % 8 && (square & bitboards.BA) != 0) {
                pawnAttacks |= ((square >> 1) & bitboards.WP & ~FILE_H);
                pawnAttacks |= ((square << 1) & bitboards.WP & ~FILE_A);
            }
            long knightAttacks = ((square >>> 17 & ~FILE_H) | (square >>> 15 & ~FILE_A) | (square >>> 10 & ~FILE_GH) | (square >>> 6 & ~FILE_AB) | (square << 17 & ~FILE_A) | (square << 15 & ~FILE_H) | (square << 10 & ~FILE_AB) | (square << 6 & ~FILE_GH)) & bitboards.WN;
            long rookAttacks = (Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(square)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * square))) & rankMasks[index / 8] ^ (((A & fileMasks[index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(square)))) & fileMasks[index % 8]) & (bitboards.WR | bitboards.WQ);
            long bishopAttacks = ((((A & diagonalMasks[index / 8 + index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(square)))) & diagonalMasks[index / 8 + index % 8] ^ (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(square)))) & antiDiagonalMasks[index / 8 + 7 - index % 8]) & (bitboards.WB | bitboards.WQ);
            return pawnAttacks | knightAttacks | rookAttacks | bishopAttacks;
        }
        return 0L;
    }

    private static long blockerBoardW(Bitboards bitboards, long square) {
        if (square != 0) {
            long A = bitboards.A;
            int index = Long.numberOfTrailingZeros(square);
            long pawnAttacks;
            pawnAttacks = ((square >>> 7 & ~FILE_A & bitboards.WA) | (square >>> 9 & ~FILE_H & bitboards.WA)) & bitboards.BP;
            pawnAttacks |= square >>> 8 & bitboards.BP | ((square & rankMasks[3]) >>> 8 & ~A) >>> 8 & bitboards.BP;

            if (bitboards.enPassant != null && (bitboards.enPassant.charAt(0) - 48) == index / 8 - 1 && (bitboards.enPassant.charAt(1) - 48) == index % 8) {
                pawnAttacks |= (square >>> 7) & bitboards.BP & ~FILE_A;
                pawnAttacks |= (square >>> 9) & bitboards.BP & ~FILE_H;
            }
            long knightAttacks = ((square >>> 17 & ~FILE_H) | (square >>> 15 & ~FILE_A) | (square >>> 10 & ~FILE_GH) | (square >>> 6 & ~FILE_AB) | (square << 17 & ~FILE_A) | (square << 15 & ~FILE_H) | (square << 10 & ~FILE_AB) | (square << 6 & ~FILE_GH)) & bitboards.BN;
            long rookAttacks = (Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(square)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * square))) & rankMasks[index / 8] ^ (((A & fileMasks[index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(square)))) & fileMasks[index % 8]) & (bitboards.BR | bitboards.BQ);
            long bishopAttacks = ((((A & diagonalMasks[index / 8 + index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(square)))) & diagonalMasks[index / 8 + index % 8] ^ (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(square)))) & antiDiagonalMasks[index / 8 + 7 - index % 8]) & (bitboards.BB | bitboards.BQ);
            return pawnAttacks | knightAttacks | rookAttacks | bishopAttacks;
        }
        return 0L;
    }

    private static long blockerBoardB(Bitboards bitboards, long square) {
        if (square != 0) {
            long A = bitboards.A;
            int index = Long.numberOfTrailingZeros(square);
            long pawnAttacks;
            pawnAttacks = ((square << 7 & ~FILE_H & bitboards.BA) | (square << 9 & ~FILE_A & bitboards.BA)) & bitboards.WP;
            pawnAttacks |= square << 8 & bitboards.WP | ((square & rankMasks[4]) << 8 & ~A) << 8 & bitboards.WP;
            if (bitboards.enPassant != null && (bitboards.enPassant.charAt(0) - 48) == index / 8 + 1 && (bitboards.enPassant.charAt(1) - 48) == index % 8) {
                pawnAttacks |= (square << 7) & bitboards.WP & ~FILE_H;
                pawnAttacks |= (square << 9) & bitboards.WP & ~FILE_A;
            }
            long knightAttacks = ((square >>> 17 & ~FILE_H) | (square >>> 15 & ~FILE_A) | (square >>> 10 & ~FILE_GH) | (square >>> 6 & ~FILE_AB) | (square << 17 & ~FILE_A) | (square << 15 & ~FILE_H) | (square << 10 & ~FILE_AB) | (square << 6 & ~FILE_GH)) & bitboards.WN;
            long rookAttacks = (Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(square)))) & rankMasks[index / 8] ^ (A ^ (A - (2 * square))) & rankMasks[index / 8] ^ (((A & fileMasks[index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & fileMasks[index % 8]) - (2 * Long.reverse(square)))) & fileMasks[index % 8]) & (bitboards.WR | bitboards.WQ);
            long bishopAttacks = ((((A & diagonalMasks[index / 8 + index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - (2 * Long.reverse(square)))) & diagonalMasks[index / 8 + index % 8] ^ (((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * square)) ^ Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * Long.reverse(square)))) & antiDiagonalMasks[index / 8 + 7 - index % 8]) & (bitboards.WB | bitboards.WQ);
            return pawnAttacks | knightAttacks | rookAttacks | bishopAttacks;
        }
        return 0L;
    }

    // mask between king and horizontal/vertical slider
    private static long getPushMaskLine(long attackers, long K) {
        int index = Long.numberOfTrailingZeros(K);
        // int indexAttacker = Long.numberOfTrailingZeros(attackers);
        long A = K | attackers;
        long left = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(K)))) & rankMasks[index / 8];
        if ((left & attackers) != 0) {
            return (left & ~attackers);
        }
        long right = (A ^ (A - (2 * K))) & rankMasks[index / 8];
        if ((right & attackers) != 0) {
            return (right & ~attackers);
        }
        long down = ((A & fileMasks[index % 8]) ^ ((A & fileMasks[index % 8]) - (2 * K))) & fileMasks[index % 8];
        if ((down & attackers) != 0) {
            return (down & ~attackers);
        }
        long up = Long.reverse(Long.reverse(A & fileMasks[index % 8]) ^ (Long.reverse(A & fileMasks[index % 8]) - 2 * (Long.reverse(K)))) & fileMasks[index % 8];
        if ((up & attackers) != 0) {
            return (up & ~attackers);
        }
        return 0L;
    }

    // mask between king and diagonal slider
    private static long getPushMaskDiagonal(long attackers, long K) {
        int index = Long.numberOfTrailingZeros(K);
        // int indexAttacker = Long.numberOfTrailingZeros(attackers);
        long A = K | attackers;
        long diag1 = ((A & diagonalMasks[index / 8 + index % 8]) ^ ((A & diagonalMasks[index / 8 + index % 8]) - (2 * K))) & diagonalMasks[index / 8 + index % 8];
        if ((diag1 & attackers) != 0) {
            return (diag1 & ~attackers);
        }
        long diag2 = Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) ^ (Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - 2 * (Long.reverse(K)))) & diagonalMasks[index / 8 + index % 8];
        if ((diag2 & attackers) != 0) {
            return (diag2 & ~attackers);
        }
        long adiag1 = ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * K))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
        if ((adiag1 & attackers) != 0) {
            return (adiag1 & ~attackers);
        }
        long adiag2 = Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ (Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - 2 * (Long.reverse(K)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
        if ((adiag2 & attackers) != 0) {
            return (adiag2 & ~attackers);
        }
        return 0L;
    }

    public static long getPinnedPiecesW(Bitboards bitboards, long WK) {
        long BQ = bitboards.BQ;
        long BB = bitboards.BB;
        long BR = bitboards.BR;
        long A = bitboards.A;
        long WA = bitboards.WA;
        long pinned = 0L;
        int kingIndex = Long.numberOfTrailingZeros(WK);

        long diag1King = ((A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) ^ ((A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) - (2 * WK))) & diagonalMasks[kingIndex / 8 + kingIndex % 8] & WA;
        int pinnedIndex = Long.numberOfTrailingZeros(diag1King);
        long diag1Pinned = ((A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) ^ ((A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) - (2 * diag1King))) & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8] & (BQ | BB);
        if (diag1Pinned != 0) {
            pinned |= diag1King;
        }
        long diag2King = Long.reverse(Long.reverse(A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) ^ (Long.reverse(A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) - 2 * (Long.reverse(WK)))) & diagonalMasks[kingIndex / 8 + kingIndex % 8] & WA;
        pinnedIndex = Long.numberOfTrailingZeros(diag2King);
        long diag2Pinned = Long.reverse(Long.reverse(A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) ^ (Long.reverse(A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) - 2 * (Long.reverse(diag2King)))) & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8] & (BQ | BB);
        if (diag2Pinned != 0) {
            pinned |= diag2King;
        }

        long adiag1King = ((A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) ^ ((A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) - (2 * WK))) & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8] & WA;
        if (adiag1King != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(adiag1King);
            long adiag1Pinned = ((A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) ^ ((A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) - (2 * adiag1King))) & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8] & (BQ | BB);
            if (adiag1Pinned != 0) {
                pinned |= adiag1King;
            }
        }

        long adiag2King = Long.reverse(Long.reverse(A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) ^ (Long.reverse(A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) - 2 * (Long.reverse(WK)))) & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8] & WA;
        if (adiag2King != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(adiag2King);
            long adiag2Pinned = Long.reverse(Long.reverse(A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) ^ (Long.reverse(A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) - 2 * (Long.reverse(adiag2King)))) & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8] & (BQ | BB);
            if (adiag2Pinned != 0) {
                pinned |= adiag2King;
            }
        }

        long leftKing = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(WK)))) & rankMasks[kingIndex / 8] & WA;
        if (leftKing != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(leftKing);
            long leftPinned = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(leftKing)))) & rankMasks[pinnedIndex / 8] & (BR | BQ);
            if (leftPinned != 0) {
                pinned |= leftKing;
            }
        }

        long rightKing = (A ^ (A - (2 * WK))) & rankMasks[kingIndex / 8] & WA;
        if (rightKing != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(rightKing);
            long rightPinned = (A ^ (A - (2 * rightKing))) & rankMasks[pinnedIndex / 8] & (BR | BQ);
            if (rightPinned != 0) {
                pinned |= rightKing;
            }
        }

        long downKing = ((A & fileMasks[kingIndex % 8]) ^ ((A & fileMasks[kingIndex % 8]) - (2 * WK))) & fileMasks[kingIndex % 8] & WA;
        pinnedIndex = Long.numberOfTrailingZeros(downKing);
        long downPinned = ((A & fileMasks[pinnedIndex % 8]) ^ ((A & fileMasks[pinnedIndex % 8]) - (2 * downKing))) & fileMasks[pinnedIndex % 8] & (BR | BQ);
        if (downPinned != 0) {
            pinned |= downKing;
        }

        long upKing = Long.reverse(Long.reverse(A & fileMasks[kingIndex % 8]) ^ (Long.reverse(A & fileMasks[kingIndex % 8]) - 2 * (Long.reverse(WK)))) & fileMasks[kingIndex % 8] & WA;
        pinnedIndex = Long.numberOfTrailingZeros(upKing);
        long upPinned = Long.reverse(Long.reverse(A & fileMasks[pinnedIndex % 8]) ^ (Long.reverse(A & fileMasks[pinnedIndex % 8]) - 2 * (Long.reverse(upKing)))) & fileMasks[pinnedIndex % 8] & (BR | BQ);
        if (upPinned != 0) {
            pinned |= upKing;
        }
        /* while (BQ != 0) {
            int index = Long.numberOfTrailingZeros(BQ);
            long queen = 1L << index;
            long diag1Queen = ((A & diagonalMasks[index / 8 + index % 8]) ^ ((A & diagonalMasks[index / 8 + index % 8]) - (2 * queen))) & diagonalMasks[index / 8 + index % 8];
            long diag2Queen = Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) ^ (Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - 2 * (Long.reverse(queen)))) & diagonalMasks[index / 8 + index % 8];
            long adiag1Queen = ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * queen))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            long adiag2Queen = Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ (Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - 2 * (Long.reverse(queen)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            long leftQueen = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(queen)))) & rankMasks[index / 8];
            long rightQueen = (A ^ (A - (2 * queen))) & rankMasks[index / 8];
            long downQueen = ((A & fileMasks[index % 8]) ^ ((A & fileMasks[index % 8]) - (2 * queen))) & fileMasks[index % 8];
            long upQueen = Long.reverse(Long.reverse(A & fileMasks[index % 8]) ^ (Long.reverse(A & fileMasks[index % 8]) - 2 * (Long.reverse(queen)))) & fileMasks[index % 8];
            pinned |= ((diag1Queen & diag2King) | (diag2Queen & diag1King) | (adiag1King & adiag2Queen) | (adiag2King & adiag1Queen) | (leftQueen & rightKing) | (rightQueen & leftKing) | (upQueen & downKing) | (downQueen & upKing)) & WA;
            BQ = BQ & (BQ - 1);
        }
        while (BB != 0) {
            int index = Long.numberOfTrailingZeros(BB);
            long bishop = 1L << index;
            long diag1Bishop = ((A & diagonalMasks[index / 8 + index % 8]) ^ ((A & diagonalMasks[index / 8 + index % 8]) - (2 * bishop))) & diagonalMasks[index / 8 + index % 8];
            long diag2Bishop = Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) ^ (Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - 2 * (Long.reverse(bishop)))) & diagonalMasks[index / 8 + index % 8];
            long adiag1Bishop = ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * bishop))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            long adiag2Bishop = Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ (Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - 2 * (Long.reverse(bishop)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            pinned |= ((diag1Bishop & diag2King) | (diag2Bishop & diag1King) | (adiag1King & adiag2Bishop) | (adiag2King & adiag1Bishop)) & WA;
            BB = BB & (BB - 1);
        }
        while (BR != 0) {
            int index = Long.numberOfTrailingZeros(BR);
            long rook = 1L << index;
            long leftRook = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(rook)))) & rankMasks[index / 8];
            long rightRook = (A ^ (A - (2 * rook))) & rankMasks[index / 8];
            long downRook = ((A & fileMasks[index % 8]) ^ ((A & fileMasks[index % 8]) - (2 * rook))) & fileMasks[index % 8];
            long upRook = Long.reverse(Long.reverse(A & fileMasks[index % 8]) ^ (Long.reverse(A & fileMasks[index % 8]) - 2 * (Long.reverse(rook)))) & fileMasks[index % 8];
            pinned |= ((leftRook & rightKing) | (rightRook & leftKing) | (upKing & downRook) | (downKing & upRook)) & WA;
            BR = BR & (BR - 1);
        }*/
        return pinned;
    }

    private static long getPinnedPiecesB(Bitboards bitboards, long BK) {
        long WQ = bitboards.WQ;
        long WB = bitboards.WB;
        long WR = bitboards.WR;
        long A = bitboards.A;
        long BA = bitboards.BA;
        long pinned = 0L;
        int kingIndex = Long.numberOfTrailingZeros(BK);

        long diag1King = ((A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) ^ ((A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) - (2 * BK))) & diagonalMasks[kingIndex / 8 + kingIndex % 8] & BA;
        int pinnedIndex = Long.numberOfTrailingZeros(diag1King);
        long diag1Pinned = ((A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) ^ ((A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) - (2 * diag1King))) & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8] & (WQ | WB);
        if (diag1Pinned != 0) {
            pinned |= diag1King;
        }
        long diag2King = Long.reverse(Long.reverse(A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) ^ (Long.reverse(A & diagonalMasks[kingIndex / 8 + kingIndex % 8]) - 2 * (Long.reverse(BK)))) & diagonalMasks[kingIndex / 8 + kingIndex % 8] & BA;
        pinnedIndex = Long.numberOfTrailingZeros(diag2King);
        long diag2Pinned = Long.reverse(Long.reverse(A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) ^ (Long.reverse(A & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8]) - 2 * (Long.reverse(diag2King)))) & diagonalMasks[pinnedIndex / 8 + pinnedIndex % 8] & (WQ | WB);
        if (diag2Pinned != 0) {
            pinned |= diag2King;
        }

        long adiag1King = ((A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) ^ ((A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) - (2 * BK))) & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8] & BA;
        if (adiag1King != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(adiag1King);
            long adiag1Pinned = ((A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) ^ ((A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) - (2 * adiag1King))) & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8] & (WQ | WB);
            if (adiag1Pinned != 0) {
                pinned |= adiag1King;
            }
        }

        long adiag2King = Long.reverse(Long.reverse(A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) ^ (Long.reverse(A & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8]) - 2 * (Long.reverse(BK)))) & antiDiagonalMasks[kingIndex / 8 + 7 - kingIndex % 8] & BA;
        if (adiag2King != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(adiag2King);
            long adiag2Pinned = Long.reverse(Long.reverse(A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) ^ (Long.reverse(A & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8]) - 2 * (Long.reverse(adiag2King)))) & antiDiagonalMasks[pinnedIndex / 8 + 7 - pinnedIndex % 8] & (WQ | WB);
            if (adiag2Pinned != 0) {
                pinned |= adiag2King;
            }
        }

        long leftKing = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(BK)))) & rankMasks[kingIndex / 8] & BA;
        if (leftKing != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(leftKing);
            long leftPinned = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(leftKing)))) & rankMasks[pinnedIndex / 8] & (WR | WQ);
            if (leftPinned != 0) {
                pinned |= leftKing;
            }
        }

        long rightKing = (A ^ (A - (2 * BK))) & rankMasks[kingIndex / 8] & BA;
        if (rightKing != 0) {
            pinnedIndex = Long.numberOfTrailingZeros(rightKing);
            long rightPinned = (A ^ (A - (2 * rightKing))) & rankMasks[pinnedIndex / 8] & (WR | WQ);
            if (rightPinned != 0) {
                pinned |= rightKing;
            }
        }

        long downKing = ((A & fileMasks[kingIndex % 8]) ^ ((A & fileMasks[kingIndex % 8]) - (2 * BK))) & fileMasks[kingIndex % 8] & BA;
        pinnedIndex = Long.numberOfTrailingZeros(downKing);
        long downPinned = ((A & fileMasks[pinnedIndex % 8]) ^ ((A & fileMasks[pinnedIndex % 8]) - (2 * downKing))) & fileMasks[pinnedIndex % 8] & (WR | WQ);
        if (downPinned != 0) {
            pinned |= downKing;
        }

        long upKing = Long.reverse(Long.reverse(A & fileMasks[kingIndex % 8]) ^ (Long.reverse(A & fileMasks[kingIndex % 8]) - 2 * (Long.reverse(BK)))) & fileMasks[kingIndex % 8] & BA;
        pinnedIndex = Long.numberOfTrailingZeros(upKing);
        long upPinned = Long.reverse(Long.reverse(A & fileMasks[pinnedIndex % 8]) ^ (Long.reverse(A & fileMasks[pinnedIndex % 8]) - 2 * (Long.reverse(upKing)))) & fileMasks[pinnedIndex % 8] & (WR | WQ);
        if (upPinned != 0) {
            pinned |= upKing;
        }

        /*   while (WQ != 0) {
            int index = Long.numberOfTrailingZeros(WQ);
            long queen = 1L << index;
            long diag1Queen = ((A & diagonalMasks[index / 8 + index % 8]) ^ ((A & diagonalMasks[index / 8 + index % 8]) - (2 * queen))) & diagonalMasks[index / 8 + index % 8];
            long diag2Queen = Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) ^ (Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - 2 * (Long.reverse(queen)))) & diagonalMasks[index / 8 + index % 8];
            long adiag1Queen = ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * queen))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            long adiag2Queen = Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ (Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - 2 * (Long.reverse(queen)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            long leftQueen = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(queen)))) & rankMasks[index / 8];
            long rightQueen = (A ^ (A - (2 * queen))) & rankMasks[index / 8];
            long downQueen = ((A & fileMasks[index % 8]) ^ ((A & fileMasks[index % 8]) - (2 * queen))) & fileMasks[index % 8];
            long upQueen = Long.reverse(Long.reverse(A & fileMasks[index % 8]) ^ (Long.reverse(A & fileMasks[index % 8]) - 2 * (Long.reverse(queen)))) & fileMasks[index % 8];
            pinned |= ((diag1Queen & diag2King) | (diag2Queen & diag1King) | (adiag1King & adiag2Queen) | (adiag2King & adiag1Queen) | (leftQueen & rightKing) | (rightQueen & leftKing) | (upQueen & downKing) | (downQueen & upKing)) & BA;
            WQ = WQ & (WQ - 1);
        }
        while (WB != 0) {
            int index = Long.numberOfTrailingZeros(WB);
            long bishop = 1L << index;
            long diag1Bishop = ((A & diagonalMasks[index / 8 + index % 8]) ^ ((A & diagonalMasks[index / 8 + index % 8]) - (2 * bishop))) & diagonalMasks[index / 8 + index % 8];
            long diag2Bishop = Long.reverse(Long.reverse(A & diagonalMasks[index / 8 + index % 8]) ^ (Long.reverse(A & diagonalMasks[index / 8 + index % 8]) - 2 * (Long.reverse(bishop)))) & diagonalMasks[index / 8 + index % 8];
            long adiag1Bishop = ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ ((A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - (2 * bishop))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            long adiag2Bishop = Long.reverse(Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) ^ (Long.reverse(A & antiDiagonalMasks[index / 8 + 7 - index % 8]) - 2 * (Long.reverse(bishop)))) & antiDiagonalMasks[index / 8 + 7 - index % 8];
            pinned |= ((diag1Bishop & diag2King) | (diag2Bishop & diag1King) | (adiag1King & adiag2Bishop) | (adiag2King & adiag1Bishop)) & BA;
            WB = WB & (WB - 1);
        }
        while (WR != 0) {
            int index = Long.numberOfTrailingZeros(WR);
            long rook = 1L << index;
            long leftRook = Long.reverse(Long.reverse(A) ^ (Long.reverse(A) - 2 * (Long.reverse(rook)))) & rankMasks[index / 8];
            long rightRook = (A ^ (A - (2 * rook))) & rankMasks[index / 8];
            long downRook = ((A & fileMasks[index % 8]) ^ ((A & fileMasks[index % 8]) - (2 * rook))) & fileMasks[index % 8];
            long upRook = Long.reverse(Long.reverse(A & fileMasks[index % 8]) ^ (Long.reverse(A & fileMasks[index % 8]) - 2 * (Long.reverse(rook)))) & fileMasks[index % 8];
            pinned |= ((leftRook & rightKing) | (rightRook & leftKing) | (upKing & downRook) | (downKing & upRook)) & BA;
            WR = WR & (WR - 1);
        }*/
        return pinned;
    }

    static void makeMove(Bitboards bitboards, String move) {

        long hashKey = bitboards.hashKey;
        long initialSquare = 1L << (move.charAt(0) - 48) * 8 + (move.charAt(1) - 48);
        int initialSquareIndex = Long.numberOfTrailingZeros(initialSquare);
        long finalSquare = 1L << (move.charAt(2) - 48) * 8 + (move.charAt(3) - 48);
        int finalSquareIndex = Long.numberOfTrailingZeros(finalSquare);
        char pieceCaptured = ' ';
        boolean enPassant = false;
        boolean promotion = false;
        boolean movedPawnUp2Squares = false;
        //white's move
        if (bitboards.turn) {
            if ((bitboards.WQ & initialSquare) != 0) {
                bitboards.WQ = bitboards.WQ & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[1][initialSquareIndex] ^ randomIntegers[1][finalSquareIndex];
            } else if ((bitboards.WR & initialSquare) != 0) {
                if (bitboards.castle[0] && (RANK_1 & FILE_H & initialSquare) != 0) {
                    bitboards.castle[0] = false;
                }
                if (bitboards.castle[1] && (RANK_1 & FILE_A & initialSquare) != 0) {
                    bitboards.castle[1] = false;
                }
                bitboards.WR = bitboards.WR & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[2][initialSquareIndex] ^ randomIntegers[2][finalSquareIndex];
            } else if ((bitboards.WN & initialSquare) != 0) {
                bitboards.WN = bitboards.WN & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[4][initialSquareIndex] ^ randomIntegers[4][finalSquareIndex];
            } else if ((bitboards.WB & initialSquare) != 0) {
                bitboards.WB = bitboards.WB & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[3][initialSquareIndex] ^ randomIntegers[3][finalSquareIndex];
            } else if ((bitboards.WP & initialSquare) != 0) {
                if (initialSquare >>> 16 == finalSquare) {
                    int index = Long.numberOfTrailingZeros(finalSquare);
                    bitboards.enPassant = "" + (index / 8) + (index % 8);
                    movedPawnUp2Squares = true;
                }
                bitboards.WP = bitboards.WP & ~initialSquare;
                hashKey ^= randomIntegers[5][initialSquareIndex];
                if ((initialSquare >>> 7 == finalSquare || initialSquare >>> 9 == finalSquare) && (bitboards.BA & finalSquare) == 0) {
                    enPassant = true;
                    bitboards.BP = bitboards.BP & ~(finalSquare << 8);
                    hashKey ^= randomIntegers[11][Long.numberOfTrailingZeros(finalSquare << 8)];
                }
                if (move.charAt(4) == ' ') {
                    bitboards.WP = bitboards.WP | finalSquare;
                    hashKey ^= randomIntegers[5][finalSquareIndex];
                } else {
                    promotion = true;
                    switch (move.charAt(4)) {
                        case 'Q':
                            bitboards.WQ = bitboards.WQ | finalSquare;
                            hashKey ^= randomIntegers[1][finalSquareIndex];
                            break;
                        case 'R':
                            bitboards.WR = bitboards.WR | finalSquare;
                            hashKey ^= randomIntegers[2][finalSquareIndex];
                            break;
                        case 'N':
                            bitboards.WN = bitboards.WN | finalSquare;
                            hashKey ^= randomIntegers[4][finalSquareIndex];
                            break;
                        case 'B':
                            bitboards.WB = bitboards.WB | finalSquare;
                            hashKey ^= randomIntegers[3][finalSquareIndex];
                            break;
                    }
                }
            } else {
                bitboards.castle[0] = false;
                bitboards.castle[1] = false;
                bitboards.WK = (bitboards.WK & ~initialSquare) | finalSquare;
                hashKey ^= randomIntegers[0][initialSquareIndex] ^ randomIntegers[0][finalSquareIndex];
                if (initialSquare << 2 == finalSquare) {
                    bitboards.WR = bitboards.WR & ~(finalSquare << 1) | (finalSquare >>> 1);
                    hashKey ^= randomIntegers[2][Long.numberOfTrailingZeros(finalSquare << 1)] ^ randomIntegers[2][Long.numberOfTrailingZeros(finalSquare >>> 1)];
                } else if (initialSquare >>> 2 == finalSquare) {
                    bitboards.WR = bitboards.WR & ~(finalSquare >>> 2) | (finalSquare << 1);
                    hashKey ^= randomIntegers[2][Long.numberOfTrailingZeros(finalSquare << 1)] ^ randomIntegers[2][Long.numberOfTrailingZeros(finalSquare >>> 2)];
                }
            }
            bitboards.WA = bitboards.WK | bitboards.WQ | bitboards.WR | bitboards.WN | bitboards.WB | bitboards.WP;
            if ((bitboards.BQ & finalSquare) != 0) {
                pieceCaptured = 'q';
                bitboards.BQ = bitboards.BQ & ~finalSquare;
                hashKey ^= randomIntegers[7][finalSquareIndex];
            } else if ((bitboards.BR & finalSquare) != 0) {
                pieceCaptured = 'r';
                bitboards.BR = bitboards.BR & ~finalSquare;
                hashKey ^= randomIntegers[8][finalSquareIndex];
            } else if ((bitboards.BN & finalSquare) != 0) {
                pieceCaptured = 'n';
                bitboards.BN = bitboards.BN & ~finalSquare;
                hashKey ^= randomIntegers[10][finalSquareIndex];
            } else if ((bitboards.BB & finalSquare) != 0) {
                pieceCaptured = 'b';
                bitboards.BB = bitboards.BB & ~finalSquare;
                hashKey ^= randomIntegers[9][finalSquareIndex];
            } else if ((bitboards.BP & finalSquare) != 0) {
                pieceCaptured = 'p';
                bitboards.BP = bitboards.BP & ~finalSquare;
                hashKey ^= randomIntegers[11][finalSquareIndex];
            }
            bitboards.BA = bitboards.BK | bitboards.BQ | bitboards.BR | bitboards.BN | bitboards.BB | bitboards.BP;
            bitboards.turn = false;
            long rook = 1L << 63;
            if ((rook & bitboards.WR) == 0) {
                bitboards.castle[0] = false;
            }
            rook = 1L << 56;
            if ((rook & bitboards.WR) == 0) {
                bitboards.castle[1] = false;
            }
        } else {
            if ((bitboards.BQ & initialSquare) != 0) {
                bitboards.BQ = bitboards.BQ & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[7][initialSquareIndex] ^ randomIntegers[7][finalSquareIndex];
            } else if ((bitboards.BR & initialSquare) != 0) {
                if (bitboards.castle[2] && (RANK_8 & FILE_H & initialSquare) != 0) {
                    bitboards.castle[2] = false;
                }
                if (bitboards.castle[3] && (RANK_8 & FILE_A & initialSquare) != 0) {
                    bitboards.castle[3] = false;
                }
                bitboards.BR = bitboards.BR & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[8][initialSquareIndex] ^ randomIntegers[8][finalSquareIndex];
            } else if ((bitboards.BN & initialSquare) != 0) {
                bitboards.BN = bitboards.BN & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[10][initialSquareIndex] ^ randomIntegers[10][finalSquareIndex];
            } else if ((bitboards.BB & initialSquare) != 0) {
                bitboards.BB = bitboards.BB & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[9][initialSquareIndex] ^ randomIntegers[9][finalSquareIndex];
            } else if ((bitboards.BP & initialSquare) != 0) {
                bitboards.BP = bitboards.BP & ~initialSquare;
                hashKey ^= randomIntegers[11][initialSquareIndex];
                if (initialSquare << 16 == finalSquare) {
                    int index = Long.numberOfTrailingZeros(finalSquare);
                    bitboards.enPassant = "" + (index / 8) + (index % 8);
                    movedPawnUp2Squares = true;
                }
                if ((initialSquare << 7 == finalSquare || initialSquare << 9 == finalSquare) && (bitboards.WA & finalSquare) == 0) {
                    enPassant = true;
                    bitboards.WP = bitboards.WP & ~(finalSquare >>> 8);
                    hashKey ^= randomIntegers[5][Long.numberOfTrailingZeros(finalSquare >>> 8)];
                }
                if (move.charAt(4) == ' ') {
                    bitboards.BP = bitboards.BP | finalSquare;
                    hashKey ^= randomIntegers[11][finalSquareIndex];
                } else {
                    promotion = true;
                    switch (move.charAt(4)) {
                        case 'q':
                            bitboards.BQ = bitboards.BQ | finalSquare;
                            hashKey ^= randomIntegers[7][finalSquareIndex];
                            break;
                        case 'r':
                            bitboards.BR = bitboards.BR | finalSquare;
                            hashKey ^= randomIntegers[8][finalSquareIndex];
                            break;
                        case 'n':
                            bitboards.BN = bitboards.BN | finalSquare;
                            hashKey ^= randomIntegers[10][finalSquareIndex];
                            break;
                        case 'b':
                            bitboards.BB = bitboards.BB | finalSquare;
                            hashKey ^= randomIntegers[9][finalSquareIndex];
                            break;
                    }
                }
            } else {
                bitboards.castle[2] = false;
                bitboards.castle[3] = false;
                bitboards.BK = bitboards.BK & ~initialSquare | finalSquare;
                hashKey ^= randomIntegers[6][initialSquareIndex] ^ randomIntegers[6][finalSquareIndex];
                if (initialSquare << 2 == finalSquare) {
                    bitboards.BR = bitboards.BR & ~(finalSquare << 1) | (finalSquare >>> 1);
                    hashKey ^= randomIntegers[8][Long.numberOfTrailingZeros(finalSquare << 1)] ^ randomIntegers[8][Long.numberOfTrailingZeros(finalSquare >>> 1)];
                } else if (initialSquare >>> 2 == finalSquare) {
                    bitboards.BR = bitboards.BR & ~(finalSquare >>> 2) | (finalSquare << 1);
                    hashKey ^= randomIntegers[8][Long.numberOfTrailingZeros(finalSquare << 1)] ^ randomIntegers[8][Long.numberOfTrailingZeros(finalSquare >>> 2)];
                }
            }
            bitboards.BA = bitboards.BK | bitboards.BQ | bitboards.BR | bitboards.BN | bitboards.BB | bitboards.BP;
            if ((bitboards.WQ & finalSquare) != 0) {
                pieceCaptured = 'Q';
                bitboards.WQ = bitboards.WQ & ~finalSquare;
                hashKey ^= randomIntegers[1][finalSquareIndex];
            } else if ((bitboards.WR & finalSquare) != 0) {
                pieceCaptured = 'R';
                bitboards.WR = bitboards.WR & ~finalSquare;
                hashKey ^= randomIntegers[2][finalSquareIndex];
            } else if ((bitboards.WN & finalSquare) != 0) {
                pieceCaptured = 'N';
                bitboards.WN = bitboards.WN & ~finalSquare;
                hashKey ^= randomIntegers[4][finalSquareIndex];
            } else if ((bitboards.WB & finalSquare) != 0) {
                pieceCaptured = 'B';
                bitboards.WB = bitboards.WB & ~finalSquare;
                hashKey ^= randomIntegers[3][finalSquareIndex];
            } else if ((bitboards.WP & finalSquare) != 0) {
                pieceCaptured = 'P';
                bitboards.WP = bitboards.WP & ~finalSquare;
                hashKey ^= randomIntegers[5][finalSquareIndex];
            }
            bitboards.WA = bitboards.WK | bitboards.WQ | bitboards.WR | bitboards.WN | bitboards.WB | bitboards.WP;
            bitboards.turn = true;
            long rook = 1L << 7;
            if ((rook & bitboards.BR) == 0) {
                bitboards.castle[2] = false;
            }
            rook = 1L;
            if ((rook & bitboards.BR) == 0) {
                bitboards.castle[3] = false;
            }
        }
        bitboards.A = bitboards.WA | bitboards.BA;
        bitboards.lastMove = move + pieceCaptured + (promotion) + (enPassant);
        if (!movedPawnUp2Squares) {
            bitboards.enPassant = null;
        }
        bitboards.hashKey = hashKey;
    }

    private static boolean nextToWhiteKing(Bitboards bitboards, long BK) {
        long kingAttacks = ((BK >>> 1 & ~FILE_H) | (BK << 1 & ~FILE_A) | (BK >>> 8 & ~RANK_1) | (BK << 8 & ~RANK_8) | (BK >>> 7 & ~RANK_1 & ~FILE_A) | (BK << 7 & ~FILE_H & ~RANK_8) | (BK >>> 9 & ~FILE_H & ~RANK_1) | (BK << 9 & ~RANK_8 & ~FILE_A)) & bitboards.WK;
        return kingAttacks != 0;
    }

    private static boolean nextToBlackKing(Bitboards bitboards, long WK) {
        long kingAttacks = ((WK >>> 1 & ~FILE_H) | (WK << 1 & ~FILE_A) | (WK >>> 8 & ~RANK_1) | (WK << 8 & ~RANK_8) | (WK >>> 7 & ~RANK_1 & ~FILE_A) | (WK << 7 & ~FILE_H & ~RANK_8) | (WK >>> 9 & ~FILE_H & ~RANK_1) | (WK << 9 & ~RANK_8 & ~FILE_A)) & bitboards.BK;
        return kingAttacks != 0;
    }
}
