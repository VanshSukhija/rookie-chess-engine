package com.example.chess.model;

import java.util.*;

import com.example.chess.utils.Utilities;

public class Board {
    /*
     * color - 1 bit
     * piece type - 3 bits
     * square representation - ....|....|....|....|....|....|....|.... => color-piece
     * board representation - array of 8 32-bit integers for each rank
     */

    private static BoardRow[] board;
    private static List<Move> moveHistory = new ArrayList<>();
    private Color turn = Color.WHITE;
    private final Color playerColor;
    private GameResult winner;
    private static int whiteKingFirstMove = -1;
    private static int blackKingFirstMove = -1;
    private static int whiteRookOnFileZeroFirstMove = -1;
    private static int whiteRookOnFileSevenFirstMove = -1;
    private static int blackRookOnFileZeroFirstMove = -1;
    private static int blackRookOnFileSevenFirstMove = -1;

    public Board(Color playerColor) {
        board = new BoardRow[8];
        initializeStartingPosition();
        this.playerColor = playerColor;
        this.winner = GameResult.ONGOING;
    }

    public Board(String fen, Color playerColor) {
        board = new BoardRow[8];
        initializeFromFEN(fen);
        this.playerColor = playerColor;
        this.winner = GameResult.ONGOING;
    }

    public BoardRow[] getBoardRows() {
        return board;
    }

    public String getFEN() {
        String fen = "";
        for(int i=7; i >= 0; i--) {
            fen += getBoardRow(i).getFen();
            if(i != 0) {
                fen += "/";
            }
        }
        return fen;
    }

    public BoardRow getBoardRow(int rowIndex) {
        return board[rowIndex];
    }

    private void initializeFromFEN(String fen) {
        String[] fenParts = fen.split(" ");
        String[] ranks = fenParts[0].split("/");

        for(int i = 0; i < ranks.length; i++) {
            board[7 - i] = new BoardRow(0);
            for(int j=0; j < ranks[i].length(); j++) {
                if(Character.isDigit(ranks[i].charAt(j))) {
                    for(int k=0; k < Character.getNumericValue(ranks[i].charAt(j)); k++) {
                        board[7 - i].setFile((byte) j, Color.BLACK, Pieces.NONE);
                    }
                } else if (Character.isLowerCase(ranks[i].charAt(j))) {
                    board[7 - i].setFile((byte) j, Color.BLACK, Utilities.getPieceFromSymbol(Character.toUpperCase(ranks[i].charAt(j))));
                } else if (Character.isUpperCase(ranks[i].charAt(j))) {
                    board[7 - i].setFile((byte) j, Color.WHITE, Utilities.getPieceFromSymbol(Character.toUpperCase(ranks[i].charAt(j))));
                    
                }
            }
        }

        turn = fenParts[1].equals("w") ? Color.WHITE : Color.BLACK;
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
            moves.addAll(PawnMove.getPossibleMoves(board, moveHistory, rank, file, true));
        } else if(piece == Pieces.KNIGHT) {
            moves.addAll(KnightMove.getPossibleMoves(board, rank, file));
        } else if(piece == Pieces.BISHOP || piece == Pieces.ROOK || piece == Pieces.QUEEN) {
            moves.addAll(SlidingMove.getPossibleMoves(board, rank, file, piece));
        } else if(piece == Pieces.KING) {
            moves.addAll(KingMove.getPossibleMoves(board, rank, file));
        }

        return moves;
    }

    private List<Move> getLegalMoves(int rank, byte file) {
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
        move.setIsKingInCheck(!isLegal);
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

    public boolean isKingInCheck(Color color) {
        byte kingRankAndFile = findKing(color);

        if(kingRankAndFile == -1) {
            return false;
        }

        byte rank = (byte) (kingRankAndFile >> 4);
        byte file = (byte) (kingRankAndFile & 0b1111);

        return isSquareAttackedByColor(rank, file, color.opposite());
    }

    private static boolean isSquareAttackedByColor(int rank, byte file, Color color) {
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
        if(moveHistory.size() < 6) {
            return false;
        }

        int size = moveHistory.size();
        return moveHistory.get(size - 1).equals(moveHistory.get(size - 5)) &&
            moveHistory.get(size - 5).equals(moveHistory.get(size - 9)) &&
            moveHistory.get(size - 3).equals(moveHistory.get(size - 7)) &&
            moveHistory.get(size - 1).isOpposite(moveHistory.get(size - 3)) &&
            moveHistory.get(size - 2).equals(moveHistory.get(size - 6)) &&
            moveHistory.get(size - 6).equals(moveHistory.get(size - 10)) &&
            moveHistory.get(size - 4).equals(moveHistory.get(size - 8)) &&
            moveHistory.get(size - 2).isOpposite(moveHistory.get(size - 4));
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
            String moveString = (move instanceof SlidingMove ? move.toString(((SlidingMove) move).getPiece()) : move.toString());
            if(moveString.contains("x")) {
                return false;
            }
        }

        return true;
    }

    public static boolean isKingSideCastleAvailable(Color color) {
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

    public static boolean isQueenSideCastleAvailable(Color color) {
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
            if (((SlidingMove) move).getColor() == Color.WHITE && move.fromFile == 0 && whiteRookOnFileZeroFirstMove == -1) {
                whiteRookOnFileZeroFirstMove = moveHistory.size();
            } else if (((SlidingMove) move).getColor() == Color.WHITE && move.fromFile == 7 && whiteRookOnFileSevenFirstMove == -1) {
                whiteRookOnFileSevenFirstMove = moveHistory.size();
            } else if (((SlidingMove) move).getColor() == Color.BLACK && move.fromFile == 0 && blackRookOnFileZeroFirstMove == -1) {
                blackRookOnFileZeroFirstMove = moveHistory.size();
            } else if (((SlidingMove) move).getColor() == Color.BLACK && move.fromFile == 7 && blackRookOnFileSevenFirstMove == -1) {
                blackRookOnFileSevenFirstMove = moveHistory.size();
            }
        }

        return capturedPiece;
    }

    public void undoMove(Move move, byte capturedPiece) {
        if (move instanceof KingMove) {
            if (((KingMove) move).getColor() == Color.WHITE && whiteKingFirstMove == moveHistory.size()) {
                whiteKingFirstMove = -1;
            } else if (((KingMove) move).getColor() == Color.BLACK && blackKingFirstMove == moveHistory.size()) {
                blackKingFirstMove = -1;
            }
        }

        if (move instanceof SlidingMove && ((SlidingMove) move).getPiece() == Pieces.ROOK) {
            if (((SlidingMove) move).getColor() == Color.WHITE && move.fromFile == 0 && whiteRookOnFileZeroFirstMove == moveHistory.size()) {
                whiteRookOnFileZeroFirstMove = -1;
            } else if (((SlidingMove) move).getColor() == Color.WHITE && move.fromFile == 7 && whiteRookOnFileSevenFirstMove == moveHistory.size()) {
                whiteRookOnFileSevenFirstMove = -1;
            } else if (((SlidingMove) move).getColor() == Color.BLACK && move.fromFile == 0 && blackRookOnFileZeroFirstMove == moveHistory.size()) {
                blackRookOnFileZeroFirstMove = -1;
            } else if (((SlidingMove) move).getColor() == Color.BLACK && move.fromFile == 7 && blackRookOnFileSevenFirstMove == moveHistory.size()) {
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

    public void printMoveHistory() {
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
}
