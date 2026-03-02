package main;

public class Tabuleiro implements Cloneable {

    private char[][] matriz;
    private final int TAMANHO = 6;
    private int[] pecas; // pecas[1] = brancas, pecas[2] = pretas
    private int[] damas; // damas[1] = damas brancas, damas[2] = damas pretas (3 e 4)

    public Tabuleiro() {
        this.matriz = new char[TAMANHO][TAMANHO];
        this.pecas = new int[] { 0, TAMANHO, TAMANHO }; // a primeira posicao é ignoravel pra facilitar depois
        this.damas = new int[] { 0, 0, 0 };
        inicializar();
    }

    private void inicializar() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if ((i + j) % 2 != 0) {

                    matriz[i][j] = '0';

                    if (i < 2) {
                        matriz[i][j] = '2'; // Pretas
                    } else if (i > 3) {
                        matriz[i][j] = '1'; // Brancas
                    }
                } else {
                    matriz[i][j] = 'b'; // Espaço Branco
                }
            }
        }
    }

    /**
     * Remove uma peça capturada da posição e atualiza as contagens.
     */
    public void removerPeca(int linha, int col) {
        int peca = matriz[linha][col];
        if (peca == '0' || peca == 'b')
            return;

        if (peca <= '2') {
            pecas[peca - '0']--;
        } else {
            // Dama: '3' é dama branca (time 1), '4' é dama preta (time 2)
            int time = peca - '0' - 2; // '3'->1, '4'->2
            damas[time]--;
            pecas[time]--;
        }
        matriz[linha][col] = '0';
    }

    @Override
    public Tabuleiro clone() {
        try {
            Tabuleiro clone = (Tabuleiro) super.clone();
            clone.matriz = new char[TAMANHO][];
            for (int i = 0; i < TAMANHO; i++) {
                clone.matriz[i] = this.matriz[i].clone();
            }
            clone.pecas = this.pecas.clone();
            clone.damas = this.damas.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public char[][] getMatriz() {
        return matriz;
    }

    public void setMatriz(char[][] matriz) {
        this.matriz = matriz;
    }

    public int[] getPecas() {
        return pecas;
    }

    public int[] getDamas() {
        return damas;
    }

    public int getTamanho() {
        return TAMANHO;
    }
}
