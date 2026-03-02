package main;

import java.util.ArrayList;

public class Node {

    private char origem;
    private char dest;
    private boolean turn; // true = branco; false = preto
    private char[][] matriz;
    private int miniMax;
    private ArrayList<Node> children;

    public Node() {

    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public char getDest() {
        return dest;
    }

    public char[][] getMatriz() {
        return matriz;
    }

    public int getMiniMax() {
        return miniMax;
    }

    public char getOrigem() {
        return origem;
    }

    public boolean getTurn() {
        return turn;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public void setDest(char dest) {
        this.dest = dest;
    }

    public void setMatriz(char[][] matriz) {
        this.matriz = matriz;
    }

    public void setMiniMax(int miniMax) {
        this.miniMax = miniMax;
    }

    public void setOrigem(char origem) {
        this.origem = origem;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }
}