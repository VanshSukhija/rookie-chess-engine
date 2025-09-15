package com.example.chess.model;

import java.util.*;

import com.example.chess.neuralnetwork.NeuralNetwork;
import com.example.chess.utils.Utilities;

public class Board {
    /*
     * color - 1 bit
     * piece type - 3 bits
     * square representation - ....|....|....|....|....|....|....|.... => color-piece
     * board representation - array of 8 32-bit integers for each rank
     */

    private BoardRow[] board;
    private List<Move> moveHistory;
    private Color turn = Color.WHITE;
    private final Color playerColor;
    private GameResult winner;
    private int whiteKingFirstMove = -1;
    private int blackKingFirstMove = -1;
    private int whiteRookOnFileZeroFirstMove = -1;
    private int whiteRookOnFileSevenFirstMove = -1;
    private int blackRookOnFileZeroFirstMove = -1;
    private int blackRookOnFileSevenFirstMove = -1;
    private HashMap<String, Integer> positionCountMap;
    private HashMap<String, TTEntry> transpositionTable;

    public Board(Color playerColor) {
        board = new BoardRow[8];
        positionCountMap = new HashMap<>();
        moveHistory = new ArrayList<>();
        this.playerColor = playerColor;
        this.winner = GameResult.ONGOING;
        this.transpositionTable = new HashMap<>();
        initializeStartingPosition();
    }

    public Board(String fen, Color playerColor) {
        board = new BoardRow[8];
        positionCountMap = new HashMap<>();
        moveHistory = new ArrayList<>();
        this.playerColor = playerColor;
        this.winner = GameResult.ONGOING;
        this.transpositionTable = new HashMap<>();
        initializeFromFEN(fen);
    }

    public BoardRow[] getBoardRows() {
        return board;
    }

    public String getFEN() {
        String rows = "";
        for(int i=7; i >= 0; i--) {
            rows += getBoardRow(i).getFen();
            if(i != 0) {
                rows += "/";
            }
        }

        // side to move
        String sideToMove = turn == Color.WHITE ? "w" : "b";

        // castling availability
        String castling = "";
        if(whiteKingFirstMove == -1 && whiteRookOnFileSevenFirstMove == -1 && board[0].getPiece((byte)7) == Pieces.ROOK && board[0].getColor((byte)7) == Color.WHITE) {
            castling += "K";
        }
        if(whiteKingFirstMove == -1 && whiteRookOnFileZeroFirstMove == -1 && board[0].getPiece((byte)0) == Pieces.ROOK && board[0].getColor((byte)0) == Color.WHITE) {
            castling += "Q";
        }
        if(blackKingFirstMove == -1 && blackRookOnFileSevenFirstMove == -1 && board[7].getPiece((byte)7) == Pieces.ROOK && board[7].getColor((byte)7) == Color.BLACK) {
            castling += "k";
        }
        if(blackKingFirstMove == -1 && blackRookOnFileZeroFirstMove == -1 && board[7].getPiece((byte)0) == Pieces.ROOK && board[7].getColor((byte)0) == Color.BLACK) {
            castling += "q";
        }
        if(castling.equals("")) {
            castling = "-";
        }

        // en passant target square
        String enPassantAvailable = "-";
        if(moveHistory.size() > 0) {
            Move lastMove = moveHistory.get(moveHistory.size() - 1);
            if(lastMove instanceof PawnMove && Math.abs(lastMove.fromRank - lastMove.toRank) == 2) {
                char file = (char) ('a' + lastMove.fromFile);
                char rank = (char) ('1' + (lastMove.fromRank + lastMove.toRank) / 2);
                enPassantAvailable = "" + file + rank;
            }
        }

        return String.join(" ", rows, sideToMove, castling, enPassantAvailable);
    }

    public BoardRow getBoardRow(int rowIndex) {
        return board[rowIndex];
    }

