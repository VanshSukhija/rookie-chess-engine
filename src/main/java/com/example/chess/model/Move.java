package com.example.chess.model;

import java.util.List;

public class Move {
    protected byte fromFile;
    protected int fromRank;
    protected byte toFile;
    protected int toRank;
    protected boolean isCapture;
    protected boolean isKingInCheck;
    protected boolean isCheckmate;
    protected Color color;
    protected Pieces piece;

    public Move(Color color, Pieces piece, int fromRank, byte fromFile, int toRank, byte toFile, boolean isCapture) {
        this.fromFile = fromFile;
        this.fromRank = fromRank;
        this.toFile = toFile;
        this.toRank = toRank;
        this.isCapture = isCapture;
        this.color = color;
        this.piece = piece;
        this.isKingInCheck = false;
        this.isCheckmate = false;
    }

    public Color getColor() {
        return color;
    }

    public Pieces getPiece() {
        return piece;
    }

    public void setIsKingInCheck(boolean isKingInCheck) {
        this.isKingInCheck = isKingInCheck;
    }

    public boolean isKingInCheck() {
        return isKingInCheck;
    }

    public void setIsCheckmate(boolean isCheckmate) {
        this.isCheckmate = isCheckmate;
    }

    public byte getFromFile() {
        return fromFile;
    }

    public int getFromRank() {
        return fromRank;
    }

    public byte getToFile() {
        return toFile;
    }

    public int getToRank() {
        return toRank;
    }

    public boolean isCapture() {
        return isCapture;
    }

    public boolean isOpposite(Move move) {
        return move.getFromRank() == this.getToRank() &&
            move.getToFile() == this.getFromFile() &&
            move.getToRank() == this.getFromRank() &&
            move.getFromFile() == this.getToFile();
    }

    public boolean equals(Move move) {
        return move.getFromRank() == this.getFromRank() &&
            move.getToFile() == this.getToFile() &&
            move.getToRank() == this.getToRank() &&
            move.getFromFile() == this.getFromFile();
    }

    protected static String rankToString(int rank) {
        return String.valueOf(rank + 1);
    }

    protected static String fileToString(byte file) {
        return String.valueOf((char)('a' + file));
    }

    public static String squareToString(int rank, byte file) {
        return fileToString(file) + rankToString(rank);
    }

    public static Move uciStringToMove(String moveString, Board board) {
        String[] moveTokens = moveString.split("");

        byte fromFile = (byte) (moveTokens[0].charAt(0) - 'a');
        int fromRank = Integer.parseInt(moveTokens[1]) - 1;
        byte toFile = (byte) (moveTokens[2].charAt(0) - 'a');
        int toRank = Integer.parseInt(moveTokens[3]) - 1;

        Pieces boardPiece = board.getBoardRow(fromRank).getPiece(fromFile);
        Color boardColor = board.getBoardRow(fromRank).getColor(fromFile);
        Color color = board.getTurn();

        if(boardPiece == Pieces.NONE || boardColor != color) {
            return null;
        }
        
        List<Move> possibleMoves = board.getLegalMoves(fromRank, fromFile);
        Move move = possibleMoves.stream().filter(m -> m.getToFile() == toFile && m.getToRank() == toRank).findFirst().orElse(null);
        return move;
    }

    public String toString(Pieces piece) {
        return piece.getSymbol() + 
            squareToString(fromRank, fromFile) +
            (isCapture ?  "x" : "") + 
            squareToString(toRank, toFile) + 
            (isCheckmate ? "#" : isKingInCheck ? "+" : "");
    }

    public static byte makeMove(BoardRow[] board, Move move) {
        byte fromRank = (byte) move.getFromRank();
        byte fromFile = (byte) move.getFromFile();
        byte toRank = (byte) move.getToRank();
        byte toFile = (byte) move.getToFile();
        
        byte capturedPiece = board[toRank].getFile(toFile);
        board[toRank].setFile(
            toFile,
            board[fromRank].getColor(fromFile),
            board[fromRank].getPiece(fromFile)
        );
        board[fromRank].setFile(
            fromFile,
            (byte) 0
        );

        if(move instanceof KingMove && ((KingMove) move).isCastlingMove()) {
            byte rookFileIndex = (byte) ((KingMove) move).rookFileForCastle();
            byte afterCastleRookFileIndex = (byte) ((KingMove) move).moveRookToFileAfterCastle();

            board[toRank].setFile(
                afterCastleRookFileIndex,
                board[fromRank].getColor(rookFileIndex),
                board[fromRank].getPiece(rookFileIndex)
            );
            board[fromRank].setFile(
                rookFileIndex,
                (byte) 0
            );
        } else if (move instanceof PawnMove && ((PawnMove) move).isEnPassant()) {
            board[fromRank].setFile(
                toFile,
                (byte) 0
            );
        } else if (move instanceof PawnMove && ((PawnMove) move).isPromotion()) {
            board[toRank].setFile(
                toFile,
                board[toRank].getColor(toFile), 
                ((PawnMove) move).getPromotedPiece()
            );
        }

        return capturedPiece;
    }

    public static void undoMove(BoardRow[] board, Move move, byte capturedPiece) {
        byte fromRank = (byte) move.getFromRank();
        byte fromFile = (byte) move.getFromFile();
        byte toRank = (byte) move.getToRank();
        byte toFile = (byte) move.getToFile();
        
        board[fromRank].setFile(
            fromFile,
            board[toRank].getColor(toFile),
            board[toRank].getPiece(toFile)
        );
        board[toRank].setFile(
            toFile,
            capturedPiece
        );

        Color color = move.getColor();
                
        if(move instanceof KingMove && ((KingMove) move).isCastlingMove()) {
            byte rookFileIndex = (byte) ((KingMove) move).rookFileForCastle();
            byte afterCastleRookFileIndex = (byte) ((KingMove) move).moveRookToFileAfterCastle();

            board[fromRank].setFile(
                rookFileIndex,
                board[toRank].getColor(afterCastleRookFileIndex),
                board[toRank].getPiece(afterCastleRookFileIndex)
            );
            board[toRank].setFile(
                afterCastleRookFileIndex,
                (byte) 0
            );
        } else if (move instanceof PawnMove && ((PawnMove) move).isEnPassant()) {
            board[fromRank].setFile(
                toFile,
                color.opposite(),
                Pieces.PAWN
            );
        } else if (move instanceof PawnMove && ((PawnMove) move).isPromotion()) {
            board[fromRank].setFile(
                fromFile,
                color,
                Pieces.PAWN
            );
        }
    }

}
