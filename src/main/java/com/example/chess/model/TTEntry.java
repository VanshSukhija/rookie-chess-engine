package com.example.chess.model;

class TTEntry {
    public String fen;
    public double score;
    public int depth;
    public TTFlag flag;  // EXACT, LOWERBOUND, UPPERBOUND
    public Move bestMove;

    public TTEntry(String fen, double score, int depth, TTFlag flag, Move bestMove) {
        this.fen = fen;
        this.score = score;
        this.depth = depth;
        this.flag = flag;
        this.bestMove = bestMove;
    }
}
