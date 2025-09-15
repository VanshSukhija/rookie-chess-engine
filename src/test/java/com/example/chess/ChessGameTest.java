package com.example.chess;

import com.example.chess.model.*;
import com.example.chess.neuralnetwork.NeuralNetwork;
import com.example.chess.utils.Constants;

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
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";
        Board boardFromFen = setUp(fen, Color.WHITE);
        Board board = setUp(Color.WHITE);

        for(int i=0; i<8; i++) {
            assertEquals(board.getBoardRow(i).getRow(), boardFromFen.getBoardRow(i).getRow());
        }

        assertEquals(board.getFEN(), fen);
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

    @Test
    public void predictionTest() {
        NeuralNetwork nn = new NeuralNetwork("chess_nn_model.json");
        String fen = "r1b2bnr/pp1p1kpp/2p5/8/8/2N1P3/PPP1P1PP/R2QKB1R b KQ -";
        double prediction = nn.evaluate(fen);
        System.out.println(prediction);
    }

    @Test
    public void bestMoveTest() {
        NeuralNetwork nn = new NeuralNetwork("chess_nn_model.json");
        String fen = "2Q5/8/3R4/7P/4k3/B5K1/7r/8 b - -";
        Board board = setUp(fen, Color.BLACK);
        int depth = board.getDepthExtensionWithPhase(Constants.MAX_DEPTH_TO_SEARCH);
        double cp = board.minimax(
            depth,
            nn,
            board.getTurn() == Color.WHITE,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY
        );
        Move bestMove = board.getBestMove();
        System.out.println(bestMove);
        System.out.println(cp);
        System.out.println(depth);
    }
}