    private void initializeFromFEN(String fen) {
        String[] fenParts = fen.split(" ");
        String[] ranks = fenParts[0].split("/");

        for (int i = 0; i < ranks.length; i++) {
            board[7 - i] = new BoardRow(0);
            int fileIndex = 0;
            for (int j = 0; j < ranks[i].length(); j++) {
                char c = ranks[i].charAt(j);
                if (Character.isDigit(c)) {
                    int emptySquares = Character.getNumericValue(c);
                    for (int k = 0; k < emptySquares; k++) {
                        board[7 - i].setFile((byte) fileIndex, Color.BLACK, Pieces.NONE);
                        fileIndex++;
                    }
                } else if (Character.isLowerCase(c)) {
                    board[7 - i].setFile((byte) fileIndex, Color.BLACK, Utilities.getPieceFromSymbol(Character.toUpperCase(c)));
                    fileIndex++;
                } else if (Character.isUpperCase(c)) {
                    board[7 - i].setFile((byte) fileIndex, Color.WHITE, Utilities.getPieceFromSymbol(c));
                    fileIndex++;
                }
            }
        }

        turn = fenParts[1].equals("w") ? Color.WHITE : Color.BLACK;

        if(fenParts.length > 2 && !fenParts[2].equals("-")) {
            whiteRookOnFileSevenFirstMove = fenParts[2].contains("K") ? -1 : 0;
            whiteRookOnFileZeroFirstMove = fenParts[2].contains("Q") ? -1 : 0;
            blackRookOnFileSevenFirstMove = fenParts[2].contains("k") ? -1 : 0;
            blackRookOnFileZeroFirstMove = fenParts[2].contains("q") ? -1 : 0;
        }

        if(fenParts.length > 3 && !fenParts[3].equals("-")) {
            int firstRank = turn.opposite() == Color.WHITE ? 1 : 6;
            int delta = turn.opposite() == Color.WHITE ? 1 : -1;
            int file = fenParts[3].charAt(0) - 'a';
            Move move = new PawnMove(turn, firstRank, (byte) file, firstRank + 2 * delta, (byte) file, false, Pieces.NONE, false);
            moveHistory.add(move);
        }
    }

    private void initializeStartingPosition() {
        for(int i = 0; i < 8; i++) {
            board[i] = new BoardRow(0);
        }

        for(byte i = 0; i < 8; i++) {
            board[1].setFile(i, Color.WHITE, Pieces.PAWN);
            board[6].setFile(i, Color.BLACK, Pieces.PAWN);
        }

        board[0].setFile((byte) 0, Color.WHITE, Pieces.ROOK);
        board[0].setFile((byte) 1, Color.WHITE, Pieces.KNIGHT);
        board[0].setFile((byte) 2, Color.WHITE, Pieces.BISHOP);
        board[0].setFile((byte) 3, Color.WHITE, Pieces.QUEEN);
        board[0].setFile((byte) 4, Color.WHITE, Pieces.KING);
        board[0].setFile((byte) 5, Color.WHITE, Pieces.BISHOP);
        board[0].setFile((byte) 6, Color.WHITE, Pieces.KNIGHT);
        board[0].setFile((byte) 7, Color.WHITE, Pieces.ROOK);

        board[7].setFile((byte) 0, Color.BLACK, Pieces.ROOK);
        board[7].setFile((byte) 1, Color.BLACK, Pieces.KNIGHT);
        board[7].setFile((byte) 2, Color.BLACK, Pieces.BISHOP);
        board[7].setFile((byte) 3, Color.BLACK, Pieces.QUEEN);
        board[7].setFile((byte) 4, Color.BLACK, Pieces.KING);
        board[7].setFile((byte) 5, Color.BLACK, Pieces.BISHOP);
        board[7].setFile((byte) 6, Color.BLACK, Pieces.KNIGHT);
        board[7].setFile((byte) 7, Color.BLACK, Pieces.ROOK);
    }

