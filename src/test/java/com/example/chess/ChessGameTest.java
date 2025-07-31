package com.example.chess;

import com.example.chess.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ChessGameTest {
    private Board setUp(Color color) {
        Board board = new Board(color);
        return board;
    }

    private Board setUp(String fen, Color color) {
        Board board = new Board(fen, color);
        return board;
    }

    private long countLegalMoves(Board board, int maxDepth, int currentDepth) {
        if(currentDepth == maxDepth) {
            return 0;
        }
        
        long count = 0;

        List<Move> legalMoves = board.getAllLegalMoves();
        for(Move move : legalMoves) {
            byte capturedPiece = board.makeMove(move);
            count += (currentDepth == maxDepth - 1 ? 1 : 0) + countLegalMoves(board, maxDepth, currentDepth + 1);
            board.undoMove(move, capturedPiece);
        }

        return count;
    }

    @Test
    public void testBoardFen() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        Board boardFromFen = setUp(fen, Color.WHITE);
        Board board = setUp(Color.WHITE);

        for(int i=0; i<8; i++) {
            assertEquals(board.getBoardRow(i).getRow(), boardFromFen.getBoardRow(i).getRow());
        }

        assertEquals(board.getFEN(), fen.split(" ")[0]);
    }

    @Test
    public void countMovesAtDepth1() {
        Board board = setUp(Color.WHITE);
        assertEquals(20, countLegalMoves(board, 1, 0));
    }

    @Test
    public void countMovesAtDepth2() {
        Board board = setUp(Color.WHITE);
        assertEquals(400, countLegalMoves(board, 2, 0));
    }

    @Test
    public void countMovesAtDepth3() {
        Board board = setUp(Color.WHITE);
        assertEquals(8902, countLegalMoves(board, 3, 0));
    }

    @Test
    public void countMovesAtDepth4() {
        Board board = setUp(Color.WHITE);
        assertEquals(197281, countLegalMoves(board, 4, 0));
    }

    @Test
    public void countMovesAtDepth5() {
        Board board = setUp(Color.WHITE);
        assertEquals(4865609, countLegalMoves(board, 5, 0));
    }

    @Test
    public void countMovesAtDepth6() {
        Board board = setUp(Color.WHITE);
        assertEquals(119060324, countLegalMoves(board, 6, 0));
    }
}
