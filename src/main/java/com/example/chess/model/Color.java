package com.example.chess.model;

public enum Color {
    BLACK, // 0
    WHITE; // 1

    public Color opposite() {
        return this == Color.BLACK ? Color.WHITE : Color.BLACK;
    }

    public int multiplier() {
        return this == Color.BLACK ? -1 : 1;
    }
}
