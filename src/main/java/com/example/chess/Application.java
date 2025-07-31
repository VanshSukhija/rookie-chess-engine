package com.example.chess;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import com.example.chess.model.Board;
import com.example.chess.model.Color;
import com.example.chess.model.GameResult;
import com.example.chess.model.Move;

public class Application {
    public static void main(String[] args) {
        PrintStream originalOut = System.out;

        int random = (int) (Math.random() * 100);
        Color playerColor = random % 2 == 0 ? Color.WHITE : Color.BLACK;
        System.out.println("Player Color: " + playerColor);

        Board board = new Board(playerColor);

        try {
            File outputFile = new File("output.txt");
            System.setOut(new PrintStream(outputFile));

            int moveNumber = 0;
            while(board.getWinner() == GameResult.ONGOING) {
                if(board.getTurn() != playerColor) {
                    List<Move> moves = board.getAllLegalMoves();

                    if(moves.size() == 0) {
                        if(board.isKingInCheck(board.getTurn())) {
                            System.out.println("Checkmate!");
                            board.setWinner(board.getTurn() == Color.WHITE ? GameResult.BLACK_WIN : GameResult.WHITE_WIN);
                        } else {
                            System.out.println("Stalemate!");
                            board.setWinner(GameResult.DRAW);
                        }
                        break;
                    }

                    Move move = moves.get((int) (Math.random() * moves.size()));
                    board.makeMove(move);

                    System.out.println();
                    if(board.getTurn().opposite() == Color.WHITE) {
                        System.out.println(++moveNumber + ". ");
                    }
                    String moveSquareString = Move.squareToString(move.getFromRank(), move.getFromFile()) + " - " + Move.squareToString(move.getToRank(), move.getToFile());
                    System.out.println(moveSquareString);
                    
                    for(int i=7; i>=0; i--){
                        for(byte j=0; j<8; j++) {
                            System.out.print(board.getBoardRow(i).printFile(j) + " ");
                        }
                        System.out.println();
                    }
                } else {
                    List<Move> moves = board.getAllLegalMoves();

                    if(moves.size() == 0) {
                        if(board.isKingInCheck(board.getTurn())) {
                            System.out.println("Checkmate!");
                            board.setWinner(board.getTurn() == Color.WHITE ? GameResult.BLACK_WIN : GameResult.WHITE_WIN);
                        } else {
                            System.out.println("Stalemate!");
                            board.setWinner(GameResult.DRAW);
                        }
                        break;
                    }

                    Move move = moves.get((int) (Math.random() * moves.size()));
                    board.makeMove(move);

                    System.out.println();
                    if(board.getTurn().opposite() == Color.WHITE) {
                        System.out.println(++moveNumber + ". ");
                    }
                    String moveSquareString = Move.squareToString(move.getFromRank(), move.getFromFile()) + " - " + Move.squareToString(move.getToRank(), move.getToFile());
                    System.out.println(moveSquareString);
                    
                    for(int i=7; i>=0; i--){
                        for(byte j=0; j<8; j++) {
                            System.out.print(board.getBoardRow(i).printFile(j) + " ");
                        }
                        System.out.println();
                    }
                }

                if (board.isThreeFoldRepetition()) {
                    board.setWinner(GameResult.DRAW);
                    System.out.println("Threefold Repetition");
                    break;
                } else if (board.onlyKingsLeftOnBoard()) {
                    board.setWinner(GameResult.DRAW);
                    System.out.println("Only Kings Left on Board");
                    break;
                } else if (board.fiftyMoveRule()) {
                    board.setWinner(GameResult.DRAW);
                    System.out.println("Fifty Move Rule");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.setOut(originalOut);
            System.out.println("Winner: " + board.getWinner().toString());
            board.printMoveHistory();
        }
    }    
}