    private List<Move> getPossibleMoves(int rank, byte file) {
        List<Move> moves = new ArrayList<>();
        Pieces piece = board[rank].getPiece(file);

        if(piece == Pieces.PAWN) {
            moves.addAll(PawnMove.getPossibleMoves(this, moveHistory, rank, file, true));
        } else if(piece == Pieces.KNIGHT) {
            moves.addAll(KnightMove.getPossibleMoves(this, rank, file));
        } else if(piece == Pieces.BISHOP || piece == Pieces.ROOK || piece == Pieces.QUEEN) {
            moves.addAll(SlidingMove.getPossibleMoves(this, rank, file, piece));
        } else if(piece == Pieces.KING) {
            moves.addAll(KingMove.getPossibleMoves(this, rank, file));
        }

        return moves;
    }

    public List<Move> getLegalMoves(int rank, byte file) {
        List<Move> moves = getPossibleMoves(rank, file);
        List<Move> legalMoves = new ArrayList<>();

        for(Move move : moves) {
            if(isMoveLegal(move)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    public List<Move> getAllLegalMoves() {
        List<Move> moves = new ArrayList<>();

        for(byte i = 0; i < 8; i++) {
            for(byte j = 0; j < 8; j++) {
                if(board[i].getColor(j) == turn && board[i].getPiece(j) != Pieces.NONE) {   
                    moves.addAll(getLegalMoves(i, j));
                }
            }
        }

        return moves;
    }

    private boolean isMoveLegal(Move move) {
        byte capturedPiece = Move.makeMove(board, move);
        boolean isLegal = !isKingInCheck(turn);
        move.setIsKingInCheck(isKingInCheck(turn.opposite()));
        Move.undoMove(board, move, capturedPiece);
        
        return isLegal;
    }

    private byte findKing(Color color) {
        byte king = 0;
        for(byte i = 0; i < 8; i++) {
            for(byte j = 0; j < 8; j++) {
                if(board[i].getPiece(j) == Pieces.KING && board[i].getColor(j) == color) {
                    king = (byte) ((i << 4) | j);
                    return king;
                }
            }
        }

        return (byte) -1;
    }

    public boolean isPassedPawn(int rank, byte file) {
        Pieces piece = board[rank].getPiece(file);
        Color color = turn;
        int lastFullRank = color == Color.WHITE ? 6 : 1;
        int delta = color == Color.WHITE ? 1 : -1;

        if(piece != Pieces.PAWN || color != turn) {
            return false;
        }
        
        for(int i = rank + delta; i < lastFullRank; i += delta) {
            for(int j=-1; j <= 1; j++) {
                if(file + j >= 0 && file + j < 8 && board[i].getPiece((byte) (file + j)) == Pieces.PAWN && board[i].getColor((byte) (file + j)) == color.opposite()) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isKingInCheck(Color color) {
        byte kingRankAndFile = findKing(color);

        if(kingRankAndFile == -1) {
            return false;
        }

        byte rank = (byte) (kingRankAndFile >> 4);
        byte file = (byte) (kingRankAndFile & 0b1111);

        return isSquareAttackedByColor(rank, file, color.opposite());
    }

    private boolean isSquareAttackedByColor(int rank, byte file, Color color) {
        for(int[] offset : Pieces.PAWN.getDirections(color.opposite())) {
            byte newRank = (byte) (rank + offset[0]);
            byte newFile = (byte) (file + offset[1]);
            if(Utilities.isOnBoard(newRank, newFile) && 
                board[newRank].getPiece(newFile) == Pieces.PAWN &&
                board[newRank].getColor(newFile) == color
            ) {
                return true;
            }
        }

        for(int[] offset : Pieces.KNIGHT.getDirections(color)) {
            byte newRank = (byte) (rank + offset[0]);
            byte newFile = (byte) (file + offset[1]);
            if(Utilities.isOnBoard(newRank, newFile) && 
                board[newRank].getPiece(newFile) == Pieces.KNIGHT &&
                board[newRank].getColor(newFile) == color
            ) {
                return true;
            }
        }

        for(int[] offset : Pieces.BISHOP.getDirections(color)) {
            for(int i = 1; i < 8; i++) {
                byte newRank = (byte) (rank + i * offset[0]);
                byte newFile = (byte) (file + i * offset[1]);
                if(Utilities.isOnBoard(newRank, newFile)) {
                    if(board[newRank].getPiece(newFile) == Pieces.BISHOP &&
                    board[newRank].getColor(newFile) == color) {
                        return true;
                    } else if (board[newRank].getPiece(newFile) != Pieces.NONE) {
                        break;
                    }
                }
            }
        }

        for(int[] offset : Pieces.ROOK.getDirections(color)) {
            for(int i = 1; i < 8; i++) {
                byte newRank = (byte) (rank + i * offset[0]);
                byte newFile = (byte) (file + i * offset[1]);
                if(Utilities.isOnBoard(newRank, newFile)) {
                    if(board[newRank].getPiece(newFile) == Pieces.ROOK &&
                    board[newRank].getColor(newFile) == color) {
                        return true;
                    } else if (board[newRank].getPiece(newFile) != Pieces.NONE) {
                        break;
                    }
                }
            }
        }

        for(int[] offset : Pieces.QUEEN.getDirections(color)) {
            for(int i = 1; i < 8; i++) {
                byte newRank = (byte) (rank + i * offset[0]);
                byte newFile = (byte) (file + i * offset[1]);
                if(Utilities.isOnBoard(newRank, newFile)) {
                    if(board[newRank].getPiece(newFile) == Pieces.QUEEN &&
                    board[newRank].getColor(newFile) == color) {
                        return true;
                    } else if (board[newRank].getPiece(newFile) != Pieces.NONE) {
                        break;
                    }
                }
            }
        }

        for(int[] offset : Pieces.KING.getDirections(color)) {
            byte newRank = (byte) (rank + offset[0]);
            byte newFile = (byte) (file + offset[1]);
            if(Utilities.isOnBoard(newRank, newFile) && 
                board[newRank].getPiece(newFile) == Pieces.KING &&
                board[newRank].getColor(newFile) == color
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean isGameOver() {
        return getAllLegalMoves().isEmpty();
    }

    public boolean isCheckMate() {
        return isGameOver() && isKingInCheck(turn);
    }

    public boolean isStaleMate() {
        return isGameOver() && !isKingInCheck(turn);
    }

    public boolean isThreeFoldRepetition() {
        for(String fen : positionCountMap.keySet()) {
            if(positionCountMap.get(fen) >= 3) {
                return true;
            }
        }
        return false;
    }

    public boolean onlyKingsLeftOnBoard() {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(board[i].getPiece((byte) j) != Pieces.KING && board[i].getPiece((byte) j) != Pieces.NONE) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean fiftyMoveRule() {
        if(moveHistory.size() < 100) {
            return false;
        }

        for(int i = moveHistory.size() - 1; i >= moveHistory.size() - 100; i--) {
            Move move = moveHistory.get(i);
            if(move.isCapture || move instanceof PawnMove) {
                return false;
            }
        }

        return true;
    }

    public boolean isKingSideCastleAvailable(Color color) {
        int rank = color == Color.WHITE ? 0 : 7;
        boolean movementCheck = (
            color == Color.WHITE ? 
                whiteKingFirstMove == -1 && whiteRookOnFileSevenFirstMove == -1 :
                blackKingFirstMove == -1 && blackRookOnFileSevenFirstMove == -1
        );
        return board[rank].getPiece((byte) 4) == Pieces.KING 
            && board[rank].getPiece((byte) 5) == Pieces.NONE 
            && board[rank].getPiece((byte) 6) == Pieces.NONE
            && board[rank].getPiece((byte) 7) == Pieces.ROOK
            && board[rank].getColor((byte) 7) == color
            && movementCheck
            && !isSquareAttackedByColor(rank, (byte) 4, color.opposite())
            && !isSquareAttackedByColor(rank, (byte) 5, color.opposite())
            && !isSquareAttackedByColor(rank, (byte) 6, color.opposite())
        ;
    }

    public boolean isQueenSideCastleAvailable(Color color) {
        int rank = color == Color.WHITE ? 0 : 7;
        boolean movementCheck = (
            color == Color.WHITE ? 
                whiteKingFirstMove == -1 && whiteRookOnFileZeroFirstMove == -1 :
                blackKingFirstMove == -1 && blackRookOnFileZeroFirstMove == -1
        );
        return board[rank].getPiece((byte) 4) == Pieces.KING 
            && board[rank].getPiece((byte) 3) == Pieces.NONE 
            && board[rank].getPiece((byte) 2) == Pieces.NONE
            && board[rank].getPiece((byte) 1) == Pieces.NONE
            && board[rank].getPiece((byte) 0) == Pieces.ROOK
            && board[rank].getColor((byte) 0) == color
            && movementCheck
            && !isSquareAttackedByColor(rank, (byte) 4, color.opposite())
            && !isSquareAttackedByColor(rank, (byte) 3, color.opposite())
            && !isSquareAttackedByColor(rank, (byte) 2, color.opposite())
        ;
    }
    
    public byte makeMove(Move move) {
        moveHistory.add(move);
        byte capturedPiece = Move.makeMove(board, move);
        turn = turn.opposite();

        if (move instanceof KingMove) {
            if (((KingMove) move).getColor() == Color.WHITE && whiteKingFirstMove == -1) {
                whiteKingFirstMove = moveHistory.size();
            } else if (((KingMove) move).getColor() == Color.BLACK && blackKingFirstMove == -1) {
                blackKingFirstMove = moveHistory.size();
            }
        }

        if (move instanceof SlidingMove && ((SlidingMove) move).getPiece() == Pieces.ROOK) {
            if (((SlidingMove) move).getColor() == Color.WHITE 
                && move.fromFile == 0 
                && whiteRookOnFileZeroFirstMove == -1
            ) {
                whiteRookOnFileZeroFirstMove = moveHistory.size();
            } else if (((SlidingMove) move).getColor() == Color.WHITE
                && move.fromFile == 7
                && whiteRookOnFileSevenFirstMove == -1
            ) {
                whiteRookOnFileSevenFirstMove = moveHistory.size();
            } else if (((SlidingMove) move).getColor() == Color.BLACK
                && move.fromFile == 0
                && blackRookOnFileZeroFirstMove == -1
            ) {
                blackRookOnFileZeroFirstMove = moveHistory.size();
            } else if (((SlidingMove) move).getColor() == Color.BLACK
                && move.fromFile == 7
                && blackRookOnFileSevenFirstMove == -1
            ) {
                blackRookOnFileSevenFirstMove = moveHistory.size();
            }
        }

        String fen = getFEN();
        positionCountMap.put(fen, positionCountMap.getOrDefault(fen, 0) + 1);

        return capturedPiece;
    }

    public void undoMove(Move move, byte capturedPiece) {
        String fen = getFEN();
        positionCountMap.put(fen, positionCountMap.get(fen) - 1);
        if(positionCountMap.get(fen) == 0) {
            positionCountMap.remove(fen);
        }

        if (move instanceof KingMove) {
            if (((KingMove) move).getColor() == Color.WHITE && whiteKingFirstMove == moveHistory.size()) {
                whiteKingFirstMove = -1;
            } else if (((KingMove) move).getColor() == Color.BLACK && blackKingFirstMove == moveHistory.size()) {
                blackKingFirstMove = -1;
            }
        }

        if (move instanceof SlidingMove && ((SlidingMove) move).getPiece() == Pieces.ROOK) {
            if (((SlidingMove) move).getColor() == Color.WHITE
                && move.fromFile == 0
                && whiteRookOnFileZeroFirstMove == moveHistory.size()
            ) {
                whiteRookOnFileZeroFirstMove = -1;
            } else if (((SlidingMove) move).getColor() == Color.WHITE
                && move.fromFile == 7
                && whiteRookOnFileSevenFirstMove == moveHistory.size()
            ) {
                whiteRookOnFileSevenFirstMove = -1;
            } else if (((SlidingMove) move).getColor() == Color.BLACK
                && move.fromFile == 0
                && blackRookOnFileZeroFirstMove == moveHistory.size()
            ) {
                blackRookOnFileZeroFirstMove = -1;
            } else if (((SlidingMove) move).getColor() == Color.BLACK
                && move.fromFile == 7
                && blackRookOnFileSevenFirstMove == moveHistory.size()
            ) {
                blackRookOnFileSevenFirstMove = -1;
            }
        }

        turn = turn.opposite();
        Move.undoMove(board, move, capturedPiece);
        moveHistory.remove(moveHistory.size() - 1);
    }

    public Color getTurn() {
        return turn;
    }

    public void setTurn(Color turn) {
        this.turn = turn;
    }

    public GameResult getWinner() {
        return winner;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public void setWinner(GameResult winner) {
        this.winner = winner;
    }

    public void printPGN() {
        for(int i=0, j=1; i < moveHistory.size(); i+=2, j++) {
            Move whiteMove = moveHistory.get(i);
            Pieces whitePiece = whiteMove instanceof SlidingMove ? ((SlidingMove) whiteMove).getPiece() : Pieces.NONE;
            
            String pgnString = j + ". " + (whiteMove instanceof SlidingMove ? whiteMove.toString(whitePiece) : whiteMove.toString());
            
            if (i+1 < moveHistory.size()) {
                Move blackMove = moveHistory.get(i + 1);
                Pieces blackPiece = blackMove instanceof SlidingMove ? ((SlidingMove) blackMove).getPiece() : Pieces.NONE;
                pgnString += " " + (blackMove instanceof SlidingMove ? blackMove.toString(blackPiece) : blackMove.toString());
            }

            System.out.println(pgnString);
        }
    }

    public double minimax(int depth, NeuralNetwork neuralNetwork, boolean maximizingPlayer, double alpha, double beta) {
        String fen = getFEN();

        // 1. Lookup in TT
        TTEntry entry = transpositionTable.get(fen);
        if (entry != null && entry.depth >= depth) {
            if (entry.flag == TTFlag.EXACT) return entry.score;
            if (entry.flag == TTFlag.LOWERBOUND && entry.score >= beta) return entry.score;
            if (entry.flag == TTFlag.UPPERBOUND && entry.score <= alpha) return entry.score;
        }

        if (onlyKingsLeftOnBoard() || fiftyMoveRule() || isThreeFoldRepetition()) {
            double evaluation = 0.0; // Default value Draw
            return evaluation; // Draw
        }

        if (depth <= 0) {
            double evaluation = neuralNetwork.evaluate(fen);
            return evaluation;
        }

        List<Move> moves = getAllLegalMoves();
        orderMoves(moves);
        if (moves.isEmpty()) {
            double evaluation = 0.0; // Default value Stalemate
            if (isKingInCheck(turn)) {
                evaluation = (turn == Color.WHITE) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY; // Checkmate
            }

            return evaluation;
        }

        // Order moves: try TT best move first if exists
        if (entry != null && entry.bestMove != null) {
            moves.remove(entry.bestMove);
            moves.add(0, entry.bestMove);
        }

        double bestValue = maximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        Move bestMove = null;
        double alphaOrginal = alpha;
        double betaOrginal = beta;
        for (Move move : moves) {
            byte capturedPiece = makeMove(move);

            int newDepth = depth - 1;
            double result = minimax(newDepth, neuralNetwork, !maximizingPlayer, alpha, beta);
            double value = result;
            undoMove(move, capturedPiece);

            if (maximizingPlayer) {
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestValue);
            } else {
                if (value < bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
                beta = Math.min(beta, bestValue);
            }

            if (beta <= alpha) {
                if(bestMove == null) {
                    bestMove = move;
                }
                break; // Alpha-Beta cutoff
            }
        }

        // 3. Save in TT
        TTFlag flag;
        if (bestValue <= alphaOrginal) flag = TTFlag.UPPERBOUND;
        else if (bestValue >= betaOrginal) flag = TTFlag.LOWERBOUND;
        else flag = TTFlag.EXACT;
        transpositionTable.put(fen, new TTEntry(fen, bestValue, depth, flag, bestMove));
        
        return bestValue;
    }

    private void orderMoves(List<Move> moves) {
        moves.sort((move1, move2) -> {
            // Captures first
            if (move1.isCapture != move2.isCapture) {
                return move1.isCapture ? -1 : 1;
            }
            // Checks next
            if (move1.isKingInCheck != move2.isKingInCheck) {
                return move1.isKingInCheck ? -1 : 1;
            }
            // Promotions next
            boolean isPromotion1 = move1 instanceof PawnMove && ((PawnMove) move1).isPromotion();
            boolean isPromotion2 = move2 instanceof PawnMove && ((PawnMove) move2).isPromotion();
            if (isPromotion1 != isPromotion2) {
                return isPromotion1 ? -1 : 1;
            }
            // en passant captures next
            boolean isEnPassant1 = move1 instanceof PawnMove && ((PawnMove) move1).isEnPassant();
            boolean isEnPassant2 = move2 instanceof PawnMove && ((PawnMove) move2).isEnPassant();
            if (isEnPassant1 != isEnPassant2) {
                return isEnPassant1 ? -1 : 1;
            }
            // Castling next
            boolean isCastle1 = move1 instanceof KingMove && ((KingMove) move1).isCastlingMove();
            boolean isCastle2 = move2 instanceof KingMove && ((KingMove) move2).isCastlingMove();
            if (isCastle1 != isCastle2) {
                return isCastle1 ? -1 : 1;
            }
            // Higher value piece moves first (for SlidingMove)
            int value1 = (move1 instanceof SlidingMove) ? ((SlidingMove) move1).getPiece().getValue() : 0;
            int value2 = (move2 instanceof SlidingMove) ? ((SlidingMove) move2).getPiece().getValue() : 0;
            if (value1 != value2) {
                return Integer.compare(value2, value1);
            }
            // Otherwise, maintain original order
            return 0;
        });
    }

    public Move getBestMove() {
        Move bestMove = transpositionTable.get(getFEN()).bestMove;
        return bestMove;
    }

    public int getDepthExtensionWithPhase(int maxDepth) {
        double maxPhase = 24.0;
        double phase = maxPhase;

        // Assign piece phase values
        phase -= getNumberOfPiecesOnBoard(Pieces.KNIGHT) * 1;
        phase -= getNumberOfPiecesOnBoard(Pieces.BISHOP) * 1;
        phase -= getNumberOfPiecesOnBoard(Pieces.ROOK)   * 2;
        phase -= getNumberOfPiecesOnBoard(Pieces.QUEEN)  * 4;

        // Normalize phase between 1 (endgame) and 0 (opening)
        double gamePhase = (double)phase / maxPhase;
        return (int) Math.floor(maxDepth + 3 * gamePhase);
    }

    private int getNumberOfPiecesOnBoard(Pieces piece) {
        int count = 0;
        for (byte i = 0; i < 8; i++) {
            for (byte j = 0; j < 8; j++) {
                if (board[i].getPiece(j) == piece) {
                    count++;
                }
            }
        }
        return count;
    }
}
