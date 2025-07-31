package com.example.chess.utils;

import com.example.chess.model.Pieces;

public class Utilities {
    public static boolean isOnBoard(int rank, int file) {
        return rank >= 0 && rank < 8 && file >= 0 && file < 8;
    }

    public static Pieces getPieceFromSymbol(char symbol) {
        return switch (symbol) {
            case 'P' -> Pieces.PAWN;
            case 'N' -> Pieces.KNIGHT;
            case 'B' -> Pieces.BISHOP;
            case 'R' -> Pieces.ROOK;
            case 'Q' -> Pieces.QUEEN;
            case 'K' -> Pieces.KING;
            default -> throw new IllegalArgumentException("Unexpected value: " + symbol);
        };
    }
}
