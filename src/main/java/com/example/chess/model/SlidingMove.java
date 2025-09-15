package com.example.chess.model;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.utils.Utilities;

public class SlidingMove extends Move {
    public SlidingMove(Color color, int fromRank, byte fromFile, int toRank, byte toFile, boolean isCapture, Pieces pieces) {
        super(color, pieces, fromRank, fromFile, toRank, toFile, isCapture);
    }

    public static List<SlidingMove> getPossibleMoves(Board board, int rank, byte file, Pieces piece) {
        List<SlidingMove> moves = new ArrayList<>();
        Color color = board.getBoardRow(rank).getColor(file);
        int[][] direction = piece.getDirections(color);

        for(int i = 0; i < direction.length; i++) {
            for(int j = 1; j < 8; j++) {
                int newRank = rank + j * direction[i][0];
                byte newFile = (byte) (file + j * direction[i][1]);
                boolean isCapture = 
                    Utilities.isOnBoard(newRank, newFile) &&
                    !board.getBoardRow(newRank).isEmpty(newFile) &&
                    board.getBoardRow(newRank).getColor(newFile) == color.opposite();

                if(Utilities.isOnBoard(newRank, newFile) && board.getBoardRow(newRank).isEmpty(newFile)) {
                    moves.add(new SlidingMove(color, rank, file, newRank, newFile, isCapture, piece));
                } else if (Utilities.isOnBoard(newRank, newFile) && isCapture) {
                    moves.add(new SlidingMove(color, rank, file, newRank, newFile, isCapture, piece));
                    break;
                } else {
                    break;
                }
            }
        }

        return moves;
    }

    @Override
    public String toString(Pieces piece) {
        return super.toString(piece);
    }
}
