package com.example.chess.model;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.utils.Utilities;

public class KingMove extends Move {
    private final int castleFile;
    private static final int kingSideCastleFile = 6;
    private static final int queenSideCastleFile = 2;

    public KingMove(Color color, int fromRank, byte fromFile, int toRank, byte toFile, boolean isCapture, int castleFile) {
        super(color, Pieces.KING, fromRank, fromFile, toRank, toFile, isCapture);
        this.castleFile = castleFile;
    }

    public KingMove(Color color, int fromRank, byte fromFile, int toRank, byte toFile, boolean isCapture) {
        super(color, Pieces.KING, fromRank, fromFile, toRank, toFile, isCapture);
        this.castleFile = -1;
    }

    public boolean isCastlingMove() {
        return castleFile != -1;
    }

    public int rookFileForCastle() {
        if(castleFile == queenSideCastleFile) {
            return 0;
        } else if(castleFile == kingSideCastleFile) {
            return 7;
        }
        return -1;
    }

    public int moveRookToFileAfterCastle() {
        if(castleFile == queenSideCastleFile) {
            return queenSideCastleFile + 1;
        } else if(castleFile == kingSideCastleFile) {
            return kingSideCastleFile - 1;
        }
        return -1;
    }

    public static List<KingMove> getPossibleMoves(BoardRow[] board, int rank, byte file) {
        List<KingMove> moves = new ArrayList<>();
        Color color = board[rank].getColor(file);
        int[][] directions = Pieces.KING.getDirections(color);

        for(int i = 0; i < 8; i++) {
            int newRank = rank + directions[i][0];
            byte newFile = (byte) (file + directions[i][1]);
            boolean isCapture = Utilities.isOnBoard(newRank, newFile) 
                && !board[newRank].isEmpty(newFile) 
                && board[newRank].getColor(newFile) == color.opposite();

            if(Utilities.isOnBoard(newRank, newFile) && (board[newRank].isEmpty(newFile) || isCapture)) {
                moves.add(new KingMove(color, rank, file, newRank, newFile, isCapture));
            }
        }

        if(Board.isKingSideCastleAvailable(color)) {
            moves.add(new KingMove(color, rank, file, rank, (byte) 6, false, kingSideCastleFile));
        }
        if(Board.isQueenSideCastleAvailable(color)) {
            moves.add(new KingMove(color, rank, file, rank, (byte) 2, false, queenSideCastleFile));
        }

        return moves;
    }

    @Override
    public String toString() {
        return (
            castleFile == queenSideCastleFile ? 
                "O-O-O" : 
                (
                    castleFile == kingSideCastleFile ? 
                        "O-O" : 
                        super.toString(Pieces.KING)
                )
        ) + (isCheckmate ? "#" : isKingInCheck ? "+" : "");
    }
}
