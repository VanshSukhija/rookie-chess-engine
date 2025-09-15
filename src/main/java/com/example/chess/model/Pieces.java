package com.example.chess.model;

public enum Pieces {
    NONE, // 0
    PAWN, // 1
    KNIGHT, // 2
    BISHOP, // 3
    ROOK, // 4
    QUEEN, // 5
    KING; // 6

    public String getSymbol() {
        return switch (this) {
            case PAWN -> "P";
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case ROOK -> "R";
            case QUEEN -> "Q";
            case KING -> "K";
            case NONE -> "-";
            default -> throw new IllegalArgumentException("Unexpected value: " + this);
        };
    }

    public int[][] getDirections(Color color) {
        return switch (this) {
            case PAWN -> new int[][]{{1 * color.multiplier(), -1}, {1 * color.multiplier(), 1}};
            case KNIGHT -> new int[][]{{1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};
            case BISHOP -> new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            case ROOK -> new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            case QUEEN -> new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            case KING -> new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            default -> throw new IllegalArgumentException("Unexpected value: " + this);
        };
    }

    public int getValue() {
        return switch (this) {
            case PAWN -> 100;
            case KNIGHT -> 320;
            case BISHOP -> 330;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 0;
            case NONE -> 0;
            default -> throw new IllegalArgumentException("Unexpected value: " + this);
        };
    }
};
