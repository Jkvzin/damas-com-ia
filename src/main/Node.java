package main;

import java.util.ArrayList;

public class Node {

    private char origem;
    private char dest;
    private boolean turn; // true = branco; false = preto
    private char[][] matriz;
    private int miniMax;
    private ArrayList<Node> children;

    /*
     * minimax
     * 0 empate
     * 1 vitória
     * -1 derrota
     * 
     * vitória + 100
     * derrota - 100
     * empate -1
     * dama + 2
     * normal + 1
     * se é vezIA e temCapturaDisponivel + 1
     * // // // // e combo possivel + (tamanho do combo)
     * //
     * 
     * turn - maximizar ou minimizar o valor do minimax dos filhos
     */

    public Node() {
        children = new ArrayList<>();
        miniMax = Integer.MIN_VALUE;
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

    public void setChildren(Node child) {
        this.children.add(child);
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