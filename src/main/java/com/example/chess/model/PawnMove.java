package com.example.chess.model;

import java.util.*;
import com.example.chess.utils.Utilities;

public class PawnMove extends Move {
    private final Pieces promotion;
    private final boolean isEnPassant;

    public PawnMove(Color color, int fromRank, byte fromFile, int toRank, byte toFile, boolean isCapture, Pieces promotion, boolean isEnPassant) {
        super(color, Pieces.PAWN, fromRank, fromFile, toRank, toFile, isCapture);
        this.promotion = promotion;
        this.isEnPassant = isEnPassant;
    }

    public PawnMove(Color color, int fromRank, byte fromFile, int toRank, byte toFile, boolean isCapture) {
        this(color, fromRank, fromFile, toRank, toFile, isCapture, Pieces.NONE, false);
    }

    public Pieces getPromotedPiece() {
        return promotion;
    }

    public boolean isPromotion() {
        return promotion == Pieces.BISHOP || promotion == Pieces.KNIGHT || promotion == Pieces.ROOK || promotion == Pieces.QUEEN;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public static boolean isMoveEnPassant(BoardRow[] board, List<Move> moveHistory, int fromRank, byte fromFile, int toRank, byte toFile) {
        Color color = board[fromRank].getColor(fromFile);
        int rankOffset = color == Color.WHITE ? 1 : -1;

        return Utilities.isOnBoard(fromRank, toFile) &&
            board[fromRank].getPiece(toFile) == Pieces.PAWN &&
            board[fromRank].getColor(toFile) == color.opposite() &&
            moveHistory.size() > 0 &&
            moveHistory.getLast().getFromFile() == toFile &&
            moveHistory.getLast().getToFile() == toFile &&
            moveHistory.getLast().getFromRank() == fromRank + 2 * rankOffset &&
            moveHistory.getLast().getToRank() == fromRank;
    }

    @Override
    public String toString() {
        return 
            (isCapture ? fileToString(fromFile) + "x" : "") + 
            squareToString(toRank, toFile) + 
            (promotion == Pieces.NONE ? "" : "=" + promotion.getSymbol()) + 
            (isCheckmate ? "#" : isKingInCheck ? "+" : "")
        ;
    }

    public static List<PawnMove> getPossibleMoves(BoardRow[] board, List<Move> moveHistory, int rank, byte file, boolean isEnPassantValid) {
        List<PawnMove> moves = new ArrayList<>();
        int[] direction = new int[]{1, -1};
        Color color = board[rank].getColor(file);
        int rankOffset = color == Color.WHITE ? 1 : -1;
        int firstRank = color == Color.WHITE ? 1 : 6;
        int lastRank = color == Color.WHITE ? 7 : 0;
        boolean wasFirstRank = rank == firstRank;

        if(Utilities.isOnBoard(rank + rankOffset, file) && board[rank + rankOffset].isEmpty(file)) {
            if(rank + rankOffset == lastRank) {
                moves.add(new PawnMove(color, rank, file, rank + rankOffset, file, false, Pieces.QUEEN, false));
                moves.add(new PawnMove(color, rank, file, rank + rankOffset, file, false, Pieces.BISHOP, false));
                moves.add(new PawnMove(color, rank, file, rank + rankOffset, file, false, Pieces.ROOK, false));
                moves.add(new PawnMove(color, rank, file, rank + rankOffset, file, false, Pieces.KNIGHT, false));
            } else {
                moves.add(new PawnMove(color, rank, file, rank + rankOffset, file, false));
            }
        }

        if(
            wasFirstRank &&
            Utilities.isOnBoard(rank + 2 * rankOffset, file) &&
            board[rank + 2 * rankOffset].isEmpty(file) &&
            board[rank + rankOffset].isEmpty(file)
        ) {
            moves.add(new PawnMove(color, rank, file, rank + 2 * rankOffset, file, false));
        }

        for(int i = 0; i < 2; i++) {
            int newRank = rank + rankOffset;
            byte newFile = (byte) (file + direction[i]);
            
            if (Utilities.isOnBoard(newRank, newFile) && !board[newRank].isEmpty(newFile) && board[newRank].getColor(newFile) == color.opposite()) {
                if (newRank == lastRank) {
                    moves.add(new PawnMove(color, rank, file, newRank, newFile, true, Pieces.QUEEN, false));
                    moves.add(new PawnMove(color, rank, file, newRank, newFile, true, Pieces.BISHOP, false));
                    moves.add(new PawnMove(color, rank, file, newRank, newFile, true, Pieces.ROOK, false));
                    moves.add(new PawnMove(color, rank, file, newRank, newFile, true, Pieces.KNIGHT, false));
                } else {
                    moves.add(new PawnMove(color, rank, file, newRank, newFile, true));
                }
            }
            if (
                isEnPassantValid &&
                Utilities.isOnBoard(rank, newFile) &&
                board[rank].getPiece(newFile) == Pieces.PAWN &&
                board[rank].getColor(newFile) == color.opposite() &&
                moveHistory.size() > 0 &&
                moveHistory.getLast().getFromFile() == newFile &&
                moveHistory.getLast().getToFile() == newFile &&
                moveHistory.getLast().getFromRank() == rank + 2 * rankOffset &&
                moveHistory.getLast().getToRank() == rank
            ) {
                moves.add(new PawnMove(color, rank, file, newRank, newFile, true, Pieces.NONE, true));    
            }
        }

        return moves;
    }
}
