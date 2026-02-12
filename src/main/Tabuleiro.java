/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author Douglas
 */
public class Tabuleiro implements Cloneable {

    private int[][] matriz;
    private final int TAMANHO = 6;

    public Tabuleiro() {
        this.matriz = new int[TAMANHO][TAMANHO];
        inicializar();
    }

    private void inicializar() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if ((i + j) % 2 != 0) {
                    if (i < 2) {
                        matriz[i][j] = 2; // Pretas
                    } else if (i > 3) {
                        matriz[i][j] = 1; // Brancas
                    }
                } else {
                    matriz[i][j] = -2; // Espaço Branco
                }
            }
        }
    }

    @Override
    public Tabuleiro clone() {
        try {
            Tabuleiro clone = (Tabuleiro) super.clone();
            clone.matriz = new int[TAMANHO][];
            for (int i = 0; i < TAMANHO; i++) {
                clone.matriz[i] = this.matriz[i].clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    
    /*
        Implmentação dos métodos - getMovimentosPossiveis(), fazerMovimento(), etc
    */

    public int[][] getMatriz() {
        return matriz;
    }

    public void setMatriz(int[][] matriz) {
        this.matriz = matriz;
    }
}
