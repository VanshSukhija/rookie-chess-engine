package com.example.chess.neuralnetwork;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;

public class NeuralNetwork {
    private static final int INPUT_SIZE = 8 * 8 * 12 + 1 + 4 + 8; // 768 + 1 + 4 + 8 = 781
    private static final int HIDDEN_LAYER_1_SIZE = 256; // Size of the hidden layer 1
    private static final int HIDDEN_LAYER_2_SIZE = 64; // Size of the hidden layer 2
    private static final HashMap<Character, Integer> pieceTypes = new HashMap<>() {{
        put('P', 0);
        put('N', 1);
        put('B', 2);
        put('R', 3);
        put('Q', 4);
        put('K', 5);
        put('p', 6);
        put('n', 7);
        put('b', 8);
        put('r', 9);
        put('q', 10);
        put('k', 11);
    }};

    private static double[][] WEIGHTS_INPUT_HIDDEN_1;
    private static double[][] WEIGHTS_HIDDEN_1_HIDDEN_2;
    private static double[][] WEIGHTS_HIDDEN_2_OUTPUT;

    private static double[] BIAS_HIDDEN_1;
    private static double[] BIAS_HIDDEN_2;
    private static double[] BIAS_OUTPUT;

    public NeuralNetwork(String filePath) {
        // Load weights and biases from the specified file
        loadWeightsAndBiases(filePath);
    }

    private void loadWeightsAndBiases(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            Gson gson = new Gson();

            WEIGHTS_INPUT_HIDDEN_1 = gson.fromJson(jsonArray.get(0), double[][].class);
            BIAS_HIDDEN_1 = gson.fromJson(jsonArray.get(1), double[].class);
            WEIGHTS_HIDDEN_1_HIDDEN_2 = gson.fromJson(jsonArray.get(2), double[][].class);
            BIAS_HIDDEN_2 = gson.fromJson(jsonArray.get(3), double[].class);
            WEIGHTS_HIDDEN_2_OUTPUT = gson.fromJson(jsonArray.get(4), double[][].class);
            BIAS_OUTPUT = gson.fromJson(jsonArray.get(5), double[].class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double evaluate(String fen) {
        return predict(encodeFen(fen));
    }

    private double[] encodeFen(String fen) {
        // 1. Piece planes: planes[piece][rank][file]
        double[][][] planes = new double[12][8][8];
        String[] parts = fen.split(" ");
        String[] boardRows = parts[0].split("/");

        for (int r = 0; r < 8; r++) {
            String row = boardRows[r];
            int f = 0;
            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    f += Character.getNumericValue(c);
                } else if (pieceTypes.containsKey(c)) {
                    int idx = pieceTypes.get(c);
                    planes[idx][r][f] = 1.0;
                    f++;
                }
            }
        }

        // Flatten planes to 768-length vector
        double[] flat = new double[12 * 8 * 8];
        int k = 0;
        for (int p = 0; p < 12; p++) {
            for (int r = 0; r < 8; r++) {
                for (int f = 0; f < 8; f++) {
                    flat[k++] = planes[p][r][f];
                }
            }
        }

        // 2. Side to move
        double stm = parts[1].equals("w") ? 1.0 : 0.0;

        // 3. Castling rights
        double[] castling = new double[4];
        String castlingStr = parts[2];
        String castleOrder = "KQkq";
        for (int i = 0; i < 4; i++) {
            castling[i] = castlingStr.indexOf(castleOrder.charAt(i)) != -1 ? 1.0 : 0.0;
        }

        // 4. En passant
        double[] ep = new double[8];
        if (!parts[3].equals("-")) {
            int file = parts[3].charAt(0) - 'a';
            ep[file] = 1.0;
        }

        // 5. Concatenate all features
        double[] features = new double[flat.length + 1 + 4 + 8];
        System.arraycopy(flat, 0, features, 0, flat.length);
        features[flat.length] = stm;
        System.arraycopy(castling, 0, features, flat.length + 1, 4);
        System.arraycopy(ep, 0, features, flat.length + 1 + 4, 8);

        return features;
    }

    private double predict(double[] input) {
        // Hidden Layer 1 (ReLU)
        double[] hidden1 = new double[HIDDEN_LAYER_1_SIZE];
        for (int i = 0; i < HIDDEN_LAYER_1_SIZE; i++) {
            double sum = BIAS_HIDDEN_1[i];
            for (int j = 0; j < INPUT_SIZE; j++) {
                sum += input[j] * WEIGHTS_INPUT_HIDDEN_1[j][i];
            }
            hidden1[i] = relu(sum);
        }

        // Hidden Layer 2 (ReLU)
        double[] hidden2 = new double[HIDDEN_LAYER_2_SIZE];
        for (int i = 0; i < HIDDEN_LAYER_2_SIZE; i++) {
            double sum = BIAS_HIDDEN_2[i];
            for (int j = 0; j < HIDDEN_LAYER_1_SIZE; j++) {
                sum += hidden1[j] * WEIGHTS_HIDDEN_1_HIDDEN_2[j][i];
            }
            hidden2[i] = relu(sum);
        }

        // Output Layer (Linear)
        double output = BIAS_OUTPUT[0];
        for (int i = 0; i < HIDDEN_LAYER_2_SIZE; i++) {
            output += hidden2[i] * WEIGHTS_HIDDEN_2_OUTPUT[i][0];
        }

        return output;
    }

    private double relu(double x) {
        return Math.max(0, x);
    }

    public void printEncodedFen(String fen) {
        double[] encodedFen = encodeFen(fen);
        System.out.print(Arrays.toString(encodedFen));
    }
}
