package com.example.chess.model;

public class BoardRow {
    private int row = 0;

    public BoardRow(int row) {
        this.row = row;
    }

    public String getFen() {
        String fen = "";
        int spaceCount = 0;
        for(byte file = 0; file < 8; file++) {
            Pieces piece = getPiece(file);
            Color color = getColor(file);
            if(piece == Pieces.NONE) {
                spaceCount++;
            } else {
                if(spaceCount > 0) {
                    fen += spaceCount;
                    spaceCount = 0;
                }
                if(color == Color.WHITE) {
                    fen += piece.getSymbol().toUpperCase();
                } else {
                    fen += piece.getSymbol().toLowerCase();
                }
            }
        }
        if(fen.isEmpty() || spaceCount > 0) {
            fen += spaceCount;
        }
        return fen;
    }

    public int getRow() {
        return row;
    }

    public Color getColor(byte file) {
        int square = (row >> (4 * (7 - file)));
        int color = (square >> 3) & 1;
        return Color.values()[color];
    }

    public Pieces getPiece(byte file) {
        int square = (row >> (4 * (7 - file)));
        int piece = square & 7;
        return Pieces.values()[piece];
    }

    public byte getFile(byte file) {
        byte square = (byte) (row >> (4 * (7 - file)));
        return (byte) (square & 15);
    }

    public void setColor(byte file, Color color) {
        if(color == getColor(file)) {
            return;
        }

        int mask = (1 << (3 + (4 * (7 - file))));
        row ^= mask;
    }

    public void setPiece(byte file, Pieces piece) {
        int mask = (7 << (4 * (7 - file)));
        row &= ~mask;
        row |= (piece.ordinal() << (4 * (7 - file)));
    }

    public void setFile(byte file, Color color, Pieces piece) {
        setColor(file, color);
        setPiece(file, piece);
    }

    public void setFile(byte file, byte newFileValue) {
        setColor(file, Color.values()[(newFileValue >> 3) & 1]);
        setPiece(file, Pieces.values()[newFileValue & 7]);
    }

    public boolean isEmpty(byte file) {
        return getPiece(file) == Pieces.NONE;
    }

    public String printFile(byte file) {
        return getColor(file) == Color.WHITE ? 
            getPiece(file).getSymbol().toUpperCase() : 
            getPiece(file).getSymbol().toLowerCase();
    }
}
