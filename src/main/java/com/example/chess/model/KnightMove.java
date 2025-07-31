package com.example.chess.model;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.utils.Utilities;

public class KnightMove extends Move {
    public KnightMove(Color color, int fromRank, byte fromFile, int toRank, byte toFile, boolean isCapture) {
        super(color, Pieces.KNIGHT, fromRank, fromFile, toRank, toFile, isCapture);
    }

    public static List<KnightMove> getPossibleMoves(BoardRow[] board, int rank, byte file) {
        List<KnightMove> moves = new ArrayList<>();
        Color color = board[rank].getColor(file);
        int[][] direction = new int[][]{
            {2, 1}, {2, -1}, {1, 2}, {1, -2},
            {-2, 1}, {-2, -1}, {-1, 2}, {-1, -2}
        };

        for(int i = 0; i < 8; i++) {
            int newRank = rank + direction[i][0];
            byte newFile = (byte) (file + direction[i][1]);
            boolean isCapture = Utilities.isOnBoard(newRank, newFile) && !board[newRank].isEmpty(newFile) && board[newRank].getColor(newFile) == color.opposite();
            if(Utilities.isOnBoard(newRank, newFile) && (board[newRank].isEmpty(newFile) || isCapture)) {
                moves.add(new KnightMove(color, rank, file, newRank, newFile, isCapture));
            }
        }

        return moves;
    }

    @Override
    public String toString() {
        return super.toString(Pieces.KNIGHT);
    }
}
