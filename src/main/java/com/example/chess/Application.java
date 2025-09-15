package com.example.chess;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.example.chess.model.Board;
import com.example.chess.model.Color;
import com.example.chess.model.GameResult;
import com.example.chess.model.Move;
import com.example.chess.neuralnetwork.NeuralNetwork;
import com.example.chess.utils.Constants;

public class Application {
    public static void main(String[] args) {
        PrintStream originalOut = System.out;
        Scanner scanner = new Scanner(System.in);

        int random = (int) (Math.random() * 100);
        Color randomColor = random % 2 == 0 ? Color.WHITE : Color.BLACK;
        System.out.println("Max Depth to Search: " + Constants.MAX_DEPTH_TO_SEARCH);
        System.out.println("Engine_1: " + randomColor);
        System.out.println("Engine_2: " + randomColor.opposite());

        Board board = new Board(randomColor);
        NeuralNetwork neuralNetwork = new NeuralNetwork("chess_nn_model.json");

        List<Double> playerTimes = new ArrayList<>();
        List<Double> opponentTimes = new ArrayList<>();

        try {
            File outputFile = new File("output.txt");
            PrintStream output = new PrintStream(outputFile);
            System.setOut(output);

            int moveNumber = 0;
            double lastEval = 0;
            while (board.getWinner() == GameResult.ONGOING) {
                if (board.getTurn() == randomColor) {
                    int depth = Constants.MAX_DEPTH_TO_SEARCH + Math.abs((int) Math.floor(lastEval * 3));
                    long startTime = System.currentTimeMillis();
                    double cp = board.minimax(
                        depth,
                        neuralNetwork,
                        board.getTurn() == Color.WHITE,
                        Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY
                    );
                    long endTime = System.currentTimeMillis();
                    playerTimes.add((endTime - startTime) / 1000.0);

                    Move move = board.getBestMove();

                    if (move == null) {
                        if (cp == 0.0) {
                            System.out.println("Stalemate!");
                            board.setWinner(GameResult.DRAW);
                        } else {
                            System.out.println("Checkmate!");
                            board.setWinner(
                                    board.getTurn() == Color.WHITE ? GameResult.BLACK_WIN : GameResult.WHITE_WIN);
                        }
                        break;
                    }

                    board.makeMove(move);

                    System.out.println();
                    if (board.getTurn().opposite() == Color.WHITE) {
                        System.out.println(++moveNumber + ". ");
                    }
                    String moveSquareString = Move.squareToString(move.getFromRank(), move.getFromFile()) + " - "
                            + Move.squareToString(move.getToRank(), move.getToFile());
                    System.out.println(moveSquareString + " (CP: " + cp + ")" + " [Time: "
                            + (endTime - startTime) / 1000.0 + "s," + " Depth: " + depth + "] FEN: " + board.getFEN());

                    for (int i = 7; i >= 0; i--) {
                        for (byte j = 0; j < 8; j++) {
                            System.out.print(board.getBoardRow(i).printFile(j) + " ");
                        }
                        System.out.println();
                    }
                } else {
                    int depth = board.getDepthExtensionWithPhase(Constants.MAX_DEPTH_TO_SEARCH);
                    long startTime = System.currentTimeMillis();
                    double cp = board.minimax(
                        depth,
                        neuralNetwork,
                        board.getTurn() == Color.WHITE,
                        Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY
                    );
                    long endTime = System.currentTimeMillis();
                    lastEval = cp;
                    opponentTimes.add((endTime - startTime) / 1000.0);

                    Move move = board.getBestMove();

                    if (move == null) {
                        if (cp == 0.0) {
                            System.out.println("Stalemate!");
                            board.setWinner(GameResult.DRAW);
                        } else {
                            System.out.println("Checkmate!");
                            board.setWinner(
                                    board.getTurn() == Color.WHITE ? GameResult.BLACK_WIN : GameResult.WHITE_WIN);
                        }
                        break;
                    }

                    board.makeMove(move);

                    System.out.println();
                    if (board.getTurn().opposite() == Color.WHITE) {
                        System.out.println(++moveNumber + ". ");
                    }
                    String moveSquareString = Move.squareToString(move.getFromRank(), move.getFromFile()) + " - "
                            + Move.squareToString(move.getToRank(), move.getToFile());
                    System.out.println(moveSquareString + " (CP: " + cp + ")" + " [Time: "
                            + (endTime - startTime) / 1000.0 + "s," + " Depth: " + depth + "] FEN: " + board.getFEN());

                    for (int i = 7; i >= 0; i--) {
                        for (byte j = 0; j < 8; j++) {
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
            System.out.println("(Player) Average time per move: "
                    + (playerTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)) + "s");
            System.out.println("(Player) Maximum time for a move: "
                    + (playerTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0)) + "s");
            System.out.println(
                    "(Player) Median time for a move: " + (playerTimes.stream().mapToDouble(Double::doubleValue)
                            .sorted().skip(playerTimes.size() / 2).findFirst().orElse(0.0)) + "s");
            System.out.println("(Opponent) Average time per move: "
                    + (opponentTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)) + "s");
            System.out.println("(Opponent) Maximum time for a move: "
                    + (opponentTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0)) + "s");
            System.out.println(
                    "(Opponent) Median time for a move: " + (opponentTimes.stream().mapToDouble(Double::doubleValue)
                            .sorted().skip(opponentTimes.size() / 2).findFirst().orElse(0.0)) + "s");
            System.out.println("Result: " + board.getWinner().toString());
            board.printPGN();
            scanner.close();
        }
    }
}
